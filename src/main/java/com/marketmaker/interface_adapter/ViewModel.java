package com.marketmaker.interface_adapter;

import java.beans.PropertyChangeSupport;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

/**
 * Holds the state one panel displays and notifies that panel when it changes.
 *
 * <p>Presenters write here; views subscribe. Neither knows about the other, so the
 * use-case layer never depends on Swing. One generic view model serves every panel
 * because they all have the same shape: a single immutable state object, replaced
 * wholesale each time an interactor reports a result.
 *
 * <p>Interactors run on a worker thread - quoting a ticker is an HTTP round trip - but Swing
 * may only be touched from the event dispatch thread. The hop happens here, once, rather than
 * in every presenter and panel: a presenter writes from wherever it happens to be running, and
 * subscribers are always called on the EDT.
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
        this.state = newState;
        // Old value is passed as null so an unchanged state still notifies: PropertyChangeSupport
        // drops equal old/new pairs, which would silently swallow the same order failure twice
        // in a row - the second click would look like nothing happened.
        onEventThread(() -> support.firePropertyChange(STATE, null, newState));
    }

    public String getError() {
        return error;
    }

    public void setError(String newError) {
        this.error = newError;
        onEventThread(() -> support.firePropertyChange(ERROR, null, newError));
    }

    /**
     * Runs {@code notify} on the event dispatch thread, immediately when already there so a
     * presenter called from the EDT still sees its listeners run before it returns.
     */
    private static void onEventThread(Runnable notify) {
        if (SwingUtilities.isEventDispatchThread()) {
            notify.run();
        } else {
            SwingUtilities.invokeLater(notify);
        }
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
