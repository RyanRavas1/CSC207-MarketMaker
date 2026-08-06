package com.marketmaker.interface_adapter.trading;

import java.util.ArrayList;

import com.marketmaker.interface_adapter.ViewModel;

/** The trading screen's state, shared by all four order use cases. */
public class TradingViewModel extends ViewModel<TradingState> {
    public TradingViewModel() {
        super("trading");
        setState(new TradingState());
    }

    /**
     * Drops every pending row, for when the screen switches to a different account.
     * ponytail: clears rather than reloads — showing the new account's existing pending
     * orders needs a ListPendingOrders use case, which doesn't exist yet.
     */
    public void clearPendingOrders() {
        TradingState state = new TradingState(getState());
        state.setPendingOrders(new ArrayList<>());
        publish(state);
    }
}
