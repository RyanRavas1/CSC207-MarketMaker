package com.marketmaker.interface_adapter.order_history;

import com.marketmaker.interface_adapter.ViewModel;

/** The order and trade history screen's state. */
public class OrderHistoryViewModel extends ViewModel<OrderHistoryState> {
    public OrderHistoryViewModel() {
        super("order history");
        setState(new OrderHistoryState());
    }
}
