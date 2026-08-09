package com.marketmaker.interface_adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.watchlist.WatchlistOutputBoundary;
import com.marketmaker.use_case.watchlist.WatchlistResponseModel;

/**
 * Publishes the quoted watchlist, and hands the same prices to whoever else needs them.
 *
 * <p>The prices a refresh fetches are the only prices the app has, so a resting order has to
 * see them too — {@code onQuotes} is how the matcher gets its look before the table is drawn.
 *
 * <p>Tickers the feed couldn't price are reported rather than dropped: silently showing four
 * rows where the user added five looks like the app lost one.
 */
public class WatchlistPresenter implements WatchlistOutputBoundary {
    private final ViewModel<List<Quote>> viewModel;
    private final Consumer<List<Quote>> onQuotes;

    public WatchlistPresenter(ViewModel<List<Quote>> viewModel, Consumer<List<Quote>> onQuotes) {
        this.viewModel = viewModel;
        this.onQuotes = onQuotes;
    }

    @Override
    public void presentWatchlist(WatchlistResponseModel response) {
        List<Quote> quotes = new ArrayList<>(response.getRows().size());
        for (WatchlistResponseModel.Row row : response.getRows()) {
            quotes.add(new Quote(row.getTicker(), row.getPrice(), row.getTradedAt()));
        }

        onQuotes.accept(quotes);
        viewModel.setState(quotes);

        if (!response.getUnavailable().isEmpty()) {
            viewModel.setError("No price for " + String.join(", ", response.getUnavailable()) + ".");
        }
    }

    @Override
    public void presentFailure(String errorMessage) {
        viewModel.setError(errorMessage);
    }
}
