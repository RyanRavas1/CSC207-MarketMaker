package com.marketmaker.view;

import com.marketmaker.entities.Quote;
import com.marketmaker.interface_adapter.ViewModel;
import com.marketmaker.use_case.view_candlestick_chart.ViewCandlestickChartResponseModel;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Smoke tests that intentionally run only where a graphical display is available. */
class FramesDisplayTest {
    @Test void dashboardFrameBuildsWithAllViewModels() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "JFrame requires a graphical display");
        onEdt(() -> {
            DashboardFrame frame = new DashboardFrame(new ViewModel<ViewPortfolioSummaryResponseModel>(), new ViewModel<ViewPositionsResponseModel>(), new ViewModel<ViewOrderHistoryResponseModel>(), new ViewModel<ViewCandlestickChartResponseModel>(), new ViewModel<List<Quote>>());
            try { assertEquals("MarketMaker — Paper Trading Simulator", frame.getTitle()); assertNotNull(frame.getContentPane()); assertTrue(frame.getContentPane().getComponentCount() > 0); }
            finally { frame.dispose(); }
        });
    }

    @Test void mainWindowBuildsAroundProvidedHistoryPanel() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "JFrame requires a graphical display");
        onEdt(() -> {
            JPanel history = new JPanel(); MainWindow window = new MainWindow(history);
            try { assertEquals("MarketMaker — Paper Trading Simulator", window.getTitle()); assertSame(history, ((BorderLayout) window.getContentPane().getLayout()).getLayoutComponent(BorderLayout.SOUTH)); }
            finally { window.dispose(); }
        });
    }

    private static void onEdt(Runnable runnable) throws Exception { SwingUtilities.invokeAndWait(runnable); }
}
