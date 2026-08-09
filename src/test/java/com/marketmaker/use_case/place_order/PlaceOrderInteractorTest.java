package com.marketmaker.use_case.place_order;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.price_feed.PriceFeedException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PlaceOrderInteractorTest {
    private static class Presenter implements PlaceOrderOutputBoundary {
        PlaceOrderResponseModel success; String failure;
        public void presentSuccess(PlaceOrderResponseModel response) { success = response; }
        public void presentFailure(String errorMessage) { failure = errorMessage; }
    }

    private static PriceFeed fixedPrice(double price) {
        return ticker -> new Quote(ticker, price, Instant.EPOCH);
    }

    @Test void buyFillsOrderUpdatesCashPositionAndTradeLog() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO();
        Account account = new Account("ada", 1_000); dao.save(account);
        Presenter presenter = new Presenter();

        new PlaceOrderInteractor(dao, fixedPrice(25), presenter)
                .execute(new PlaceOrderRequestModel("ada", "AAPL", Order.Side.BUY, 4));

        assertNull(presenter.failure);
        assertEquals("AAPL", presenter.success.getTicker());
        assertEquals(4, presenter.success.getQuantity());
        assertEquals(25.0, presenter.success.getFillPrice());
        assertEquals(900.0, presenter.success.getNewCashBalance());
        assertEquals(4, presenter.success.getNewShareCount());
        assertEquals(900, account.getUserBalance());
        assertEquals(4, account.getHoldings().get(0).getShares());
        assertEquals(25, account.getHoldings().get(0).getAveragePrice());
        assertEquals(Order.Status.FILLED, account.getPlacedOrders().get(0).getStatus());
        assertNull(account.getTradeLog().get(0).getRealizedPnL());
    }

    @Test void buyMergesPositionAtWeightedAveragePrice() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO();
        Account account = new Account("ada", 1_000); account.addPosition(new Position("AAPL", 2, 10)); dao.save(account);
        Presenter presenter = new Presenter();

        new PlaceOrderInteractor(dao, fixedPrice(20), presenter)
                .execute(new PlaceOrderRequestModel("ada", "AAPL", Order.Side.BUY, 3));

        Position position = account.getHoldings().get(0);
        assertEquals(5, position.getShares());
        assertEquals(16, position.getAveragePrice());
    }

    @Test void sellClosesPositionAndRecordsRealizedProfit() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO();
        Account account = new Account("ada", 100); account.addPosition(new Position("AAPL", 2, 10)); dao.save(account);
        Presenter presenter = new Presenter();

        new PlaceOrderInteractor(dao, fixedPrice(15), presenter)
                .execute(new PlaceOrderRequestModel("ada", "AAPL", Order.Side.SELL, 2));

        assertEquals(130, account.getUserBalance());
        assertTrue(account.getHoldings().isEmpty());
        assertEquals(10, account.getTradeLog().get(0).getRealizedPnL());
        assertEquals(0, presenter.success.getNewShareCount());
    }

    @Test void rejectsInvalidOrUnfillableOrdersWithoutMutatingAccount() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO();
        Account account = new Account("ada", 10); dao.save(account);
        Presenter presenter = new Presenter();
        PlaceOrderInteractor interactor = new PlaceOrderInteractor(dao, fixedPrice(10), presenter);

        interactor.execute(new PlaceOrderRequestModel("ada", "AAPL", Order.Side.BUY, 0));
        assertEquals("Quantity must be positive.", presenter.failure);
        interactor.execute(new PlaceOrderRequestModel("ada", "AAPL", Order.Side.BUY, 2));
        assertEquals("Insufficient buying power.", presenter.failure);
        interactor.execute(new PlaceOrderRequestModel("ada", "AAPL", Order.Side.SELL, 1));
        assertEquals("Not enough shares to sell.", presenter.failure);
        assertEquals(10, account.getUserBalance());
        assertTrue(account.getPlacedOrders().isEmpty());
    }

    @Test void reportsMissingAccountAndFeedFailure() {
        Presenter missing = new Presenter();
        new PlaceOrderInteractor(new InMemoryAccountDAO(), fixedPrice(1), missing)
                .execute(new PlaceOrderRequestModel("none", "AAPL", Order.Side.BUY, 1));
        assertEquals("Account not found.", missing.failure);

        InMemoryAccountDAO dao = new InMemoryAccountDAO(); dao.save(new Account("ada", 100));
        Presenter outage = new Presenter();
        new PlaceOrderInteractor(dao, ticker -> { throw new PriceFeedException("feed unavailable"); }, outage)
                .execute(new PlaceOrderRequestModel("ada", "AAPL", Order.Side.BUY, 1));
        assertEquals("feed unavailable", outage.failure);
    }

    @Test void sellPartiallyRetainsPositionAtOriginalAveragePrice() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 0); account.addPosition(new Position("AAPL", 3, 10)); dao.save(account);
        Presenter presenter = new Presenter();
        new PlaceOrderInteractor(dao, fixedPrice(12), presenter).execute(new PlaceOrderRequestModel("ada", "AAPL", Order.Side.SELL, 1));
        assertEquals(2, account.getHoldings().get(0).getShares()); assertEquals(10, account.getHoldings().get(0).getAveragePrice()); assertEquals(2, presenter.success.getNewShareCount());
    }
}
