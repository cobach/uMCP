package org.gegolabs.mcp.protocol;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Base interface for capabilities with asynchronous support.
 * 
 * @param <I> Input type
 * @param <O> Output type
 */
public interface Capability<I, O> {

    /**
     * Initializes the capability.
     * This method is called when the capability is registered with the server.
     * Implementations should perform any necessary setup here.
     *
     * @throws CapabilityException if initialization fails
     */
    default void initialize() throws CapabilityException {
        // Default implementation does nothing
    }

    /**
     * Shuts down the capability.
     * This method is called when the capability is being removed or the server is shutting down.
     * Implementations should perform any necessary cleanup here.
     *
     * @throws CapabilityException if shutdown fails
     */
    default void shutdown() throws CapabilityException {
        // Default implementation does nothing
    }

    /**
     * Synchronous execution (blocks until completion).
     * By default, calls executeAsync().get()
     *
     * @param input the input for the capability
     * @return the output of the capability execution
     * @throws CapabilityException if the execution fails
     */
    default O execute(I input) throws CapabilityException {
        try {
            return executeAsync(input).get();
        } catch (Exception e) {
            if (e.getCause() instanceof CapabilityException) {
                throw (CapabilityException) e.getCause();
            }
            throw new CapabilityException("Execution failed", e);
        }
    }

    /**
     * Asynchronous execution (returns immediately).
     * By default, executes the sync method in another thread.
     *
     * @param input the input for the capability
     * @return a CompletableFuture that will complete with the output of the capability execution
     */
    default CompletableFuture<O> executeAsync(I input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(input);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, getExecutor());
    }

    /**
     * Gets the executor for async operations.
     * By default, uses ForkJoinPool.
     *
     * @return the executor to use for async operations
     */
    default Executor getExecutor() {
        return ForkJoinPool.commonPool();
    }

    /**
     * Gets the default timeout in seconds.
     *
     * @return the default timeout in seconds
     */
    /*default long getTimeoutSeconds() {
        return 30L;
    }*/
}
