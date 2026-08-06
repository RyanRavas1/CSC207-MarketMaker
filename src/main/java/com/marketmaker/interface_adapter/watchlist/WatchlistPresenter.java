package com.marketmaker.interface_adapter.watchlist;

import java.util.ArrayList;
import java.util.List;

import com.marketmaker.interface_adapter.Money;
import com.marketmaker.use_case.watchlist.WatchlistOutputBoundary;
import com.marketmaker.use_case.watchlist.WatchlistResponseModel;

/** Turns the watchlist response into strings the view can render as-is. */
public class WatchlistPresenter implements WatchlistOutputBoundary {
    private final WatchlistViewModel viewModel;

    public WatchlistPresenter(WatchlistViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentWatchlist(WatchlistResponseModel response) {
        List<String[]> rows = new ArrayList<>();
        for (WatchlistResponseModel.Row row : response.getRows()) {
            rows.add(new String[]{row.getTicker(), Money.format(row.getPrice())});
        }

        WatchlistState state = new WatchlistState();
        state.setRows(rows);
        // The quoted tickers still render; name the ones that didn't so the user knows which
        // row to remove.
        if (!response.getUnavailable().isEmpty()) {
            state.setError("No quote for " + String.join(", ", response.getUnavailable())
                    + " — check the ticker symbol.");
        }
        viewModel.publish(state);
    }

    @Override
    public void presentFailure(String errorMessage) {
        // Keep the last good rows so a failed refresh doesn't blank the table.
        WatchlistState state = new WatchlistState();
        state.setRows(viewModel.getState().getRows());
        state.setError(errorMessage);
        viewModel.publish(state);
    }
}
