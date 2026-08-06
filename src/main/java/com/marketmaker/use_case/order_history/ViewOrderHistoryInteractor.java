package com.marketmaker.use_case.order_history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Trade;

/** Reports every order the account has placed — filled, pending or cancelled. */
public class ViewOrderHistoryInteractor implements ViewOrderHistoryInputBoundary {
    private final AccountDAO accountDAO;
    private final ViewOrderHistoryOutputBoundary presenter;

    public ViewOrderHistoryInteractor(AccountDAO accountDAO, ViewOrderHistoryOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(ViewOrderHistoryRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        // The realised gain belongs to the trade, not the order, so index the trades by the
        // order that produced them rather than scanning the log once per row.
        Map<String, Double> realizedByOrder = new HashMap<>();
        double totalRealizedPnL = 0.0;
        for (Trade trade : account.getTradeLog()) {
            if (trade.getRealizedPnL() != null) {
                realizedByOrder.put(trade.getOrderId(), trade.getRealizedPnL());
                totalRealizedPnL += trade.getRealizedPnL();
            }
        }

        List<ViewOrderHistoryResponseModel.Row> rows = new ArrayList<>();
        List<Order> orders = account.getPlacedOrders();
        // Newest first: the order someone just placed is the one they want to see.
        for (int index = orders.size() - 1; index >= 0; index--) {
            Order order = orders.get(index);
            rows.add(new ViewOrderHistoryResponseModel.Row(
                    order.getCreatedAt(), order.getTicker(), order.getSide(), order.getType(),
                    order.getQuantity(), order.getLimitOrStopPrice(), order.getStatus(),
                    order.getFillPrice(), realizedByOrder.get(order.getId())));
        }

        presenter.presentHistory(new ViewOrderHistoryResponseModel(rows, totalRealizedPnL));
    }
}
