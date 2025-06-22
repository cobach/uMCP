package org.gegolabs.mcp1.protocol;

import java.util.concurrent.CompletableFuture;

/**
 * Interfaz para capabilities asíncronas
 * @param <I> Tipo de input
 * @param <O> Tipo de output
 */
public interface AsyncCapability<I, O> extends Capability<I, O> {

    /**
     * Ejecución asíncrona (retorna inmediatamente)
     * Esta implementación debe ser proporcionada por las clases que implementen esta interfaz
     */
    @Override
    CompletableFuture<O> executeAsync(I input);
}