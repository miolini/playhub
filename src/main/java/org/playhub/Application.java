package org.playhub;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Application {
    private File path;
    private ExecutorService pool;
    private ApplicationStatus status;
    private WebAppContext context;
    private List<Continuation> continuations = new ArrayList<Continuation>();

    public Application(String path, Service service) {
        this.path = new File(path);
        this.pool = service.getPool();
        checkExist();
        if (!isFound()) return;

        context = new WebAppContext();
        context.setWar(path);
        context.setContextPath("/");
        context.setServer(service.getServer());
    }

    public void checkExist() {
        if (!this.path.exists()) {
            if (isReady())
                status = ApplicationStatus.NOTFOUND;
        } else status = null;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public WebAppContext getContext() {
        return context;
    }

    public void setContext(WebAppContext context) {
        this.context = context;
    }

    public boolean isReady() {
        return status == ApplicationStatus.READY;
    }

    public boolean isInit() {
        return status == ApplicationStatus.INIT;
    }

    public void start(Continuation continuation) {
        if (continuation != null && isInit()) continuations.add(continuation);
        if (status == null) {
            status = ApplicationStatus.INIT;
            pool.execute(new ApplicationStarter(this));
        }
    }

    public void stop() {
        pool.execute(new ApplicationStopper(this));
    }

    public boolean isFound() {
        return status != ApplicationStatus.NOTFOUND;
    }

    public List<Continuation> getContinuations() {
        return continuations;
    }
}
