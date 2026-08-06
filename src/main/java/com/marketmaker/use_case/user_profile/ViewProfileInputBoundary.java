package com.marketmaker.use_case.user_profile;

/** Entry point called by controllers to run the view-profile use case. */
public interface ViewProfileInputBoundary {
    void execute(ViewProfileRequestModel request);
}
