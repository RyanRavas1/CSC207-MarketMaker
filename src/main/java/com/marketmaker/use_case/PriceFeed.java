package com.marketmaker.use_case;

import com.marketmaker.entities.Quote;

public interface PriceFeed {
    Quote getQuote(String ticker);
}
