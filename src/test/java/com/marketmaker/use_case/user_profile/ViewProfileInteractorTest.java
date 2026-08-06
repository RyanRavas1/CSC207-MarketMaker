package com.marketmaker.use_case.user_profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.marketmaker.data_access.InMemoryAccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Position;
import com.marketmaker.entities.Quote;
import com.marketmaker.price_feed.PriceFeed;

/** The interactor's arithmetic: live valuation, unrealized P/L, and total equity. */
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
            @Override
            public void presentProfile(ViewProfileResponseModel response) { out[0] = response; }

            @Override
            public void presentFailure(String errorMessage) { throw new AssertionError(errorMessage); }
        };

        new ViewProfileInteractor(dao, feed, presenter)
                .execute(new ViewProfileRequestModel("ericsson"));

        ViewProfileResponseModel.Holding holding = out[0].getHoldings().get(0);
        assertEquals(1_500.0, holding.getMarketValue());
        assertEquals(500.0, holding.getUnrealizedPnL());
        assertEquals(2_500.0, out[0].getTotalEquity()); // 1000 cash + 1500 holdings
    }

    @Test
    void missingAccountFails() {
        boolean[] failed = {false};
        ViewProfileOutputBoundary presenter = new ViewProfileOutputBoundary() {
            @Override
            public void presentProfile(ViewProfileResponseModel response) { }

            @Override
            public void presentFailure(String errorMessage) { failed[0] = true; }
        };

        new ViewProfileInteractor(new InMemoryAccountDAO(),
                ticker -> new Quote(ticker, 1.0, Instant.now()), presenter)
                .execute(new ViewProfileRequestModel("nobody"));

        assertTrue(failed[0]);
    }
}
