package com.marketmaker.use_case.view_order_history;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Trade;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewOrderHistoryInteractorTest {

    private static final class FakePresenter implements ViewOrderHistoryOutputBoundary {
        ViewOrderHistoryResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(ViewOrderHistoryResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    @Test
    void listsOrdersAndTradesForAccount() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("wayne", 100_000.0);
        Order order = new Order("o1", "AAPL", Order.Side.BUY, Order.Type.MARKET, 10, null, Instant.EPOCH);
        order.fill(232.50, Instant.EPOCH);
        account.addOrder(order);
        account.addTrade(new Trade("t1", "o1", "AAPL", Order.Side.BUY, 10, 232.50, Instant.EPOCH, null));
        accountDAO.save(account);
        FakePresenter presenter = new FakePresenter();
        ViewOrderHistoryInteractor interactor = new ViewOrderHistoryInteractor(accountDAO, presenter);

        Order pendingOrder = new Order("o2", "MSFT", Order.Side.BUY, Order.Type.LIMIT, 5, 400.0, Instant.EPOCH);
        account.addOrder(pendingOrder);

        ViewOrderHistoryRequestModel req = new ViewOrderHistoryRequestModel("wayne");
        assertEquals("wayne", req.getAccountId());

        interactor.execute(req);

        assertEquals(2, presenter.successResponse.getOrders().size());
        OrderHistoryRow row1 = presenter.successResponse.getOrders().get(0);
        assertEquals("o1", row1.getOrderId());
        assertEquals("AAPL", row1.getTicker());
        assertEquals(Order.Side.BUY, row1.getSide());
        assertEquals(Order.Type.MARKET, row1.getType());
        assertEquals(10, row1.getQuantity());
        assertNull(row1.getLimitOrStopPrice());
        assertEquals(Order.Status.FILLED, row1.getStatus());
        assertEquals(Instant.EPOCH, row1.getTimestamp());

        OrderHistoryRow row2 = presenter.successResponse.getOrders().get(1);
        assertEquals("o2", row2.getOrderId());
        assertEquals(Order.Status.PENDING, row2.getStatus());
        assertEquals(Instant.EPOCH, row2.getTimestamp());

        assertEquals(1, presenter.successResponse.getTrades().size());
        TradeHistoryRow tradeRow = presenter.successResponse.getTrades().get(0);
        assertEquals("t1", tradeRow.getTradeId());
        assertEquals("AAPL", tradeRow.getTicker());
        assertEquals(Order.Side.BUY, tradeRow.getSide());
        assertEquals(10, tradeRow.getQuantity());
        assertEquals(232.50, tradeRow.getPrice());
        assertEquals(Instant.EPOCH, tradeRow.getTimestamp());
        assertNull(tradeRow.getRealizedPnL());
    }

    @Test
    void reportsFailureWhenAccountMissing() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        FakePresenter presenter = new FakePresenter();
        ViewOrderHistoryInteractor interactor = new ViewOrderHistoryInteractor(accountDAO, presenter);

        interactor.execute(new ViewOrderHistoryRequestModel("ghost"));

        assertTrue(presenter.failureMessage.contains("Account not found"));
    }
}
