package org.gegolabs.mcp1.protocol;

/**
 * Exception thrown when a capability execution fails.
 */
public class CapabilityException extends Exception{
    /**
     * Constructs a new capability exception with the specified detail message and cause.
     *
     * @param executionFailed the detail message
     * @param e the cause
     */
    public CapabilityException(String executionFailed, Throwable e) {
        super(executionFailed, e);
    }

    /**
     * Constructs a new capability exception with the specified detail message.
     *
     * @param executionFailed the detail message
     */
    public CapabilityException(String executionFailed) {
        super(executionFailed);
    }
}
