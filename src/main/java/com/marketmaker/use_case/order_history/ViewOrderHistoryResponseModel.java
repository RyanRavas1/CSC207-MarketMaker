package com.marketmaker.use_case.order_history;

import java.time.Instant;
import java.util.List;

import com.marketmaker.entities.Order;

public class ViewOrderHistoryResponseModel {

    /** One order and, once it filled, what the fill was worth. */
    public static class Row {
        private final Instant placedAt;
        private final String ticker;
        private final Order.Side side;
        private final Order.Type type;
        private final int quantity;
        private final Double limitOrStopPrice; // null for market orders
        private final Order.Status status;
        private final Double fillPrice; // null until the order fills
        private final Double realizedPnL; // null unless the fill closed or reduced a position

        public Row(Instant placedAt, String ticker, Order.Side side, Order.Type type, int quantity,
                   Double limitOrStopPrice, Order.Status status, Double fillPrice, Double realizedPnL) {
            this.placedAt = placedAt;
            this.ticker = ticker;
            this.side = side;
            this.type = type;
            this.quantity = quantity;
            this.limitOrStopPrice = limitOrStopPrice;
            this.status = status;
            this.fillPrice = fillPrice;
            this.realizedPnL = realizedPnL;
        }

        public Instant getPlacedAt() { return placedAt; }
        public String getTicker() { return ticker; }
        public Order.Side getSide() { return side; }
        public Order.Type getType() { return type; }
        public int getQuantity() { return quantity; }
        public Double getLimitOrStopPrice() { return limitOrStopPrice; }
        public Order.Status getStatus() { return status; }
        public Double getFillPrice() { return fillPrice; }
        public Double getRealizedPnL() { return realizedPnL; }
    }

    private final List<Row> rows; // newest order first
    private final double totalRealizedPnL; // every gain and loss booked on this account

    public ViewOrderHistoryResponseModel(List<Row> rows, double totalRealizedPnL) {
        this.rows = rows;
        this.totalRealizedPnL = totalRealizedPnL;
    }

    public List<Row> getRows() { return rows; }
    public double getTotalRealizedPnL() { return totalRealizedPnL; }
}
