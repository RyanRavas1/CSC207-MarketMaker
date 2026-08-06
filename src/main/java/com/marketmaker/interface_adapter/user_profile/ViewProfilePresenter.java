package com.marketmaker.interface_adapter.user_profile;

import java.util.ArrayList;
import java.util.List;

import com.marketmaker.interface_adapter.Money;
import com.marketmaker.use_case.user_profile.ViewProfileOutputBoundary;
import com.marketmaker.use_case.user_profile.ViewProfileResponseModel;

/** Turns the profile response into strings the view can render as-is. */
public class ViewProfilePresenter implements ViewProfileOutputBoundary {
    private final ViewProfileViewModel viewModel;

    public ViewProfilePresenter(ViewProfileViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void presentProfile(ViewProfileResponseModel response) {
        List<String[]> holdings = new ArrayList<>();
        for (ViewProfileResponseModel.Holding holding : response.getHoldings()) {
            holdings.add(new String[]{
                    holding.getTicker(),
                    String.valueOf(holding.getShares()),
                    Money.format(holding.getAveragePrice()),
                    Money.format(holding.getCurrentPrice()),
                    Money.format(holding.getMarketValue()),
                    Money.format(holding.getUnrealizedPnL())
            });
        }

        ViewProfileState state = new ViewProfileState();
        state.setUserName(response.getUserName());
        state.setCashBalance(Money.format(response.getCashBalance()));
        state.setTotalEquity(Money.format(response.getTotalEquity()));
        state.setBuyingPower(Money.format(response.getBuyingPower()));
        state.setRealizedPnLToday(Money.format(response.getRealizedPnLToday()));
        state.setDailyPnL(Money.format(response.getDailyPnL()));
        state.setHoldings(holdings);
        viewModel.publish(state);
    }

    @Override
    public void presentFailure(String errorMessage) {
        ViewProfileState state = new ViewProfileState();
        state.setError(errorMessage);
        viewModel.publish(state);
    }
}
