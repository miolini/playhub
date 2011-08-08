package org.playhub;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class Service extends AbstractHandler {
    private String host = "127.0.0.1";
    private int port = 8080;
    private Logger log = LoggerFactory.getLogger(getClass());
    private Map<String, WebAppContext> virtualHosts = new HashMap<String, WebAppContext>();

    public void run() {
        try {
            Server server = new Server(new InetSocketAddress(host, port));

            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setVirtualHosts(new String[]{"headaward.com"});
            webAppContext.setWar("c:/work/startups/apps/headaward.war");
            webAppContext.setContextPath("/");
            webAppContext.setServer(server);
            webAppContext.start();
            virtualHosts.put("headaward.com", webAppContext);

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

    public void handle(String path,
                       Request request,
                       HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse)
            throws IOException, ServletException {

        String host = request.getHeader("host");
        WebAppContext context = virtualHosts.get(host);
        if (context != null) {
            context.handle(path, request, httpServletRequest, httpServletResponse);
        } else {
            request.setHandled(true);
            for (Object headerName : Collections.list(request.getHeaderNames())) {
                for (Object headerValue : Collections.list(request.getHeaders(headerName.toString()))) {
                    httpServletResponse.getOutputStream().
                            print(String.format("%s: %s", headerName, headerValue) + "\n");
                }
            }
        }
    }
}
