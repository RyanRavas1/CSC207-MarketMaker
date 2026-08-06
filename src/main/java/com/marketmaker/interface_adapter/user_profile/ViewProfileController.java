package com.marketmaker.interface_adapter.user_profile;

import com.marketmaker.use_case.user_profile.ViewProfileInputBoundary;
import com.marketmaker.use_case.user_profile.ViewProfileRequestModel;

/** Converts what the view has on screen into a view-profile request. */
public class ViewProfileController {
    private final ViewProfileInputBoundary interactor;

    public ViewProfileController(ViewProfileInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void view(String accountId) {
        interactor.execute(new ViewProfileRequestModel(accountId));
    }
}
