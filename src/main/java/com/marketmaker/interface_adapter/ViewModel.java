package com.marketmaker.interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Base view model: holds one state object and tells listening views when it changes. */
public class ViewModel<T> {
    private final String viewName;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    // Written by the thread running the use case, read by the UI thread — volatile so the
    // reader is guaranteed to see the latest state rather than a stale cached one.
    private volatile T state;

    public ViewModel(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() { return viewName; }
    public T getState() { return state; }
    public void setState(T state) { this.state = state; }
    public void addPropertyChangeListener(PropertyChangeListener listener) { support.addPropertyChangeListener(listener); }

    // Views repaint off this; old value is null so the event always fires.
    public void firePropertyChanged() { support.firePropertyChange("state", null, this.state); }

    /** How every presenter hands a finished state to its view: swap it in, then repaint. */
    public void publish(T state) {
        setState(state);
        firePropertyChanged();
    }
}
