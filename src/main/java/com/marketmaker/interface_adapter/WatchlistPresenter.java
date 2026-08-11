package com.marketmaker.interface_adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.view_watchlist.WatchlistOutputBoundary;
import com.marketmaker.use_case.view_watchlist.WatchlistResponseModel;

/**
 * Publishes the watched tickers, and hands the prices among them to whoever else needs them.
 *
 * <p>The prices a refresh fetches are the only prices the app has, so a resting order has to
 * see them too - {@code onQuotes} is how the matcher gets its look before the table is drawn.
 *
 * <p>Which rows the table has is the interactor's answer, one per watched ticker. What this
 * adds is memory: a ticker the feed could not price this time keeps the last price it had,
 * instead of blanking to a dash every time a call times out. The row's timestamp is left at
 * the moment that price was traded, so a held-over price shows its true age and the status
 * line calls the panel delayed rather than live.
 */
public class WatchlistPresenter implements WatchlistOutputBoundary {
    private final ViewModel<WatchlistResponseModel> viewModel;
    private final Consumer<List<Quote>> onQuotes;

    // Last price seen per ticker. Written only from the worker thread the interactor runs on;
    // the hop to the event dispatch thread happens inside ViewModel.
    private final Map<String, WatchlistResponseModel.Row> lastPriced = new HashMap<>();

    public WatchlistPresenter(ViewModel<WatchlistResponseModel> viewModel, Consumer<List<Quote>> onQuotes) {
        this.viewModel = viewModel;
        this.onQuotes = onQuotes;
    }

    @Override
    public void presentWatchlist(WatchlistResponseModel response) {
        List<Quote> fresh = new ArrayList<>();
        List<WatchlistResponseModel.Row> rows = new ArrayList<>(response.getRows().size());
        for (WatchlistResponseModel.Row row : response.getRows()) {
            if (row.getPrice() == null) {
                // Priceless this time: show what it last traded at, or a dash if this ticker
                // has never priced at all.
                rows.add(lastPriced.getOrDefault(row.getTicker(), row));
            } else {
                lastPriced.put(row.getTicker(), row);
                rows.add(row);
                fresh.add(new Quote(row.getTicker(), row.getPrice(), row.getTradedAt()));
            }
        }
        // A ticker that is no longer watched should not keep a price waiting for it to return.
        lastPriced.keySet().retainAll(tickersOf(response.getRows()));

        // Only the prices actually fetched this tick. Matching a resting order against a
        // held-over price would fill it again on every refresh at a price that never moved.
        onQuotes.accept(fresh);
        viewModel.setState(new WatchlistResponseModel(rows, response.getUnavailable()));

        if (!response.getUnavailable().isEmpty()) {
            viewModel.setError("No price for " + String.join(", ", response.getUnavailable()) + ".");
        }
    }

    @Override
    public void presentFailure(String errorMessage) {
        viewModel.setError(errorMessage);
    }

    private static List<String> tickersOf(List<WatchlistResponseModel.Row> rows) {
        List<String> tickers = new ArrayList<>(rows.size());
        for (WatchlistResponseModel.Row row : rows) {
            tickers.add(row.getTicker());
        }
        return tickers;
    }
}
