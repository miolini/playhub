package org.playhub;

public class ApplicationStopper implements Runnable {
    private Application app;

    public ApplicationStopper(Application app) {
        this.app = app;
    }

    public void run() {
        try {
            app.setStatus(ApplicationStatus.STOPPING);
            app.getContext().stop();
            app.setStatus(ApplicationStatus.STOPPED);
        } catch (Exception e) {
            app.setContext(null);
        }
    }
}
