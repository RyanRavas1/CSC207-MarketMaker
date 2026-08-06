package com.marketmaker.interface_adapter.watchlist;

import java.util.ArrayList;
import java.util.List;

/** Display-ready watchlist: everything is already formatted for the view. */
public class WatchlistState {
    private List<String[]> rows = new ArrayList<>(); // {ticker, price} as shown
    private String error = "";

    public List<String[]> getRows() { return rows; }
    public void setRows(List<String[]> rows) { this.rows = rows; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
