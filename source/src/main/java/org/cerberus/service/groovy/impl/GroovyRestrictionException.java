package org.cerberus.service.groovy.impl;

/**
 * Exception thrown, when a Groovy script tries to do something it is not
 * allowed to do.
 *
 * @author Kai Schwierczek
 */
public class GroovyRestrictionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later
     * retrieval by the {@link #getMessage()} method.
     */
    public GroovyRestrictionException(String message) {
        super(message);
    }
}
