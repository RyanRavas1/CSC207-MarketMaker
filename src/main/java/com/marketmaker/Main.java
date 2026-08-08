package com.marketmaker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.marketmaker.config.EnvLoader;
import com.marketmaker.config.exceptions.MissingEnvironmentVariableException;
import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.data_access.FinnhubApiClient;
import com.marketmaker.data_access.JsonFileAccountDataAccessObject;
import com.marketmaker.data_access.PolledQuoteSubscription;
import com.marketmaker.data_access.PriceFeedQuoteDataAccessObject;
import com.marketmaker.data_access.AlphaVantageHistoricalDataAccessObject;
import com.marketmaker.data_access.CandleFileCache;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Quote;
import com.marketmaker.interface_adapter.CancelOrderController;
import com.marketmaker.interface_adapter.CandlestickChartPresenter;
import com.marketmaker.interface_adapter.ChartController;
import com.marketmaker.interface_adapter.OrderFeedbackPresenter;
import com.marketmaker.interface_adapter.OrderHistoryPresenter;
import com.marketmaker.interface_adapter.OrderTicketController;
import com.marketmaker.interface_adapter.PortfolioSummaryPresenter;
import com.marketmaker.interface_adapter.PositionsPresenter;
import com.marketmaker.interface_adapter.ProfileController;
import com.marketmaker.interface_adapter.ProfilePresenter;
import com.marketmaker.interface_adapter.RealizedPnLController;
import com.marketmaker.interface_adapter.RealizedPnLPresenter;
import com.marketmaker.interface_adapter.TickerSearchPresenter;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.interface_adapter.WatchlistController;
import com.marketmaker.interface_adapter.WatchlistPresenter;
import com.marketmaker.price_feed.FinnhubPriceFeed;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.price_feed.ReplayPriceFeed;
import com.marketmaker.use_case.add_to_watchlist.AddToWatchlistInteractor;
import com.marketmaker.use_case.add_to_watchlist.AddToWatchlistOutputBoundary;
import com.marketmaker.use_case.add_to_watchlist.AddToWatchlistResponseModel;
import com.marketmaker.use_case.calculate_realized_pnl.CalculateRealizedPnLInteractor;
import com.marketmaker.use_case.cancel_order.CancelOrderInteractor;
import com.marketmaker.use_case.create_account.CreateAccountInteractor;
import com.marketmaker.use_case.create_account.CreateAccountOutputBoundary;
import com.marketmaker.use_case.create_account.CreateAccountRequestModel;
import com.marketmaker.use_case.create_account.CreateAccountResponseModel;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersInteractor;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersOutputBoundary;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersRequestModel;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersResponseModel;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderInteractor;
import com.marketmaker.use_case.place_order.PlaceOrderInteractor;
import com.marketmaker.use_case.receive_live_quotes.ReceiveLiveQuoteUpdatesInteractor;
import com.marketmaker.use_case.remove_from_watchlist.RemoveFromWatchlistInteractor;
import com.marketmaker.use_case.remove_from_watchlist.RemoveFromWatchlistOutputBoundary;
import com.marketmaker.use_case.remove_from_watchlist.RemoveFromWatchlistResponseModel;
import com.marketmaker.use_case.search_ticker.SearchTickerInteractor;
import com.marketmaker.use_case.search_ticker.TickerDataAccessInterface;
import com.marketmaker.use_case.user_profile.ViewProfileInteractor;
import com.marketmaker.use_case.user_profile.ViewProfileResponseModel;
import com.marketmaker.use_case.watchlist.WatchlistInteractor;
import com.marketmaker.use_case.watchlist.WatchlistRequestModel;
import com.marketmaker.use_case.view_candlestick_chart.HistoricalDataAccessInterface;
import com.marketmaker.use_case.view_candlestick_chart.Resolution;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartInteractor;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartResponseModel;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryInteractor;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryRequestModel;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryInteractor;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryRequestModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;
import com.marketmaker.use_case.view_positions.ViewPositionsInteractor;
import com.marketmaker.use_case.view_positions.ViewPositionsRequestModel;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;
import com.marketmaker.view.DashboardFrame;

