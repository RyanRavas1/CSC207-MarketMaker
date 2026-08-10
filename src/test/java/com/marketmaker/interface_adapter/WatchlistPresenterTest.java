package com.marketmaker.interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.marketmaker.entities.Quote;
import com.marketmaker.use_case.view_watchlist.WatchlistResponseModel;

/** The table holds a ticker's last price through a failed refresh, and drops it on removal. */
class WatchlistPresenterTest {

    private static WatchlistResponseModel.Row priced(String ticker, double price) {
        return new WatchlistResponseModel.Row(ticker, price, Instant.EPOCH);
    }

    private static WatchlistResponseModel.Row unpriced(String ticker) {
        return new WatchlistResponseModel.Row(ticker, null, null);
    }

    @Test
    void aFailedQuoteKeepsTheLastPriceInsteadOfBlanking() {
        ViewModel<WatchlistResponseModel> viewModel = new ViewModel<>();
        WatchlistPresenter presenter = new WatchlistPresenter(viewModel, quotes -> { });

        presenter.presentWatchlist(new WatchlistResponseModel(
                List.of(priced("AAPL", 190.0), priced("MSFT", 410.0)), List.of()));
        assertEquals(2, viewModel.getState().getRows().size());

        // MSFT's call fails: it keeps its row, in place, at the price it last traded at.
        presenter.presentWatchlist(new WatchlistResponseModel(
                List.of(priced("AAPL", 191.0), unpriced("MSFT")), List.of("MSFT")));
        List<WatchlistResponseModel.Row> rows = viewModel.getState().getRows();
        assertEquals(List.of("AAPL", "MSFT"), List.of(rows.get(0).getTicker(), rows.get(1).getTicker()));
        assertEquals(191.0, rows.get(0).getPrice());
        assertEquals(410.0, rows.get(1).getPrice());
    }

    @Test
    void aTickerNeverPricedShowsNoPrice() {
        ViewModel<WatchlistResponseModel> viewModel = new ViewModel<>();
        new WatchlistPresenter(viewModel, quotes -> { })
                .presentWatchlist(new WatchlistResponseModel(List.of(unpriced("WAT")), List.of("WAT")));

        assertNull(viewModel.getState().getRows().get(0).getPrice());
    }

    @Test
    void removingATickerDropsItsRememberedPrice() {
        ViewModel<WatchlistResponseModel> viewModel = new ViewModel<>();
        WatchlistPresenter presenter = new WatchlistPresenter(viewModel, quotes -> { });

        presenter.presentWatchlist(new WatchlistResponseModel(List.of(priced("AAPL", 190.0)), List.of()));
        // Removed, then added back before it could be quoted: no stale price follows it in.
        presenter.presentWatchlist(new WatchlistResponseModel(List.of(), List.of()));
        presenter.presentWatchlist(new WatchlistResponseModel(List.of(unpriced("AAPL")), List.of("AAPL")));

        assertNull(viewModel.getState().getRows().get(0).getPrice());
    }

    /** The matcher must see only prices actually fetched, or it refills at a price that never moved. */
    @Test
    void onlyFreshQuotesReachTheMatcher() {
        List<List<Quote>> seen = new ArrayList<>();
        WatchlistPresenter presenter = new WatchlistPresenter(new ViewModel<>(), seen::add);

        presenter.presentWatchlist(new WatchlistResponseModel(List.of(priced("AAPL", 190.0)), List.of()));
        presenter.presentWatchlist(new WatchlistResponseModel(List.of(unpriced("AAPL")), List.of("AAPL")));

        assertEquals(1, seen.get(0).size());
        assertEquals(0, seen.get(1).size());
    }
}
