package com.marketmaker.interface_adapter.watchlist;

import com.marketmaker.interface_adapter.ViewModel;

/** The watchlist screen's state. */
public class WatchlistViewModel extends ViewModel<WatchlistState> {
    public WatchlistViewModel() {
        super("watchlist");
        setState(new WatchlistState());
    }
}
