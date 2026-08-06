package com.marketmaker.interface_adapter.order_history;

import java.util.ArrayList;
import java.util.List;

/** Display-ready order history: everything is already formatted for the view. */
public class OrderHistoryState {
    // {time, symbol, side, type, qty, limit/stop, fill/status, realized P/L} as shown
    private List<String[]> rows = new ArrayList<>();
    private String totalRealizedPnL = "";
    private String error = "";

    public List<String[]> getRows() { return rows; }
    public void setRows(List<String[]> rows) { this.rows = rows; }
    public String getTotalRealizedPnL() { return totalRealizedPnL; }
    public void setTotalRealizedPnL(String totalRealizedPnL) { this.totalRealizedPnL = totalRealizedPnL; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
