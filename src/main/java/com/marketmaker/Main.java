package com.marketmaker;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.marketmaker.config.EnvLoader;
import com.marketmaker.config.exceptions.MissingEnvironmentVariableException;
import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.data_access.FileAccountDataAccessObject;
import com.marketmaker.data_access.FinnhubApiClient;
import com.marketmaker.entities.Account;
import com.marketmaker.interface_adapter.order_history.OrderHistoryViewModel;
import com.marketmaker.interface_adapter.order_history.ViewOrderHistoryController;
import com.marketmaker.interface_adapter.order_history.ViewOrderHistoryPresenter;
import com.marketmaker.interface_adapter.trading.CancelOrderController;
import com.marketmaker.interface_adapter.trading.CancelOrderPresenter;
import com.marketmaker.interface_adapter.trading.MatchPendingOrdersController;
import com.marketmaker.interface_adapter.trading.MatchPendingOrdersPresenter;
import com.marketmaker.interface_adapter.trading.PlaceLimitStopOrderController;
import com.marketmaker.interface_adapter.trading.PlaceLimitStopOrderPresenter;
import com.marketmaker.interface_adapter.trading.PlaceOrderController;
import com.marketmaker.interface_adapter.trading.PlaceOrderPresenter;
import com.marketmaker.interface_adapter.trading.TradingViewModel;
import com.marketmaker.interface_adapter.user_profile.ViewProfileController;
import com.marketmaker.interface_adapter.user_profile.ViewProfilePresenter;
import com.marketmaker.interface_adapter.user_profile.ViewProfileViewModel;
import com.marketmaker.interface_adapter.watchlist.WatchlistController;
import com.marketmaker.interface_adapter.watchlist.WatchlistPresenter;
import com.marketmaker.interface_adapter.watchlist.WatchlistViewModel;
import com.marketmaker.price_feed.FinnhubPriceFeed;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.price_feed.PriceFeedException;
import com.marketmaker.price_feed.ReplayPriceFeed;
import com.marketmaker.use_case.cancel_order.CancelOrderInteractor;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersInteractor;
import com.marketmaker.use_case.order_history.ViewOrderHistoryInteractor;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderInteractor;
import com.marketmaker.use_case.place_order.PlaceOrderInteractor;
import com.marketmaker.use_case.user_profile.ViewProfileInteractor;
import com.marketmaker.use_case.watchlist.WatchlistInteractor;
import com.marketmaker.view.AccountSummaryView;
import com.marketmaker.view.Background;
import com.marketmaker.view.MainWindow;
import com.marketmaker.view.OrderHistoryView;
import com.marketmaker.view.ProfileView;
import com.marketmaker.view.TradingView;
import com.marketmaker.view.WatchlistView;

/** Builds the object graph and drops each panel into its slot in the app shell. */
public class Main {
    private static final int MATCH_INTERVAL_MS = 3000;
    private static final String DATA_FILE = "data/accounts.json";
    private static final String DEMO_ACCOUNT = "demo";
    private static final double STARTING_CASH = 10000.0;
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        PriceFeed priceFeed = createPriceFeed();

        // Cash, holdings and the whole trade log live in a JSON file next to the app, so a
        // session picks up exactly where the last one stopped.
        AccountDAO accountDAO = new FileAccountDataAccessObject(Path.of(DATA_FILE));
        if (accountDAO.get(DEMO_ACCOUNT) == null) {
            // First launch: open the paper account with its fixed starting balance.
            accountDAO.save(new Account(DEMO_ACCOUNT, STARTING_CASH));
        }

        WatchlistViewModel watchlistViewModel = new WatchlistViewModel();
        WatchlistController watchlistController = new WatchlistController(
                new WatchlistInteractor(priceFeed, new WatchlistPresenter(watchlistViewModel)));

        ViewProfileViewModel profileViewModel = new ViewProfileViewModel();
        ViewProfileController profileController = new ViewProfileController(
                new ViewProfileInteractor(accountDAO, priceFeed, new ViewProfilePresenter(profileViewModel)));

