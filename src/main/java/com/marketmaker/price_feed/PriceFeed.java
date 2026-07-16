package com.marketmaker.price_feed;

import com.marketmaker.entities.Quote;

public interface PriceFeed {
    Quote getQuote(String ticker);
}
