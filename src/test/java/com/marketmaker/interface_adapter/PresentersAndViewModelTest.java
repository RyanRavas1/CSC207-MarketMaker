package com.marketmaker.interface_adapter;

import com.marketmaker.entities.Candle;
import com.marketmaker.use_case.view_trend_chart.Resolution;
import com.marketmaker.use_case.view_trend_chart.ViewTrendChartResponseModel;
import com.marketmaker.use_case.view_order_history.ViewOrderHistoryResponseModel;
import com.marketmaker.use_case.view_portfolio_summary.ViewPortfolioSummaryResponseModel;
import com.marketmaker.use_case.view_positions.ViewPositionsResponseModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PresentersAndViewModelTest {
    @Test void viewModelStoresAndPublishesStateAndErrors() {
        ViewModel<String> viewModel = new ViewModel<>(); AtomicReference<String> stateEvent = new AtomicReference<>(); AtomicReference<String> errorEvent = new AtomicReference<>();
        viewModel.onState(stateEvent::set); viewModel.onError(errorEvent::set);
        viewModel.setState("ready"); viewModel.setError("failed");
        assertAll(() -> assertEquals("ready", viewModel.getState()), () -> assertEquals("ready", stateEvent.get()), () -> assertEquals("failed", viewModel.getError()), () -> assertEquals("failed", errorEvent.get()));
    }

    @Test void presentersForwardSuccessAndFailureToTheirViewModels() {
        ViewModel<ViewPortfolioSummaryResponseModel> portfolio = new ViewModel<>(); ViewPortfolioSummaryResponseModel summary = new ViewPortfolioSummaryResponseModel(1, 2, 3, 4);
        new PortfolioSummaryPresenter(portfolio).presentSuccess(summary); assertSame(summary, portfolio.getState()); new PortfolioSummaryPresenter(portfolio).presentFailure("portfolio error"); assertEquals("portfolio error", portfolio.getError());

        ViewModel<ViewPositionsResponseModel> positions = new ViewModel<>(); ViewPositionsResponseModel positionResponse = new ViewPositionsResponseModel(List.of());
        new PositionsPresenter(positions).presentSuccess(positionResponse); assertSame(positionResponse, positions.getState()); new PositionsPresenter(positions).presentFailure("positions error"); assertEquals("positions error", positions.getError());

        ViewModel<ViewOrderHistoryResponseModel> history = new ViewModel<>(); ViewOrderHistoryResponseModel historyResponse = new ViewOrderHistoryResponseModel(List.of(), List.of());
        new OrderHistoryPresenter(history).presentSuccess(historyResponse); assertSame(historyResponse, history.getState()); new OrderHistoryPresenter(history).presentFailure("history error"); assertEquals("history error", history.getError());

        ViewModel<ViewTrendChartResponseModel> chart = new ViewModel<>(); ViewTrendChartResponseModel chartResponse = new ViewTrendChartResponseModel("AAPL", Resolution.ONE_MONTH, List.<Candle>of());
        new TrendChartPresenter(chart).presentSuccess(chartResponse); assertSame(chartResponse, chart.getState()); new TrendChartPresenter(chart).presentFailure("chart error"); assertEquals("chart error", chart.getError());
    }
}
