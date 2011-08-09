package org.playhub;

import org.eclipse.jetty.continuation.Continuation;

public class ApplicationStatusListener {
    private Continuation continuation;

    public ApplicationStatusListener(Continuation continuation) {
        this.continuation = continuation;
    }

    public void statusChanged(Application app, ApplicationStatus oldStatus) {

    }
}
