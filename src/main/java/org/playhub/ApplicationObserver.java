package org.playhub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ApplicationObserver implements Runnable {
    private Set<Application> apps = new HashSet<Application>();
    private boolean needStop;
    private static final long SLEEP = 1000;
    private Logger log = LoggerFactory.getLogger(getClass());
    private static final long LIFETIME = 1000 * 60 * 10;

    public void run() {
        while (!needStop) {
            synchronized (apps) {
                for (Application app : apps) {
                    if (!app.isReady()) continue;
                    if (app.isModified()) {
                        log.info(String.format("app %s is modified - restarting", app.getPath().getName()));
                        app.restart();
                    }
                    else if (app.isReady() && app.getTimeFromLastAccess() > LIFETIME) {
                        log.info(String.format("app %s will be shutdown now", app.getPath()));
                        app.stop();
                    }
                }
            }
            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void add(Application app) {
        synchronized (apps) {
            apps.add(app);
        }
    }

    public void remove(Application app) {
        synchronized (apps) {
            apps.remove(app);
        }
    }

    public void stop() {
        needStop = true;
    }
}