/** Builds the object graph and keeps the dashboard fed with live data. */
public class Main {
    private static final String DATA_DIRECTORY = "data";
    private static final String ACCOUNT_ID = "demo";
    private static final String CHART_TICKER = "AAPL";
    private static final List<String> DEFAULT_WATCHLIST = List.of("AAPL", "MSFT", "NVDA");
    // Slower than the price feed's own cache, so an idle screen re-polls rather than
    // re-serving the same quote over and over.
    private static final int REFRESH_INTERVAL_MS = 10_000;
    // Finnhub's free tier allows 60 calls a minute and a refresh spends one per watched
    // ticker. Aiming at 40 leaves room for the odd manual Refresh on top of the timer.
    private static final int CALLS_PER_MINUTE = 40;

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    // Quoting a ticker is an HTTP round trip, so it can't run on the event dispatch thread.
    // One worker for the whole app keeps the refreshes serialized in the order they were asked.
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "use-case");
        thread.setDaemon(true);
        return thread;
    });

    public static void main(String[] args) {
        // Use the cross-platform look so it renders the same on everyone's machine.
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        }
        catch (UnsupportedLookAndFeelException e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        PriceFeed priceFeed = createPriceFeed();
        TickerDataAccessInterface quotes = new PriceFeedQuoteDataAccessObject(priceFeed);
        AccountDAO accountDAO = new JsonFileAccountDataAccessObject(Path.of(DATA_DIRECTORY));
        seedAccount(accountDAO);

        SwingUtilities.invokeLater(() -> {
            ViewModel<ViewPortfolioSummaryResponseModel> summary = new ViewModel<>();
            ViewModel<ViewPositionsResponseModel> positions = new ViewModel<>();
            ViewModel<ViewOrderHistoryResponseModel> orderHistory = new ViewModel<>();
            ViewModel<ViewCandlestickChartResponseModel> chart = new ViewModel<>();
            ViewModel<List<Quote>> watchlist = new ViewModel<>();
            ViewModel<String> status = new ViewModel<>();
            ViewModel<ViewProfileResponseModel> profile = new ViewModel<>();

            ViewPortfolioSummaryInteractor summaryInteractor = new ViewPortfolioSummaryInteractor(
                    accountDAO, quotes, new PortfolioSummaryPresenter(summary));
            ViewPositionsInteractor positionsInteractor = new ViewPositionsInteractor(
                    accountDAO, quotes, new PositionsPresenter(positions));
            ViewOrderHistoryInteractor historyInteractor = new ViewOrderHistoryInteractor(
                    accountDAO, new OrderHistoryPresenter(orderHistory));
            ViewCandlestickChartInteractor chartInteractor = new ViewCandlestickChartInteractor(
                    createHistoricalData(), new CandlestickChartPresenter(chart));
            ChartController chartController = new ChartController(
                    chartInteractor, WORKER, CHART_TICKER, Resolution.ONE_MONTH);

            Runnable[] refreshHolder = new Runnable[1];
            Runnable refresh = () -> refreshHolder[0].run();

            OrderFeedbackPresenter feedback = new OrderFeedbackPresenter(status, refresh);
            OrderTicketController ticketController = new OrderTicketController(
                    new PlaceOrderInteractor(accountDAO, priceFeed, feedback),
                    new PlaceLimitStopOrderInteractor(accountDAO, feedback),
                    ACCOUNT_ID);
            CancelOrderController cancelController = new CancelOrderController(
                    new CancelOrderInteractor(accountDAO, feedback), ACCOUNT_ID);
            RealizedPnLPresenter realizedPnLPresenter = new RealizedPnLPresenter();
            RealizedPnLController realizedPnLController = new RealizedPnLController(
                    new CalculateRealizedPnLInteractor(accountDAO, realizedPnLPresenter),
                    realizedPnLPresenter, ACCOUNT_ID);
            ProfileController profileController = new ProfileController(
                    new ViewProfileInteractor(accountDAO, priceFeed, new ProfilePresenter(profile)),
                    ACCOUNT_ID);
            MatchPendingOrdersInteractor matchInteractor = new MatchPendingOrdersInteractor(
                    accountDAO, silentMatcher());
            // The app polls rather than streams, so subscribing is a no-op today; keeping the
            // use case in the path means a websocket adapter is a swap, not a rewrite.
            ReceiveLiveQuoteUpdatesInteractor liveQuotes = new ReceiveLiveQuoteUpdatesInteractor(
                    new PolledQuoteSubscription(), update -> { });
            // Adds are checked against the market first, so a typo is refused rather than
            // parked on the watchlist for ever with no price beside it.
            TickerSearchPresenter search = new TickerSearchPresenter(watchlist);
            WatchlistController watchlistController = new WatchlistController(
                    addToWatchlist(accountDAO), removeFromWatchlist(accountDAO), liveQuotes,
                    new SearchTickerInteractor(quotes, search), search, WORKER,
                    ACCOUNT_ID, refresh);
            // A resting order fills off an incoming price, so the quotes drawn on the
            // watchlist are offered to the matcher on their way to the screen.
            WatchlistInteractor watchlistInteractor = new WatchlistInteractor(priceFeed,
                    new WatchlistPresenter(watchlist, latest -> {
                        for (Quote quote : latest) {
                            matchInteractor.execute(new MatchPendingOrdersRequestModel(ACCOUNT_ID, quote));
                        }
                    }));

            refreshHolder[0] = () -> WORKER.execute(() -> {
                summaryInteractor.execute(new ViewPortfolioSummaryRequestModel(ACCOUNT_ID));
                positionsInteractor.execute(new ViewPositionsRequestModel(ACCOUNT_ID));
                historyInteractor.execute(new ViewOrderHistoryRequestModel(ACCOUNT_ID));
                chartController.reload();
                watchlistInteractor.execute(new WatchlistRequestModel(watchedTickers(accountDAO)));
            });

            Timer timer = new Timer(REFRESH_INTERVAL_MS, event -> refresh.run());
            // A long watchlist costs more calls per tick, so it has to tick less often. The
            // alternative is a rate limit part-way through a refresh, which shows up as some
            // tickers holding yesterday's price with nothing on screen to say why.
            watchlist.onState(priced -> timer.setDelay(intervalFor(priced.size())));
            new DashboardFrame(ticketController, watchlistController, chartController,
                    cancelController, realizedPnLController, profileController,
                    status, profile, refresh, live -> toggle(timer, live),
                    summary, positions, orderHistory, chart, watchlist).setVisible(true);

            refresh.run();
            timer.start();
        });
    }

    /** @return how long to leave between refreshes so {@code tickers} stays inside the quota */
    static int intervalFor(int tickers) {
        int needed = (int) Math.ceil(tickers * 60_000.0 / CALLS_PER_MINUTE);
        return Math.max(REFRESH_INTERVAL_MS, needed);
    }

    private static void toggle(Timer timer, boolean live) {
        if (live) {
            timer.start();
        } else {
            timer.stop();
        }
    }

    /**
     * First launch: open the paper account, then give it something to watch.
     *
     * <p>The use case decides the starting balance and refuses a second account under the
     * same id, so this runs unconditionally and the refusal is the "already open" case.
     */
    private static void seedAccount(AccountDAO accountDAO) {
        new CreateAccountInteractor(accountDAO, new CreateAccountOutputBoundary() {
            @Override
            public void presentSuccess(CreateAccountResponseModel response) {
                Account account = accountDAO.get(response.getAccountId());
                for (String ticker : DEFAULT_WATCHLIST) {
                    account.getWatchlist().add(ticker);
                }
                accountDAO.save(account);
                LOGGER.info(() -> "Opened account " + response.getAccountId()
                        + " with " + response.getStartingBalance());
            }

            @Override
            public void presentFailure(String errorMessage) {
                // Already open, which is the normal case on every launch after the first.
            }
        }).execute(new CreateAccountRequestModel(ACCOUNT_ID));
    }

    private static List<String> watchedTickers(AccountDAO accountDAO) {
        Account account = accountDAO.get(ACCOUNT_ID);
        return account == null ? List.of() : List.copyOf(account.getWatchlist().getTickers());
    }

    /** One quote per watched ticker; a symbol the feed can't price is left off the table. */

    // The matcher reports each fill it makes; the screens are reloaded on the same tick
    // anyway, so there is nothing extra for this to do.
    private static MatchPendingOrdersOutputBoundary silentMatcher() {
        return new MatchPendingOrdersOutputBoundary() {
            @Override
            public void presentFill(MatchPendingOrdersResponseModel response) {
                LOGGER.info(() -> "Filled resting order on " + response.getTicker());
            }
        };
    }

    private static AddToWatchlistInteractor addToWatchlist(AccountDAO accountDAO) {
        return new AddToWatchlistInteractor(accountDAO, new PolledQuoteSubscription(),
                quote -> { }, new AddToWatchlistOutputBoundary() {
            @Override
            public void presentSuccess(AddToWatchlistResponseModel response) {
            }

            @Override
            public void presentFailure(String errorMessage) {
                LOGGER.warning(errorMessage);
            }
        });
    }

    private static RemoveFromWatchlistInteractor removeFromWatchlist(AccountDAO accountDAO) {
        return new RemoveFromWatchlistInteractor(accountDAO, new PolledQuoteSubscription(),
                new RemoveFromWatchlistOutputBoundary() {
            @Override
            public void presentSuccess(RemoveFromWatchlistResponseModel response) {
            }

            @Override
            public void presentFailure(String errorMessage) {
                LOGGER.warning(errorMessage);
            }
        });
    }

    /**
     * Live quotes when a Finnhub key is configured, the replay feed when one isn't, so the
     * app still runs for anyone who hasn't set up a key.
     */
    private static HistoricalDataAccessInterface createHistoricalData() {
        try {
            return new AlphaVantageHistoricalDataAccessObject(EnvLoader.get("ALPHAVANTAGE_API_KEY"),
                    new CandleFileCache(Path.of(DATA_DIRECTORY, "candles")));
        } catch (MissingEnvironmentVariableException exception) {
            LOGGER.info("No ALPHAVANTAGE_API_KEY found — the chart will stay empty. "
                    + "Copy src/.env.example to .env and add a key for price history.");
            // Better an empty chart that says so than invented prices beside a live watchlist.
            return (ticker, resolution) -> List.of();
        }
    }

    private static PriceFeed createPriceFeed() {
        try {
            return new FinnhubPriceFeed(new FinnhubApiClient(EnvLoader.get("FINNHUB_API_KEY")));
        } catch (MissingEnvironmentVariableException exception) {
            LOGGER.info("No FINNHUB_API_KEY found — using the replay feed. "
                    + "Copy src/.env.example to .env and add a key for live prices.");
            Map<String, Double> startingPrices = new HashMap<>();
            startingPrices.put("AAPL", 190.0);
            startingPrices.put("MSFT", 410.0);
            startingPrices.put("NVDA", 120.0);
            return new ReplayPriceFeed(startingPrices);
        }
    }
}
