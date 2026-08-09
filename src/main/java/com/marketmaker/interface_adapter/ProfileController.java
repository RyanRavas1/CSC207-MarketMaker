package com.marketmaker.interface_adapter;

import com.marketmaker.use_case.user_profile.ViewProfileInputBoundary;
import com.marketmaker.use_case.user_profile.ViewProfileRequestModel;

/** Asks for the account overview when the user opens it. */
public class ProfileController {
    private final ViewProfileInputBoundary interactor;
    private final String accountId;

    public ProfileController(ViewProfileInputBoundary interactor, String accountId) {
        this.interactor = interactor;
        this.accountId = accountId;
    }

    public void show() {
        interactor.execute(new ViewProfileRequestModel(accountId));
    }
}
