package com.marketmaker.data_access;

import com.marketmaker.use_case.receive_live_quotes.LiveQuoteDataAccessInterface;
import com.marketmaker.use_case.receive_live_quotes.QuoteUpdateListener;

/**
 * Stands in for a streaming feed while the app polls instead.
 *
 * <p>Subscribing is implicit here: the refresh loop quotes whatever is on the account's
 * watchlist, so adding a ticker is enough to start seeing its price and removing one is
 * enough to stop. The methods are deliberately empty rather than absent, so the use cases
 * keep their streaming contract for whenever the Finnhub websocket adapter lands.
 */
public class PolledQuoteSubscription implements LiveQuoteDataAccessInterface {

    @Override
    public void subscribe(String ticker, QuoteUpdateListener listener) {
        // Nothing to do: the next poll picks the ticker up from the watchlist.
    }

    @Override
    public void unsubscribe(String ticker) {
        // Nothing to do: the next poll no longer asks for it.
    }
}
