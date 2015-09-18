package org.project.openbaton.integration.test.exceptions;

/**
 * Created by mob on 18.09.15.
 */
public class SubscriptionException extends IntegrationTestException {
    public SubscriptionException() {
        super();
    }

    public SubscriptionException(String message) {
        super(message);
    }

    public SubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
