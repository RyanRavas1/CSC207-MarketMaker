package com.marketmaker.interface_adapter.user_profile;

import com.marketmaker.interface_adapter.ViewModel;

/** The profile screen's state. */
public class ViewProfileViewModel extends ViewModel<ViewProfileState> {
    public ViewProfileViewModel() {
        super("profile");
        setState(new ViewProfileState());
    }
}
