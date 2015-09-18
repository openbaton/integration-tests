package org.project.openbaton.integration.test.interfaces;

import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.exceptions.IntegrationTestException;
import org.project.openbaton.integration.test.exceptions.SubscriptionException;
import org.project.openbaton.sdk.api.exception.SDKException;

/**
 * Created by mob on 31.07.15.
 */
public interface WaiterInterface {
    void subscribe(EventEndpoint endpoint) throws SubscriptionException, SDKException;
    void unSubscribe() throws SDKException;
    void waitForEvent(int timeOut) throws InterruptedException;
}
