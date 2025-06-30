package org.gegolabs.mcp.protocol;

/**
 * Interface for synchronous capabilities.
 * 
 * @param <I> Input type
 * @param <O> Output type
 */
public interface SyncCapability<I, O> extends Capability<I, O> {

    /**
     * Synchronous execution (blocks until completion).
     * This implementation must be provided by classes that implement this interface.
     *
     * @param input the input for the capability
     * @return the output of the capability execution
     * @throws CapabilityException if the execution fails
     */
    @Override
    O execute(I input) throws CapabilityException;
}
