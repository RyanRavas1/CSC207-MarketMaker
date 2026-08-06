package com.marketmaker.data_access;

import com.marketmaker.entities.Quote;
import com.marketmaker.price_feed.PriceFeed;
import com.marketmaker.price_feed.PriceFeedException;
import com.marketmaker.use_case.search_ticker.TickerDataAccessInterface;

/**
 * Lets a {@link PriceFeed} stand in wherever a {@link TickerDataAccessInterface} is wanted,
 * so the panels can show live prices instead of the stub.
 *
 * <p>The two contracts disagree about failure: the feed throws, while this interface answers
 * null for a ticker it doesn't know. Translating that is the whole job of this class.
 */
public class PriceFeedQuoteDataAccessObject implements TickerDataAccessInterface {
    private final PriceFeed priceFeed;

    public PriceFeedQuoteDataAccessObject(PriceFeed priceFeed) {
        this.priceFeed = priceFeed;
    }

    @Override
    public Quote fetchQuote(String ticker) {
        // A rate limit or an outage reads the same as an unknown ticker here. Callers only
        // get to say "no price", so a null is the most this contract can report.
        try {
            return priceFeed.getQuote(ticker);
        } catch (PriceFeedException exception) {
            return null;
        }
    }
}
