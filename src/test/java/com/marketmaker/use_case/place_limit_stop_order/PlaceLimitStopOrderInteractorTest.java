package com.marketmaker.use_case.place_limit_stop_order;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlaceLimitStopOrderInteractorTest {
    private static class Presenter implements PlaceLimitStopOrderOutputBoundary {
        PlaceLimitStopOrderResponseModel success; String failure;
        public void presentSuccess(PlaceLimitStopOrderResponseModel response) { success = response; }
        public void presentFailure(String errorMessage) { failure = errorMessage; }
    }
    private static PlaceLimitStopOrderRequestModel request(Order.Side side, Order.Type type, int quantity, double price) {
        return new PlaceLimitStopOrderRequestModel("ada", "AAPL", side, type, quantity, price);
    }

    @Test void createsPendingLimitOrderForExistingOrNewAccount() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); dao.save(new Account("ada", 1_000)); Presenter presenter = new Presenter();
        new PlaceLimitStopOrderInteractor(dao, presenter).execute(request(Order.Side.BUY, Order.Type.LIMIT, 4, 100));
        Order order = dao.get("ada").getPlacedOrders().get(0);
        assertEquals(Order.Status.PENDING, order.getStatus()); assertEquals(100, order.getLimitOrStopPrice());
        assertEquals(Order.Type.LIMIT, presenter.success.getType());
        assertEquals("AAPL", presenter.success.getTicker());
        assertEquals(100.0, presenter.success.getTriggerPrice());
        assertNotNull(presenter.success.getOrderId());

        PlaceLimitStopOrderRequestModel req = request(Order.Side.BUY, Order.Type.LIMIT, 4, 100);
        assertEquals("ada", req.getAccountId());
        assertEquals("AAPL", req.getTicker());
        assertEquals(Order.Side.BUY, req.getSide());
        assertEquals(Order.Type.LIMIT, req.getType());
        assertEquals(4, req.getQuantity());
        assertEquals(100.0, req.getTriggerPrice());

        InMemoryAccountDAO newDao = new InMemoryAccountDAO(); Presenter newPresenter = new Presenter();
        new PlaceLimitStopOrderInteractor(newDao, newPresenter).execute(request(Order.Side.BUY, Order.Type.LIMIT, 1, 10));
        assertEquals(100_000, newDao.get("ada").getUserBalance());
    }

    @Test void validatesTypeQuantityPriceBuyingPowerAndShares() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 10); dao.save(account); Presenter p = new Presenter(); PlaceLimitStopOrderInteractor i = new PlaceLimitStopOrderInteractor(dao, p);
        i.execute(request(Order.Side.BUY, Order.Type.MARKET, 1, 1)); assertEquals("Use PlaceOrder for market orders.", p.failure);
        i.execute(request(Order.Side.BUY, Order.Type.STOP_LOSS, 1, 1)); assertEquals("Stop-loss orders can only be sell orders.", p.failure);
        i.execute(request(Order.Side.BUY, Order.Type.LIMIT, 0, 1)); assertEquals("Quantity and trigger price must be positive.", p.failure);
        i.execute(request(Order.Side.BUY, Order.Type.LIMIT, 1, 0)); assertEquals("Quantity and trigger price must be positive.", p.failure);
        i.execute(request(Order.Side.BUY, Order.Type.LIMIT, 2, 10)); assertEquals("Insufficient buying power.", p.failure);
        i.execute(request(Order.Side.SELL, Order.Type.LIMIT, 1, 1)); assertEquals("Not enough shares to sell.", p.failure);
        account.addPosition(new Position("AAPL", 1, 5)); i.execute(request(Order.Side.SELL, Order.Type.STOP_LOSS, 1, 4));
        assertNotNull(p.success);
    }
}
