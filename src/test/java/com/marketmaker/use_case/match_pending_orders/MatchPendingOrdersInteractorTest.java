package com.marketmaker.use_case.match_pending_orders;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchPendingOrdersInteractorTest {
    private static class Presenter implements MatchPendingOrdersOutputBoundary {
        final List<MatchPendingOrdersResponseModel> fills = new ArrayList<>();
        public void presentFill(MatchPendingOrdersResponseModel response) { fills.add(response); }
    }
    private static Order pending(String id, Order.Side side, Order.Type type, int quantity, double trigger) {
        return new Order(id, "AAPL", side, type, quantity, trigger, Instant.EPOCH);
    }

    @Test void fillsTriggeredBuyLimitAndUpdatesAccount() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 100); Order order = pending("buy", Order.Side.BUY, Order.Type.LIMIT, 2, 20); account.addOrder(order); dao.save(account);
        Presenter presenter = new Presenter();
        new MatchPendingOrdersInteractor(dao, presenter).execute(new MatchPendingOrdersRequestModel("ada", new Quote("AAPL", 15, Instant.EPOCH)));
        assertEquals(Order.Status.FILLED, order.getStatus()); assertEquals(70, account.getUserBalance());
        assertEquals(2, account.getHoldings().get(0).getShares()); assertEquals(1, account.getTradeLog().size());
        MatchPendingOrdersResponseModel fill = presenter.fills.get(0);
        assertEquals("buy", fill.getOrderId());
        assertEquals("AAPL", fill.getTicker());
        assertEquals(15.0, fill.getFillPrice());
        assertEquals(70.0, fill.getNewCashBalance());
        assertEquals(2, fill.getNewShareCount());
    }

    @Test void fillsTriggeredSellAndBooksProfit() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 0); account.addPosition(new Position("AAPL", 3, 10)); Order order = pending("sell", Order.Side.SELL, Order.Type.LIMIT, 3, 12); account.addOrder(order); dao.save(account);
        Presenter presenter = new Presenter();
        new MatchPendingOrdersInteractor(dao, presenter).execute(new MatchPendingOrdersRequestModel("ada", new Quote("AAPL", 13, Instant.EPOCH)));
        assertEquals(39, account.getUserBalance()); assertTrue(account.getHoldings().isEmpty());
        assertEquals(9, account.getTradeLog().get(0).getRealizedPnL()); assertEquals(0, presenter.fills.get(0).getNewShareCount());
    }

    @Test void ignoresUntriggeredOrUnaffordableOrdersAndMissingAccounts() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 10);
        Order untriggered = pending("untriggered", Order.Side.BUY, Order.Type.LIMIT, 1, 5);
        Order unaffordable = pending("expensive", Order.Side.BUY, Order.Type.LIMIT, 2, 10);
        account.addOrder(untriggered); account.addOrder(unaffordable); dao.save(account);
        Presenter presenter = new Presenter(); MatchPendingOrdersInteractor interactor = new MatchPendingOrdersInteractor(dao, presenter);
        interactor.execute(new MatchPendingOrdersRequestModel("ada", new Quote("AAPL", 10, Instant.EPOCH)));
        interactor.execute(new MatchPendingOrdersRequestModel("none", new Quote("AAPL", 1, Instant.EPOCH)));
        assertEquals(Order.Status.PENDING, untriggered.getStatus()); assertEquals(Order.Status.PENDING, unaffordable.getStatus()); assertTrue(presenter.fills.isEmpty());
    }

    @Test void stopLossTriggersOnlyAtOrBelowStopPrice() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 0); account.addPosition(new Position("AAPL", 1, 20)); Order stop = pending("stop", Order.Side.SELL, Order.Type.STOP_LOSS, 1, 15); account.addOrder(stop); dao.save(account);
        Presenter presenter = new Presenter(); MatchPendingOrdersInteractor interactor = new MatchPendingOrdersInteractor(dao, presenter);
        interactor.execute(new MatchPendingOrdersRequestModel("ada", new Quote("AAPL", 16, Instant.EPOCH))); assertEquals(Order.Status.PENDING, stop.getStatus());
        interactor.execute(new MatchPendingOrdersRequestModel("ada", new Quote("AAPL", 14, Instant.EPOCH))); assertEquals(Order.Status.FILLED, stop.getStatus());
    }

    @Test void skipsOrdersThatCannotBeSoldAndHandlesIrrelevantOrderTypes() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 0);
        Order tooManyShares = pending("too-many", Order.Side.SELL, Order.Type.LIMIT, 2, 10);
        Order market = pending("market", Order.Side.BUY, Order.Type.MARKET, 1, 10);
        Order otherTicker = new Order("other", "MSFT", Order.Side.BUY, Order.Type.LIMIT, 1, 10d, Instant.EPOCH);
        account.addOrder(tooManyShares); account.addOrder(market); account.addOrder(otherTicker); dao.save(account);
        Presenter presenter = new Presenter();
        new MatchPendingOrdersInteractor(dao, presenter).execute(new MatchPendingOrdersRequestModel("ada", new Quote("AAPL", 10, Instant.EPOCH)));
        assertAll(() -> assertEquals(Order.Status.PENDING, tooManyShares.getStatus()), () -> assertEquals(Order.Status.PENDING, market.getStatus()), () -> assertEquals(Order.Status.PENDING, otherTicker.getStatus()), () -> assertTrue(presenter.fills.isEmpty()));
    }

    @Test void partialSellAndExistingBuyRetainTheExpectedPosition() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO(); Account account = new Account("ada", 100); account.addPosition(new Position("AAPL", 3, 10));
        Order buy = pending("buy", Order.Side.BUY, Order.Type.LIMIT, 1, 20); Order sell = pending("sell", Order.Side.SELL, Order.Type.LIMIT, 1, 10); account.addOrder(buy); account.addOrder(sell); dao.save(account);
        new MatchPendingOrdersInteractor(dao, new Presenter()).execute(new MatchPendingOrdersRequestModel("ada", new Quote("AAPL", 10, Instant.EPOCH)));
        assertEquals(3, account.getHoldings().get(0).getShares()); assertEquals(10, account.getHoldings().get(0).getAveragePrice());
    }
}
