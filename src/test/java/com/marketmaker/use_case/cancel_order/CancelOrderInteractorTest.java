package com.marketmaker.use_case.cancel_order;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CancelOrderInteractorTest {

    private static final class FakePresenter implements CancelOrderOutputBoundary {
        CancelOrderResponseModel successResponse;
        String failureMessage;

        @Override
        public void presentSuccess(CancelOrderResponseModel response) {
            this.successResponse = response;
        }

        @Override
        public void presentFailure(String errorMessage) {
            this.failureMessage = errorMessage;
        }
    }

    private static Order restingOrder(String id) {
        return new Order(id, "AAPL", Order.Side.BUY, Order.Type.LIMIT, 10, 150.0, Instant.EPOCH);
    }

    @Test
    void cancelsAPendingOrderAndSaves() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("demo", 10_000.0);
        account.addOrder(restingOrder("order-1"));
        accountDAO.save(account);
        FakePresenter presenter = new FakePresenter();

        new CancelOrderInteractor(accountDAO, presenter)
                .execute(new CancelOrderRequestModel("demo", "order-1"));

        assertEquals("order-1", presenter.successResponse.getOrderId());
        assertNull(presenter.failureMessage);
        // Read it back through the DAO: cancelling must outlive the interactor, not just the entity.
        assertEquals(Order.Status.CANCELED,
                accountDAO.get("demo").getPlacedOrders().get(0).getStatus());
    }

    @Test
    void refusesToCancelAnOrderThatAlreadyFilled() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        Account account = new Account("demo", 10_000.0);
        Order filled = restingOrder("order-1");
        filled.fill(150.0, Instant.EPOCH);
        account.addOrder(filled);
        accountDAO.save(account);
        FakePresenter presenter = new FakePresenter();

        new CancelOrderInteractor(accountDAO, presenter)
                .execute(new CancelOrderRequestModel("demo", "order-1"));

        assertTrue(presenter.failureMessage.contains("Only pending orders"));
        assertEquals(Order.Status.FILLED, account.getPlacedOrders().get(0).getStatus());
    }

    @Test
    void reportsAnOrderIdThatIsNotThere() {
        InMemoryAccountDAO accountDAO = new InMemoryAccountDAO();
        accountDAO.save(new Account("demo", 10_000.0));
        FakePresenter presenter = new FakePresenter();

        new CancelOrderInteractor(accountDAO, presenter)
                .execute(new CancelOrderRequestModel("demo", "ghost"));

        assertTrue(presenter.failureMessage.contains("Order not found"));
    }
}
