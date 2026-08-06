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
import com.marketmaker.data_access.PriceFeedQuoteDataAccessObject;
import com.marketmaker.data_access.StubHistoricalDataAccessObject;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Quote;
import com.marketmaker.interface_adapter.CandlestickChartPresenter;
import com.marketmaker.interface_adapter.OrderHistoryPresenter;
import com.marketmaker.interface_adapter.PortfolioSummaryPresenter;
import com.marketmaker.interface_adapter.PositionsPresenter;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.price_feed.FinnhubPriceFeed;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.price_feed.ReplayPriceFeed;
import com.marketmaker.use_case.search_ticker.TickerDataAccessInterface;
import com.marketmaker.use_case.view_candlestick_chart.Resolution;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartInteractor;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartRequestModel;
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
    private static final double STARTING_CASH = 10_000.0;
    private static final String CHART_TICKER = "AAPL";
    private static final List<String> WATCHLIST = List.of("AAPL", "MSFT", "NVDA");
    // Slower than the price feed's own cache, so an idle screen re-polls rather than
    // re-serving the same quote over and over.
    private static final int REFRESH_INTERVAL_MS = 10_000;

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
        if (accountDAO.get(ACCOUNT_ID) == null) {
            // First launch: open the paper account with its fixed starting balance.
            accountDAO.save(new Account(ACCOUNT_ID, STARTING_CASH));
        }

        SwingUtilities.invokeLater(() -> {
            ViewModel<ViewPortfolioSummaryResponseModel> summary = new ViewModel<>();
            ViewModel<ViewPositionsResponseModel> positions = new ViewModel<>();
            ViewModel<ViewOrderHistoryResponseModel> orderHistory = new ViewModel<>();
            ViewModel<ViewCandlestickChartResponseModel> chart = new ViewModel<>();
            ViewModel<List<Quote>> watchlist = new ViewModel<>();

            new DashboardFrame(summary, positions, orderHistory, chart, watchlist).setVisible(true);

            ViewPortfolioSummaryInteractor summaryInteractor = new ViewPortfolioSummaryInteractor(
                    accountDAO, quotes, new PortfolioSummaryPresenter(summary));
            ViewPositionsInteractor positionsInteractor = new ViewPositionsInteractor(
                    accountDAO, quotes, new PositionsPresenter(positions));
            ViewOrderHistoryInteractor historyInteractor = new ViewOrderHistoryInteractor(
                    accountDAO, new OrderHistoryPresenter(orderHistory));
            // ponytail: the chart still reads generated candles — swap in a real historical
            // source once the team settles which provider serves them.
            ViewCandlestickChartInteractor chartInteractor = new ViewCandlestickChartInteractor(
                    new StubHistoricalDataAccessObject(), new CandlestickChartPresenter(chart));

            Runnable refresh = () -> WORKER.execute(() -> {
                summaryInteractor.execute(new ViewPortfolioSummaryRequestModel(ACCOUNT_ID));
                positionsInteractor.execute(new ViewPositionsRequestModel(ACCOUNT_ID));
                historyInteractor.execute(new ViewOrderHistoryRequestModel(ACCOUNT_ID));
                watchlist.setState(quote(priceFeed, WATCHLIST));
            });

            chartInteractor.execute(new ViewCandlestickChartRequestModel(CHART_TICKER, Resolution.FIVE_MINUTE));
            refresh.run();
            new Timer(REFRESH_INTERVAL_MS, event -> refresh.run()).start();
        });
    }

    /** One quote per watched ticker; a symbol the feed can't price is left off the table. */
    private static List<Quote> quote(PriceFeed priceFeed, List<String> tickers) {
        List<Quote> quotes = new ArrayList<>();
        for (String ticker : tickers) {
            try {
                quotes.add(priceFeed.getQuote(ticker));
            } catch (RuntimeException exception) {
                // One bad ticker shouldn't cost the others their prices, and the next tick retries.
                LOGGER.warning(() -> "No quote for " + ticker + ": " + exception.getMessage());
            }
        }
        return quotes;
    }

    /**
     * Live quotes when a Finnhub key is configured, the replay feed when one isn't, so the
     * app still runs for anyone who hasn't set up a key.
     */
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
