package bflow.receipts.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Long-polls a single SQS queue on its own dedicated daemon thread.
 *
 * <p>Deliberately not built on {@code @Scheduled}: every {@code
 * @Scheduled} task in this application shares one small
 * fixed-size thread pool (see {@code SchedulingErrorConfiguration},
 * pool size 5, used by cron-style jobs like {@code
 * StoredFileCleanupTask}). A long-poll {@code receiveMessage} call
 * blocks for up to {@code waitTimeSeconds} on every iteration; two
 * of these running continuously would permanently occupy 2 of those
 * 5 threads and starve the actual cron jobs. Implementing {@link
 * SmartLifecycle} instead gives each worker its own thread, started
 * and stopped cleanly with the application context.</p>
 */
@Slf4j
public abstract class AbstractSqsPollingWorker implements SmartLifecycle {

    /** SQS client shared with the rest of the messaging beans. */
    private final SqsClient sqsClient;

    /** URL of the queue this worker polls. */
    private final String queueUrl;

    /** Long-poll wait time, in seconds (max 20 per the SQS API). */
    private final int waitTimeSeconds;

    /** Maximum messages fetched per {@code receiveMessage} call. */
    private final int maxMessages;

    /** Seconds to pause after a poll failure before retrying. */
    private final int errorBackoffSeconds;

    /** Whether the worker's poll loop should keep running. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** The dedicated thread running {@link #pollLoop()}. */
    private Thread workerThread;

    /** Milliseconds in one second, used to convert a backoff. */
    private static final long MILLIS_PER_SECOND = 1000L;

    /**
     * Creates a new polling worker.
     *
     * @param client the SQS client to poll with
     * @param queue the URL of the queue to poll
     * @param waitSeconds long-poll wait time, in seconds
     * @param maxMsgs maximum messages per receive call
     * @param backoffSeconds pause after a poll failure, in seconds
     */
    protected AbstractSqsPollingWorker(
            final SqsClient client,
            final String queue,
            final int waitSeconds,
            final int maxMsgs,
            final int backoffSeconds
    ) {
        this.sqsClient = client;
        this.queueUrl = queue;
        this.waitTimeSeconds = waitSeconds;
        this.maxMessages = maxMsgs;
        this.errorBackoffSeconds = backoffSeconds;
    }

    @Override
    public final void start() {
        if (running.compareAndSet(false, true)) {
            workerThread = new Thread(this::pollLoop, workerName());
            workerThread.setDaemon(true);
            workerThread.start();
        }
    }

    @Override
    public final void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    @Override
    public final boolean isRunning() {
        return running.get();
    }

    private void pollLoop() {
        log.info("{} started, polling {}", workerName(), queueUrl);

        while (running.get()) {
            try {
                List<Message> messages = sqsClient.receiveMessage(
                        ReceiveMessageRequest.builder()
                                .queueUrl(queueUrl)
                                .waitTimeSeconds(waitTimeSeconds)
                                .maxNumberOfMessages(maxMessages)
                                .build()
                ).messages();

                for (Message message : messages) {
                    if (!running.get()) {
                        break;
                    }
                    processMessage(message);
                }
            } catch (RuntimeException ex) {
                if (!running.get()) {
                    break;
                }
                log.error(
                        "{} poll failed, backing off {}s",
                        workerName(), errorBackoffSeconds, ex
                );
                sleepQuietly(errorBackoffSeconds);
            }
        }

        log.info("{} stopped", workerName());
    }

    private void processMessage(final Message message) {
        boolean handled;
        try {
            handled = handle(message);
        } catch (RuntimeException ex) {
            log.error(
                    "{} failed to process message {}; leaving it for "
                            + "redelivery",
                    workerName(), message.messageId(), ex
            );
            handled = false;
        }

        if (handled) {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        }
        // Otherwise the message is left in place: it becomes visible
        // again after the queue's visibility timeout and is retried.
        // A redrive policy / dead-letter queue (configured at the
        // infra level, see infra/textract-ocr/01-create-queues.sh)
        // bounds how many times that can happen.
    }

    private void sleepQuietly(final int seconds) {
        try {
            Thread.sleep(seconds * MILLIS_PER_SECOND);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * A short, log-friendly name for this worker.
     *
     * @return the worker's name
     */
    protected abstract String workerName();

    /**
     * Handles a single message.
     *
     * @param message the message received from the queue
     * @return true if the message was fully handled and should be
     *         deleted; false to leave it for redelivery (treat as a
     *         transient failure)
     */
    protected abstract boolean handle(Message message);
}
