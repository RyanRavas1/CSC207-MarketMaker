package com.marketmaker.use_case.cancel_order;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class CancelOrderInteractorTest {
    private static class Presenter implements CancelOrderOutputBoundary {
        CancelOrderResponseModel success; String failure;
        public void presentSuccess(CancelOrderResponseModel response) { success = response; }
        public void presentFailure(String errorMessage) { failure = errorMessage; }
    }
    private static Order order(String id) { return new Order(id, "AAPL", Order.Side.BUY, Order.Type.LIMIT, 1, 10d, Instant.EPOCH); }

    @Test void cancelsPendingOrder() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 10); Order order = order("o1"); account.addOrder(order); dao.save(account);
        Presenter presenter = new Presenter();
        new CancelOrderInteractor(dao, presenter).execute(new CancelOrderRequestModel("ada", "o1"));
        assertEquals(Order.Status.CANCELED, order.getStatus());
        assertEquals("o1", presenter.success.getOrderId()); assertNull(presenter.failure);
    }

    @Test void rejectsMissingAccountMissingOrderAndNonPendingOrder() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Presenter presenter = new Presenter(); CancelOrderInteractor interactor = new CancelOrderInteractor(dao, presenter);
        interactor.execute(new CancelOrderRequestModel("none", "o1")); assertEquals("Account not found.", presenter.failure);
        Account account = new Account("ada", 10); Order filled = order("filled"); filled.fill(10, Instant.EPOCH); account.addOrder(filled); dao.save(account);
        interactor.execute(new CancelOrderRequestModel("ada", "unknown")); assertEquals("Order not found.", presenter.failure);
        interactor.execute(new CancelOrderRequestModel("ada", "filled")); assertEquals("Only pending orders can be cancelled.", presenter.failure);
    }
}
