package org.playhub;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Service extends AbstractHandler {
    public static final String HEADER_APP_PATH = "X-PlayHub-AppPath";
    private static final String HEADER_WAIT_START = "X-PlayHub-WaitStart";

    private String host = "127.0.0.1";
    private int port = 8080;
    private Logger log = LoggerFactory.getLogger(getClass());
    private Map<String, Application> apps = new HashMap<String, Application>();
    private Server server;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private static final String PROP_HOST = "playhub.host";
    private ApplicationObserver appObserver;

    public void run() {
        try {
            appObserver = new ApplicationObserver();
            pool.execute(appObserver);
            host = System.getProperty(PROP_HOST, host);
            server = new Server(new InetSocketAddress(host, port));
            server.setHandler(this);
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Service service = new Service();
        service.run();
    }

    private Application getApplication(String path) {
        synchronized (apps) {
            Application app = apps.get(path);
            if (app != null && (app.isReady() || app.isInit())) return app;
            try {
                if (app == null) {
                    app = new Application(path, this);
                    appObserver.add(app);
                } else app.checkExist();
                if (!app.isFound()) return app;
                apps.put(path, app);
                return app;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void handle(String path,
                       Request request,
                       HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        String host = request.getHeader("host");
        String appPath = request.getHeader(HEADER_APP_PATH);
        boolean waitStart = Boolean.parseBoolean(request.getHeader(HEADER_WAIT_START));
        Continuation continuation = null;
        if (waitStart) continuation = ContinuationSupport.getContinuation(request);
        Application application = getApplication(appPath);
        application.start(continuation);
        if (waitStart && application.isInit()) {
            continuation.suspend();
            return;
        }
        if (application != null && application.isReady()) {
            application.storeAccess();
            application.getContext().handle
                    (path, request, httpServletRequest, httpServletResponse);
        } else {
            request.setHandled(true);
            httpServletResponse.setStatus(502);
            httpServletResponse.getOutputStream().print("{appstatus:" + application.getStatus() + "}");
        }
    }

    public ExecutorService getPool() {
        return pool;
    }
}
