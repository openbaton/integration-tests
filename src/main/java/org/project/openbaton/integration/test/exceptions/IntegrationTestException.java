package org.project.openbaton.integration.test.exceptions;

/**
 * Created by lto on 24/06/15.
 */
public class IntegrationTestException extends Exception{
    public IntegrationTestException() {
        super();
    }

    public IntegrationTestException(String message) {
        super(message);
    }

    public IntegrationTestException(String message, Throwable cause) {
        super(message, cause);
    }
}
