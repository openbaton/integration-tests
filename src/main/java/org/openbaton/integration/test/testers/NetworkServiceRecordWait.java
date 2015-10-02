package org.openbaton.integration.test.testers;

import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.interfaces.Waiter;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.integration.test.exceptions.SubscriptionException;

import java.io.*;
import java.util.Properties;

/**
 * Created by mob on 28.07.15.
 */
public class NetworkServiceRecordWait extends Waiter {

    private String name = NetworkServiceRecordWait.class.getSimpleName();

    public NetworkServiceRecordWait(Properties properties) {
        super(properties, NetworkServiceRecordWait.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws SDKException, SubscriptionException, InterruptedException {

        NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();

        EventEndpoint eventEndpoint = createEventEndpoint(name,EndpointType.REST);
        eventEndpoint.setNetworkServiceId(nsr.getId());
        //The eventEndpoint param of EventEndpoint will be set in the RestWaiter or JMSWaiter

        try {
            subscribe(eventEndpoint);
            log.debug(name + ": --- registration complete, start waiting for " + getAction().toString() + " of nsr with id:" + nsr.getId() + "....");
            if(waitForEvent())
                log.debug(name + ": --- waiting complete for " + getAction().toString() + " of nsr with id:" + nsr.getId());
            else log.debug(name + ": --- timeout elapsed for " + getAction().toString() + " of nsr with id:" + nsr.getId());
            unSubscribe();

        } catch (SubscriptionException e) {
            log.error("Subscription failed for event " + getAction() + " nsr id: " + nsr.getId() + " nsr name: " + nsr.getName());
            throw e;
        } catch (InterruptedException | SDKException e) {
            log.error("Wait failed for event " + getAction() + " nsr id: " + nsr.getId() + " nsr name: " + nsr.getName());
            throw e;
        }
        return param;
    }
}
