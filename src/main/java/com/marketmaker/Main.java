package com.marketmaker;

import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.marketmaker.entities.Quote;
import com.marketmaker.interface_adapter.CandlestickChartPresenter;
import com.marketmaker.interface_adapter.OrderHistoryPresenter;
import com.marketmaker.interface_adapter.PortfolioSummaryPresenter;
import com.marketmaker.interface_adapter.PositionsPresenter;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartResponseModel;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;
import com.marketmaker.view.DashboardFrame;

public class Main {

    public static void main(String[] args) {
        // Use the cross-platform look so it renders the same on everyone's machine.
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        }
        catch (UnsupportedLookAndFeelException e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            ViewModel<ViewPortfolioSummaryResponseModel> summary = new ViewModel<>();
            ViewModel<ViewPositionsResponseModel> positions = new ViewModel<>();
            ViewModel<ViewOrderHistoryResponseModel> orderHistory = new ViewModel<>();
            ViewModel<ViewCandlestickChartResponseModel> chart = new ViewModel<>();
            ViewModel<List<Quote>> watchlist = new ViewModel<>();

            new DashboardFrame(summary, positions, orderHistory, chart, watchlist).setVisible(true);

            // Replace these four lines with the real interactors, constructed against the
            // same presenters — AccountDAO and PriceFeed already exist for that.
            new PortfolioSummaryPresenter(summary).presentSuccess(DemoData.summary());
            new PositionsPresenter(positions).presentSuccess(DemoData.positions());
            new OrderHistoryPresenter(orderHistory).presentSuccess(DemoData.orderHistory());
            new CandlestickChartPresenter(chart).presentSuccess(DemoData.chart());
            watchlist.setState(DemoData.watchlist());
        });
    }
}
