package org.playhub;

import org.eclipse.jetty.continuation.Continuation;

public class ApplicationStarter implements Runnable {
    private Application app;

    public ApplicationStarter(Application app) {
        this.app = app;
    }

    public void run() {
        try {
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
