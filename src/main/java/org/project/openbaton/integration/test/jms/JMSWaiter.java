package org.project.openbaton.integration.test.jms;

import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.exceptions.IntegrationTestException;
import org.project.openbaton.integration.test.exceptions.SubscriptionException;
import org.project.openbaton.integration.test.interfaces.WaiterInterface;
import org.project.openbaton.sdk.api.exception.SDKException;


/**
 * Created by mob on 31.07.15.
 */
public class JMSWaiter implements WaiterInterface {
    @Override
    public void subscribe(EventEndpoint endpoint) throws SubscriptionException, SDKException {

    }

    @Override
    public void unSubscribe() throws SDKException {

    }

    @Override
    public void waitForEvent(int timeOut) throws InterruptedException {

    }
}
