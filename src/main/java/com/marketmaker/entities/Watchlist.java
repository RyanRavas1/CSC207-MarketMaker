package com.marketmaker.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The set of tickers a user is tracking, independent of what they hold. */
public class Watchlist {

    private final List<String> tickers;

    public Watchlist() {
        this.tickers = new ArrayList<>();
    }

    public boolean add(String ticker) {
        if (tickers.contains(ticker)) {
            return false;
        }
        return tickers.add(ticker);
    }

    public boolean remove(String ticker) {
        return tickers.remove(ticker);
    }

    public boolean contains(String ticker) {
        return tickers.contains(ticker);
    }

    public List<String> getTickers() {
        return Collections.unmodifiableList(tickers);
    }
}
