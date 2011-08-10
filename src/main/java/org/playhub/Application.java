package org.playhub;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.server.Server;
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
    private long lastModified;
    private long lastAccess;
    private Object timeFromLastAccess;
    private Server server;

    public Application(String path, Service service) {
        this.path = new File(path);
        this.pool = service.getPool();
        checkExist();
        if (!isFound()) return;
        server = service.getServer();
    }

    private void openApplication() {
        context = new WebAppContext();
        context.setWar(path.getAbsolutePath());
        context.setContextPath("/");
        context.setServer(server);
    }

    public void checkExist() {
        if (!this.path.exists()) {
            if (isReady())
                status = ApplicationStatus.NOTFOUND;
        } else {
            if (status == ApplicationStatus.NOTFOUND) status = null;
            lastModified = path.lastModified();
        }
    }

    public boolean isModified() {
        return path.lastModified() != lastModified;
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
        if (context == null) openApplication();
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
        if (continuation != null && !isReady())
            continuations.add(continuation);
        if (status == null || status == ApplicationStatus.STOPPED) {
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

    public void restart() {
        this.lastModified = path.lastModified();
        status = ApplicationStatus.INIT;
        pool.execute(new ApplicationRestarter(this));
    }

    public void storeAccess() {
        if (isReady()) lastAccess = System.currentTimeMillis();
    }

    public long getTimeFromLastAccess() {
        return lastAccess > 0 ? System.currentTimeMillis() - lastAccess : 0;
    }
}
