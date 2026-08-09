package com.marketmaker.interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.marketmaker.entities.Order;

import org.junit.jupiter.api.Test;

/** Money and enum formatting, which the use-case layer deliberately leaves to the view. */
class FormatTest {

    @Test
    void showsAbsentValuesAsADash() {
        assertEquals(Format.ABSENT, Format.money(null));
        assertEquals(Format.ABSENT, Format.signedMoney(null));
        assertEquals(Format.ABSENT, Format.percent(null));
        assertEquals(Format.ABSENT, Format.time(null));
    }

    @Test
    void groupsThousandsAndKeepsTwoDecimals() {
        assertEquals("1,234.50", Format.money(1234.5));
        assertEquals("0.00", Format.money(0.0));
    }

    @Test
    void alwaysSignsProfitAndLoss() {
        assertEquals("+152.30", Format.signedMoney(152.3));
        assertEquals("-78.50", Format.signedMoney(-78.5));
        assertEquals("+0.00", Format.signedMoney(0.0));
    }

    @Test
    void putsTheSignOutsideTheDollarSign() {
        assertEquals("+$342.18", Format.signedDollars(342.18));
        assertEquals("-$78.50", Format.signedDollars(-78.5));
    }

    @Test
    void abbreviatesVolumeTheWayATradingScreenDoes() {
        assertEquals("18.20M", Format.volume(18_200_000.0));
        assertEquals("1.42M", Format.volume(1_420_000.0));
        assertEquals("4.5K", Format.volume(4_500.0));
        assertEquals("820", Format.volume(820.0));
    }

    @Test
    void signsPercentagesTheSameWay() {
        assertEquals("+0.62%", Format.percent(0.62));
        assertEquals("-2.14%", Format.percent(-2.14));
    }

    @Test
    void readsMultiWordEnumConstantsAsWords() {
        assertEquals("Stop Loss", Format.enumLabel(Order.Type.STOP_LOSS));
        assertEquals("Filled", Format.enumLabel(Order.Status.FILLED));
    }
}
