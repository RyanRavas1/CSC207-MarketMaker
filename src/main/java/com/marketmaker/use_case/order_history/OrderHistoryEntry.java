package com.marketmaker.use_case.order_history;

/**
 * One row of order/trade history, as returned by the data access layer.
 * Read-only. Lives in the use-case layer so both the data access
 * implementation and the view can depend on it without depending on
 * each other.
 */
public class OrderHistoryEntry {
    private final String time;
    private final String symbol;
    private final String side;
    private final String type;
    private final int quantity;
    private final String limitOrStop;
    private final String fillStatus;
    private final String realizedPl;

    public OrderHistoryEntry(String time,
                             String symbol,
                             String side,
                             String type,
                             int quantity,
                             String limitOrStop,
                             String fillStatus,
                             String realizedPl) {
        this.time = time;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.quantity = quantity;
        this.limitOrStop = limitOrStop;
        this.fillStatus = fillStatus;
        this.realizedPl = realizedPl;
    }

    public String getTime() { return time; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public String getType() { return type; }
    public int getQuantity() { return quantity; }
    public String getLimitOrStop() { return limitOrStop; }
    public String getFillStatus() { return fillStatus; }
    public String getRealizedPl() { return realizedPl; }
}
