package com.marketmaker.use_case.view_order_history;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.marketmaker.use_case.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;
import com.marketmaker.entities.Trade;

/** Browses a timestamped log of all orders and their status, plus the trades they produced. */
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

        List<OrderHistoryRow> orderRows = new ArrayList<>();
        for (Order order : account.getPlacedOrders()) {
            Instant timestamp = order.getFilledAt() != null ? order.getFilledAt() : order.getCreatedAt();
            orderRows.add(new OrderHistoryRow(order.getId(), order.getTicker(), order.getSide(), order.getType(),
                    order.getQuantity(), order.getLimitOrStopPrice(), order.getStatus(), timestamp));
        }

        List<TradeHistoryRow> tradeRows = new ArrayList<>();
        for (Trade trade : account.getTradeLog()) {
            tradeRows.add(new TradeHistoryRow(trade.getId(), trade.getTicker(), trade.getSide(),
                    trade.getQuantity(), trade.getPrice(), trade.getTimestamp(), trade.getRealizedPnL()));
        }

        presenter.presentSuccess(new ViewOrderHistoryResponseModel(orderRows, tradeRows));
    }
}
