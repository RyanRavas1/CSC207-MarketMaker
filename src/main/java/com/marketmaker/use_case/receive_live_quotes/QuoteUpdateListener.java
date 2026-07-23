package com.marketmaker.use_case.receive_live_quotes;

import com.marketmaker.entities.Quote;

/** Called every time a subscribed ticker's price changes. */
@FunctionalInterface
public interface QuoteUpdateListener {
    void onQuote(Quote quote);
}
