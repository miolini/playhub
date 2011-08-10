package org.playhub;

import org.eclipse.jetty.continuation.Continuation;

public class ApplicationRestarter implements Runnable {
    private Application app;

    public ApplicationRestarter(Application app) {
        this.app = app;
    }

    public void run() {
        try {
            app.getContext().stop();
            app.getContext().start();
            app.setStatus(ApplicationStatus.READY);
            for(Continuation continuation : app.getContinuations()) {
                continuation.resume();
            }
            app.getContinuations().clear();
        } catch (Exception e) {
            app.setStatus(ApplicationStatus.ERROR);
        }
    }
}
