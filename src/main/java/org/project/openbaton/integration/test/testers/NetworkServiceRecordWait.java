package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.exceptions.IntegrationTestException;
import org.project.openbaton.integration.test.interfaces.Waiter;
import org.project.openbaton.sdk.api.exception.SDKException;

import java.io.*;
import java.util.Properties;

/**
 * Created by mob on 28.07.15.
 */
public class NetworkServiceRecordWait extends Waiter {

    private static final String name="NetworkServiceRecordWait";
    private Action action;

    public NetworkServiceRecordWait(Properties properties) {
        super(properties, NetworkServiceRecordWait.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws IntegrationTestException {
        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setEvent(getAction());
        NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();
        eventEndpoint.setNetworkServiceId(nsr.getId());
        //The eventEndpoint param of EventEndpoint will be set in the RestWaiter or JMSWaiter
        eventEndpoint.setName(name);
        eventEndpoint.setType(EndpointType.REST);

        if(subscribe(eventEndpoint))
        {
            log.debug(name + ": --- registration complete, start waiting for "+getAction().toString()+" of nsr with id:"+nsr.getId()+"....");
            if(waitForEvent()){
                if(unSubscribe())
                {
                    log.debug(name + ": --- unSubscription complete");
                    return param;
                }
                else {
                    log.error("Wait failed for event "+getAction() +" of nsr with id: "+nsr.getId());
                    throw new IntegrationTestException("Waiter failed the unsubscription");
                }
            }else {
                log.error("Wait failed for event "+getAction() +" of nsr with id: "+nsr.getId());
                throw new IntegrationTestException("Waiter failed the wait");
            }
        }
        else {
            log.error("Subscription failed to the eventPoint with action "+getAction() +" and nsr with id: "+nsr.getId());
            throw new IntegrationTestException("Waiter failed the subscription");
        }
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action a) {
        action=a;
    }
}