        // All four order use cases write to one trading screen, so they share a view model.
        TradingViewModel tradingViewModel = new TradingViewModel();
        PlaceOrderController placeOrderController = new PlaceOrderController(
                new PlaceOrderInteractor(accountDAO, priceFeed, new PlaceOrderPresenter(tradingViewModel)));
        PlaceLimitStopOrderController limitStopController = new PlaceLimitStopOrderController(
                new PlaceLimitStopOrderInteractor(accountDAO, new PlaceLimitStopOrderPresenter(tradingViewModel)));
        CancelOrderController cancelController = new CancelOrderController(
                new CancelOrderInteractor(accountDAO, new CancelOrderPresenter(tradingViewModel)));
        MatchPendingOrdersController matchController = new MatchPendingOrdersController(
                new MatchPendingOrdersInteractor(accountDAO, new MatchPendingOrdersPresenter(tradingViewModel)));

        OrderHistoryViewModel orderHistoryViewModel = new OrderHistoryViewModel();
        ViewOrderHistoryController orderHistoryController = new ViewOrderHistoryController(
                new ViewOrderHistoryInteractor(accountDAO, new ViewOrderHistoryPresenter(orderHistoryViewModel)));

        SwingUtilities.invokeLater(() -> {
            WatchlistView watchlistView = new WatchlistView(watchlistController, watchlistViewModel);
            ProfileView profileView = new ProfileView(profileController, profileViewModel);
            TradingView tradingView = new TradingView(
                    placeOrderController, limitStopController, cancelController, tradingViewModel);
            OrderHistoryView orderHistoryView = new OrderHistoryView(orderHistoryController, orderHistoryViewModel);
            AccountSummaryView accountSummaryView = new AccountSummaryView(profileViewModel);

            // Watchlist to the WEST, the order ticket and holdings down the EAST column, history
            // along the SOUTH, the account summary across the top. The chart slot keeps its
            // placeholder.
            new MainWindow(watchlistView, tradingView, profileView, orderHistoryView, accountSummaryView)
                    .setVisible(true);

            // A fill changes cash, holdings and the order log, which the profile and history
            // screens read. Nothing else tells them, so re-run both use cases whenever the
            // trading screen updates. Fires from the background worker now, and the views read
            // Swing fields, so bounce back to the EDT before asking any of them anything.
            tradingViewModel.addPropertyChangeListener(event -> SwingUtilities.invokeLater(() -> {
                profileView.refreshIfShowing(tradingView.getAccountId());
                orderHistoryView.refreshFor(tradingView.getAccountId());
            }));

            watchlistView.start(List.of("AAPL", "MSFT"));
            profileView.start(DEMO_ACCOUNT);
            orderHistoryView.start(DEMO_ACCOUNT);
            startMatchDriver(priceFeed, matchController, tradingViewModel, tradingView);
        });
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
            return new ReplayPriceFeed(startingPrices);
        }
    }

    /**
     * Nothing pushes prices at us, so poll the feed for every ticker that still has a
     * pending order and hand each quote to the matcher.
     * ponytail: a Swing timer because the feed is pull-only; replace with the Finnhub
     * websocket callback once that adapter exists.
     */
    private static void startMatchDriver(PriceFeed priceFeed, MatchPendingOrdersController matchController,
                                         TradingViewModel tradingViewModel, TradingView tradingView) {
        new Timer(MATCH_INTERVAL_MS, event -> {
            // Read the screen on the EDT, then do the quoting on the worker: each quote is an
            // HTTP call, and this timer fires often enough to freeze the window otherwise.
            Set<String> tickers = new LinkedHashSet<>();
            for (String[] pendingOrder : tradingViewModel.getState().getPendingOrders()) {
                tickers.add(pendingOrder[1]);
            }
            String accountId = tradingView.getAccountId();
            Background.run(() -> {
                for (String ticker : tickers) {
                    try {
                        matchController.onQuote(accountId, ticker, priceFeed.getQuote(ticker).getPrice());
                    } catch (PriceFeedException exception) {
                        // One bad ticker shouldn't stop the others, and the next tick retries.
                        LOGGER.warning(() -> "Skipping match for " + ticker + ": " + exception.getMessage());
                    }
                }
            });
        }).start();
    }
}
