package com.marketmaker.interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.interface_adapter.trading.CancelOrderController;
import com.marketmaker.interface_adapter.trading.CancelOrderPresenter;
import com.marketmaker.interface_adapter.trading.MatchPendingOrdersController;
import com.marketmaker.interface_adapter.trading.MatchPendingOrdersPresenter;
import com.marketmaker.interface_adapter.trading.PlaceLimitStopOrderController;
import com.marketmaker.interface_adapter.trading.PlaceLimitStopOrderPresenter;
import com.marketmaker.interface_adapter.trading.PlaceOrderController;
import com.marketmaker.interface_adapter.trading.PlaceOrderPresenter;
import com.marketmaker.interface_adapter.trading.TradingViewModel;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.use_case.cancel_order.CancelOrderInteractor;
import com.marketmaker.use_case.match_pending_orders.MatchPendingOrdersInteractor;
import com.marketmaker.use_case.place_limit_stop_order.PlaceLimitStopOrderInteractor;
import com.marketmaker.use_case.place_order.PlaceOrderInteractor;

/** The four order use cases, each driven controller -> interactor -> presenter -> view model. */
class TradingAdapterTest {

    // Every ticker quotes at $200 so the expected strings are exact.
    private static final PriceFeed FIXED_FEED = ticker -> new Quote(ticker, 200.0, Instant.EPOCH);

    private AccountDAO accountDAO;
    private TradingViewModel viewModel;
    private PlaceOrderController placeOrder;
    private PlaceLimitStopOrderController limitStop;
    private CancelOrderController cancel;
    private MatchPendingOrdersController match;

    @BeforeEach
    void setUp() {
        accountDAO = new InMemoryAccountDAO();
        Account account = new Account("demo", 10000.0);
        account.addPosition(new Position("AAPL", 10, 150.0));
        accountDAO.save(account);

        viewModel = new TradingViewModel();
        placeOrder = new PlaceOrderController(
                new PlaceOrderInteractor(accountDAO, FIXED_FEED, new PlaceOrderPresenter(viewModel)));
        limitStop = new PlaceLimitStopOrderController(
                new PlaceLimitStopOrderInteractor(accountDAO, new PlaceLimitStopOrderPresenter(viewModel)));
        cancel = new CancelOrderController(
                new CancelOrderInteractor(accountDAO, new CancelOrderPresenter(viewModel)));
        match = new MatchPendingOrdersController(
                new MatchPendingOrdersInteractor(accountDAO, new MatchPendingOrdersPresenter(viewModel)));
    }

    @Test
    void marketBuyReachesViewModel() {
        placeOrder.place("demo", "AAPL", Order.Side.BUY, 5);

        assertEquals("Filled 5 AAPL @ $200.00 — you now hold 15 shares.", viewModel.getState().getMessage());
        assertEquals("$9000.00", viewModel.getState().getCashBalance()); // 10000 - 5 * 200
    }

    @Test
    void rejectedOrderShowsInteractorMessage() {
        placeOrder.place("demo", "AAPL", Order.Side.SELL, 999);

        assertEquals("Not enough shares to sell.", viewModel.getState().getMessage());
    }

    @Test
    void limitOrderAppearsAsPendingRow() {
        limitStop.place("demo", "AAPL", Order.Side.BUY, Order.Type.LIMIT, 2, 180.0);

        assertEquals(1, viewModel.getState().getPendingOrders().size());
        String[] row = viewModel.getState().getPendingOrders().get(0);
        assertEquals("AAPL", row[1]);
        assertEquals("LIMIT", row[2]);
        assertEquals("$180.00", row[3]);
    }

    @Test
    void cancellingClearsThePendingRow() {
        limitStop.place("demo", "AAPL", Order.Side.BUY, Order.Type.LIMIT, 2, 180.0);
        String orderId = viewModel.getState().getPendingOrders().get(0)[0];

        cancel.cancel("demo", orderId);

        assertEquals("Order cancelled.", viewModel.getState().getMessage());
        assertTrue(viewModel.getState().getPendingOrders().isEmpty());
    }

    @Test
    void matchingFillsThePendingRowAndClearsIt() {
        // Sell limit at $180 triggers on any price at or above it.
        limitStop.place("demo", "AAPL", Order.Side.SELL, Order.Type.LIMIT, 4, 180.0);

        match.onQuote("demo", "AAPL", 200.0);

        assertTrue(viewModel.getState().getPendingOrders().isEmpty());
        assertEquals("Pending AAPL order filled @ $200.00 — you now hold 6 shares.",
                viewModel.getState().getMessage());
        assertEquals("$10800.00", viewModel.getState().getCashBalance()); // 10000 + 4 * 200
    }

    @Test
    void negativeQuantityIsRejectedInsteadOfCreditingCash() {
        placeOrder.place("demo", "AAPL", Order.Side.BUY, -100);

        assertEquals("Quantity must be positive.", viewModel.getState().getMessage());
        assertEquals(10000.0, accountDAO.get("demo").getUserBalance(), 0.001);
    }

    @Test
    void unknownAccountIsRejectedInsteadOfSilentlyFunded() {
        placeOrder.place("ghost", "AAPL", Order.Side.BUY, 1);

        assertEquals("Account not found.", viewModel.getState().getMessage());
        assertNull(accountDAO.get("ghost"));
    }

    @Test
    void switchingAccountsDropsTheOtherAccountsPendingRows() {
        limitStop.place("demo", "AAPL", Order.Side.BUY, Order.Type.LIMIT, 2, 180.0);

        viewModel.clearPendingOrders();

        assertTrue(viewModel.getState().getPendingOrders().isEmpty());
    }

    @Test
    void quoteBelowTriggerLeavesTheOrderPending() {
        limitStop.place("demo", "AAPL", Order.Side.SELL, Order.Type.LIMIT, 4, 180.0);

        match.onQuote("demo", "AAPL", 150.0);

        assertEquals(1, viewModel.getState().getPendingOrders().size());
    }
}
