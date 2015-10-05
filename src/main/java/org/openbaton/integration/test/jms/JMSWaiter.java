package org.openbaton.integration.test.jms;

import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.interfaces.WaiterInterface;
import org.openbaton.sdk.api.exception.SDKException;


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
    public boolean waitForEvent(int timeOut) throws InterruptedException {
        return false;
    }

    @Override
    public Action getAction() {
        return null;
    }

    @Override
    public String getPayload() {
        return null;
    }
}
