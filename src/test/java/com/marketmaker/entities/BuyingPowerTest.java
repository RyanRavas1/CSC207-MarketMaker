package com.marketmaker.entities;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuyingPowerTest {

    private static Order restingBuy(String id, int quantity, double trigger) {
        return new Order(id, "AAPL", Order.Side.BUY, Order.Type.LIMIT, quantity, trigger, Instant.EPOCH);
    }

    @Test
    void countsRestingBuysAgainstAvailableCash() {
        Account account = new Account("demo", 10_000.0);
        account.addOrder(restingBuy("o1", 10, 300.0));

        assertEquals(3_000.0, account.reservedForOpenBuys());
        assertEquals(7_000.0, account.buyingPower());
        // Cash itself hasn't moved - the money leaves only when the order fills.
        assertEquals(10_000.0, account.getUserBalance());
    }

    @Test
    void releasesTheReservationWhenAnOrderStopsResting() {
        Account account = new Account("demo", 10_000.0);
        Order cancelled = restingBuy("o1", 10, 300.0);
        Order filled = restingBuy("o2", 5, 200.0);
        account.addOrder(cancelled);
        account.addOrder(filled);
        assertEquals(6_000.0, account.buyingPower());

        cancelled.cancel();
        filled.fill(200.0, Instant.EPOCH);

        assertEquals(0.0, account.reservedForOpenBuys());
        assertEquals(10_000.0, account.buyingPower());
    }

    @Test
    void ignoresRestingSellsAndNeverGoesNegative() {
        Account account = new Account("demo", 1_000.0);
        account.addOrder(new Order("o1", "AAPL", Order.Side.SELL, Order.Type.LIMIT, 10, 400.0,
                Instant.EPOCH));
        assertEquals(1_000.0, account.buyingPower());

        // A buy placed before the cash was spent elsewhere can exceed the balance; the floor
        // keeps the summary from reporting a negative amount of spendable money.
        account.addOrder(restingBuy("o2", 10, 500.0));
        assertEquals(0.0, account.buyingPower());
    }
}
