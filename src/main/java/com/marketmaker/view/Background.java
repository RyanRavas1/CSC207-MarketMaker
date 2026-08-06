package com.marketmaker.view;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs a use case off the event dispatch thread, so a slow HTTP quote can't freeze the window.
 *
 * <p>One thread for the whole app on purpose: every use case here mutates the same account, and
 * a single worker keeps them serialized exactly as they were when everything ran on the EDT.
 * Presenters fire back from this thread, so views hop to the EDT in propertyChange.
 */
public final class Background {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "use-case");
        thread.setDaemon(true); // don't keep the JVM alive after the window closes
        return thread;
    });

    private Background() {
    }

    public static void run(Runnable useCase) {
        WORKER.execute(useCase);
    }
}
