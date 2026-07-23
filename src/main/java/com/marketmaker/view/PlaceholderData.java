package com.marketmaker.view;

// Hard-coded sample data so the dashboard shows something for now. This gets
// replaced once the real use cases are hooked up.
public class PlaceholderData {

    public static final String STARTING = "$25,000.00";
    public static final String CASH = "$12,480.55";
    public static final String BUYING_POWER = "$24,961.10";
    public static final String EQUITY = "$28,714.90";
    public static final String DAY_PL = "+$342.18 (+1.21%)";

    public static final String SYMBOL = "AAPL";
    public static final String COMPANY = "Apple Inc.";
    public static final String LAST = "229.35";
    public static final String CHANGE = "+1.42 (+0.62%)";
    public static final String OPEN = "227.80";
    public static final String HIGH = "230.44";
    public static final String LOW = "227.31";
    public static final String CLOSE = "229.35";
    public static final String VOLUME = "18.2M";

    public static final String[] WATCHLIST_COLUMNS = {"Symbol", "Last", "Chg%"};
    public static final String[][] WATCHLIST = {
        {"AAPL", "229.35", "+0.62%"},
        {"NVDA", "121.44", "+1.83%"},
        {"TSLA", "242.15", "-2.14%"},
        {"MSFT", "433.28", "+0.74%"},
        {"AMZN", "201.30", "+0.42%"},
        {"SPY", "559.12", "+0.28%"},
        {"AMD", "168.55", "-1.02%"},
        {"META", "597.40", "+1.14%"},
        {"GOOGL", "178.66", "-0.35%"},
    };

    public static final String[] POSITIONS_COLUMNS = {"Sym", "Qty", "Avg", "Last", "Mkt Val", "Unrl P/L"};
    public static final String[][] POSITIONS = {
        {"AAPL", "30", "224.10", "229.35", "6,880.50", "+157.50"},
        {"NVDA", "15", "118.20", "121.44", "1,821.60", "+48.60"},
        {"TSLA", "10", "250.00", "242.15", "2,421.50", "-78.50"},
        {"MSFT", "8", "430.10", "433.28", "3,466.24", "+25.44"},
    };

    public static final String[] ORDER_HISTORY_COLUMNS = {
        "Time", "Symbol", "Side", "Type", "Qty", "Lmt / Stop", "Fill", "Status", "Realized P/L",
    };
    public static final String[][] ORDER_HISTORY = {
        {"09:31:04", "AAPL", "BUY", "MARKET", "30", "—", "224.10", "Filled", "—"},
        {"09:45:12", "NVDA", "BUY", "LIMIT", "15", "118.00", "118.20", "Filled", "—"},
        {"10:02:37", "TSLA", "SELL", "LIMIT", "10", "250.00", "—", "Pending", "—"},
        {"10:18:51", "SPY", "BUY", "MARKET", "20", "—", "—", "Cancelled", "—"},
    };

    public static final String[] RECENT_BARS_COLUMNS = {"Time", "Open", "High", "Low", "Close"};
    public static final String[][] RECENT_BARS = {
        {"14:25", "228.90", "229.20", "228.70", "229.05"},
        {"14:26", "229.05", "229.40", "228.95", "229.30"},
        {"14:27", "229.30", "229.55", "229.10", "229.20"},
        {"14:28", "229.20", "229.35", "228.85", "228.95"},
        {"14:29", "228.95", "229.45", "228.90", "229.40"},
        {"14:30", "229.40", "229.60", "229.25", "229.35"},
    };
}
