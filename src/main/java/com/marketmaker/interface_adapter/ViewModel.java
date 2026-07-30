package com.marketmaker.interface_adapter;

import java.beans.PropertyChangeSupport;
import java.util.function.Consumer;

/**
 * Holds the state one panel displays and notifies that panel when it changes.
 *
 * <p>Presenters write here; views subscribe. Neither knows about the other, so the
 * use-case layer never depends on Swing. One generic view model serves every panel
 * because they all have the same shape: a single immutable state object, replaced
 * wholesale each time an interactor reports a result.
 *
 * @param <S> the state type, normally a use-case response model
 */
public class ViewModel<S> {

    private static final String STATE = "state";
    private static final String ERROR = "error";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private S state;
    private String error;

    public S getState() {
        return state;
    }

    public void setState(S newState) {
        S previous = this.state;
        this.state = newState;
        support.firePropertyChange(STATE, previous, newState);
    }

    public String getError() {
        return error;
    }

    public void setError(String newError) {
        String previous = this.error;
        this.error = newError;
        support.firePropertyChange(ERROR, previous, newError);
    }

    /** Runs {@code listener} with the new state every time a presenter publishes one. */
    public void onState(Consumer<S> listener) {
        support.addPropertyChangeListener(STATE, event -> {
            @SuppressWarnings("unchecked")
            S value = (S) event.getNewValue();
            listener.accept(value);
        });
    }

    /** Runs {@code listener} with the message every time a presenter reports a failure. */
    public void onError(Consumer<String> listener) {
        support.addPropertyChangeListener(ERROR, event -> listener.accept((String) event.getNewValue()));
    }
}
