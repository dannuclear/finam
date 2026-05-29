package ru.nuclearius.finam.subscriber;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCallStreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractBackoffObserver<TReq, TResp> extends AbstractControlledObserver<TReq, TResp> {
    private final AtomicInteger retryCount = new AtomicInteger();
    private final long retryDelay;
    private final int maxRetryCount;
    private MaxRetryExceededListener maxExceededListener;

    protected AbstractBackoffObserver() {
        this(5, 10);
    }

    protected AbstractBackoffObserver(long retryDelay, int maxRetryCount) {
        if (retryDelay < 1)
            throw new IllegalArgumentException("retryDelay must be greater than or equal to 1");
        if (maxRetryCount < 1)
            throw new IllegalArgumentException("maxRetryCount must be greater than or equal to 1");
        this.retryDelay = retryDelay;
        this.maxRetryCount = maxRetryCount;
    }

    public void setMaxRetryExceededListener(MaxRetryExceededListener listener) {
        this.maxExceededListener = listener;
    }

    @Override
    protected void doStart() {
        attemptSubscribe("Initial subscribe");
    }

    private void attemptSubscribe(String reason) {
        try {
            subscribe();
        } catch (Exception ex) {
            log.error("{} failed", reason, ex);

            if (isRunning()) {
                reconnect();
            }
        }
    }

    @Override
    public void beforeStart(ClientCallStreamObserver<TReq> requestStream) {
        super.beforeStart(requestStream);
        retryCount.set(0);
    }

    @Override
    public void onError(Throwable t) {
        if (t instanceof StatusRuntimeException) {
            Status status = Status.fromThrowable(t);
            log.debug("Subscriber stream error with status {}: {}", status.getCode(), status.getDescription());
            handleDisconnect(status.getDescription());
        } else {
            log.error("Subscriber stream error", t);
            handleDisconnect("Stream error");
        }
    }

    @Override
    public void onCompleted() {
        handleDisconnect("Stream completed");
    }

    protected void handleDisconnect(String reason) {
        log.warn("Disconnected: {}", reason);
        clearRequestStream();

        if (isRunning()) {
            reconnect();
        }
    }

    private void reconnect() {
        int retry = retryCount.incrementAndGet();

        if (retry > maxRetryCount) {
            log.error("Max retry count {} exceeded. Stopping subscriber", maxRetryCount);
            if (maxExceededListener != null) {
                maxExceededListener.onMaxRetryExceeded();
            }
            stop();
            return;
        }

        long delay = Math.min(60, retry * retryDelay);

        log.info("Reconnect attempt {}/{} in {} sec.", retry, maxRetryCount, delay);

        CompletableFuture.delayedExecutor(delay, TimeUnit.SECONDS).execute(() -> {
            if (!isRunning()) {
                return;
            }

            attemptSubscribe("Reconnect");
        });
    }

    public interface MaxRetryExceededListener {
        void onMaxRetryExceeded();
    }
}
