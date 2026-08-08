package com.marketmaker.use_case.view_order_history;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Trade;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViewOrderHistoryInteractorTest {

    private static class FakePresenter implements ViewOrderHistoryOutputBoundary {
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

        interactor.execute(new ViewOrderHistoryRequestModel("wayne"));

        assertEquals(1, presenter.successResponse.getOrders().size());
        assertEquals(Order.Status.FILLED, presenter.successResponse.getOrders().get(0).getStatus());
        assertEquals(1, presenter.successResponse.getTrades().size());
        assertEquals("AAPL", presenter.successResponse.getTrades().get(0).getTicker());
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
