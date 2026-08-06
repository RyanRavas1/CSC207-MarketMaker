package com.marketmaker.interface_adapter.order_history;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.marketmaker.interface_adapter.Money;
import com.marketmaker.use_case.order_history.ViewOrderHistoryOutputBoundary;
import com.marketmaker.use_case.order_history.ViewOrderHistoryResponseModel;

/** Turns the order history into strings the view can render as-is. */
public class ViewOrderHistoryPresenter implements ViewOrderHistoryOutputBoundary {
    // Date and time: the log outlives the session that wrote it, so the day matters.
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final String NONE = "—";

    private final OrderHistoryViewModel viewModel;

    public ViewOrderHistoryPresenter(OrderHistoryViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentHistory(ViewOrderHistoryResponseModel response) {
        List<String[]> rows = new ArrayList<>();
        for (ViewOrderHistoryResponseModel.Row row : response.getRows()) {
            rows.add(new String[]{
                    TIMESTAMP.format(row.getPlacedAt()),
                    row.getTicker(),
                    row.getSide().toString(),
                    row.getType().toString(),
                    String.valueOf(row.getQuantity()),
                    row.getLimitOrStopPrice() == null ? NONE : Money.format(row.getLimitOrStopPrice()),
                    fillStatus(row),
                    row.getRealizedPnL() == null ? NONE : Money.format(row.getRealizedPnL())
            });
        }

        OrderHistoryState state = new OrderHistoryState();
        state.setRows(rows);
        state.setTotalRealizedPnL(Money.format(response.getTotalRealizedPnL()));
        viewModel.publish(state);
    }

    @Override
    public void presentFailure(String errorMessage) {
        OrderHistoryState state = new OrderHistoryState();
        state.setError(errorMessage);
        viewModel.publish(state);
    }

    // One column answers both "what happened to it" and "at what price", the way a broker's
    // blotter does: a fill is only interesting alongside the price it filled at.
    private String fillStatus(ViewOrderHistoryResponseModel.Row row) {
        if (row.getFillPrice() != null) {
            return "Filled @ " + Money.format(row.getFillPrice());
        }
        switch (row.getStatus()) {
            case PENDING:
                return "Pending";
            case CANCELED:
                return "Cancelled";
            default:
                return row.getStatus().toString();
        }
    }
}
