package com.marketmaker.use_case.cancel_order;

import com.marketmaker.data_access.AccountDAO;
import com.marketmaker.entities.Account;
import com.marketmaker.entities.Order;

public class CancelOrderInteractor implements CancelOrderInputBoundary {
    private final AccountDAO accountDAO;
    private final CancelOrderOutputBoundary presenter;

    public CancelOrderInteractor(AccountDAO accountDAO, CancelOrderOutputBoundary presenter) {
        this.accountDAO = accountDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(CancelOrderRequestModel request) {
        Account account = accountDAO.get(request.getAccountId());
        if (account == null) {
            presenter.presentFailure("Account not found.");
            return;
        }

        Order target = null;
        for (Order order : account.getPlacedOrders()) {
            if (order.getId().equals(request.getOrderId())) {
                target = order;
                break;
            }
        }

        if (target == null) {
            presenter.presentFailure("Order not found.");
            return;
        }
        if (target.getStatus() != Order.Status.PENDING) {
            presenter.presentFailure("Only pending orders can be cancelled.");
            return;
        }

        target.cancel();
        accountDAO.save(account);
        presenter.presentSuccess(new CancelOrderResponseModel(target.getId()));
    }
}
