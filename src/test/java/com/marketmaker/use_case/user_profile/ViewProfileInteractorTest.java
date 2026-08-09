package com.marketmaker.use_case.user_profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.price_feed.PriceFeed;

class ViewProfileInteractorTest {

    @Test
    void valuesHoldingsAndEquityAtLivePrice() {
        InMemoryAccountDAO dao = new InMemoryAccountDAO();
        Account account = new Account("ericsson", 1_000.0);
        account.addPosition(new Position("AAPL", 10, 100.0)); // bought at 100
        dao.save(account);

        // Live price 150 → unrealized P&L = (150-100)*10 = 500, market value = 1500.
        PriceFeed feed = ticker -> new Quote(ticker, 150.0, Instant.now());
        ViewProfileResponseModel[] out = new ViewProfileResponseModel[1];
        ViewProfileOutputBoundary presenter = new ViewProfileOutputBoundary() {
            public void presentProfile(ViewProfileResponseModel r) { out[0] = r; }
            public void presentFailure(String e) { throw new AssertionError(e); }
        };

        ViewProfileRequestModel req = new ViewProfileRequestModel("ericsson");
        assertEquals("ericsson", req.getAccountId());

        new ViewProfileInteractor(dao, feed, presenter).execute(req);

        assertEquals("ericsson", out[0].getUserName());
        assertEquals(1_000.0, out[0].getCashBalance());
        assertEquals(2_500.0, out[0].getTotalEquity());

        ViewProfileResponseModel.Holding h = out[0].getHoldings().get(0);
        assertEquals("AAPL", h.getTicker());
        assertEquals(10, h.getShares());
        assertEquals(100.0, h.getAveragePrice());
        assertEquals(150.0, h.getCurrentPrice());
        assertEquals(1_500.0, h.getMarketValue());
        assertEquals(500.0, h.getUnrealizedPnL());
    }

    @Test
    void missingAccountFails() {
        boolean[] failed = {false};
        ViewProfileOutputBoundary presenter = new ViewProfileOutputBoundary() {
            public void presentProfile(ViewProfileResponseModel r) { }
            public void presentFailure(String e) { failed[0] = true; }
        };

        new ViewProfileInteractor(new InMemoryAccountDAO(),
                ticker -> new Quote(ticker, 1.0, Instant.now()), presenter)
                .execute(new ViewProfileRequestModel("nobody"));

        assertEquals(true, failed[0]);
    }
}
