package org.project.openbaton.integration.test.interfaces;

import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.sdk.api.exception.SDKException;

/**
 * Created by mob on 31.07.15.
 */
public interface WaiterInterface {
    void subscribe(EventEndpoint endpoint) throws SubscriptionException, SDKException;
    void unSubscribe() throws SDKException;
    boolean waitForEvent(int timeOut) throws InterruptedException;
}
