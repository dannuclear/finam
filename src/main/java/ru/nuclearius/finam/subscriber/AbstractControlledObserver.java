package ru.nuclearius.finam.subscriber;

import java.util.concurrent.atomic.AtomicBoolean;

import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractControlledObserver<TReq, TResp> implements ClientResponseObserver<TReq, TResp> {

    private volatile ClientCallStreamObserver<TReq> requestStream;
    private final AtomicBoolean running = new AtomicBoolean(false);

    protected abstract void subscribe();

    public void start() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Subscriber already started");
            return;
        }

        log.info("Starting subscriber");
        doStart();
    }

    protected void doStart() {
        subscribe();
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            log.warn("Subscriber already stopped");
            return;
        }

        log.info("Stopping subscriber");

        cancel("stop");
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void beforeStart(ClientCallStreamObserver<TReq> requestStream) {
        this.requestStream = requestStream;
    }

    protected ClientCallStreamObserver<TReq> getRequestStream() {
        return requestStream;
    }

    protected void clearRequestStream() {
        requestStream = null;
    }

    protected void cancel (String message) {
        ClientCallStreamObserver<TReq> rs = requestStream;
        clearRequestStream();

        if (rs != null) {
            rs.cancel(message, Status.CANCELLED.asException());
        }
    }
}
