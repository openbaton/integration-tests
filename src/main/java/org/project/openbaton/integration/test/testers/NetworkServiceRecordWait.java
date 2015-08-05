package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.interfaces.Waiter;

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
    protected Object doWork() throws Exception {
        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setEvent(getAction());
        NetworkServiceRecord nsr = (NetworkServiceRecord) param;
        eventEndpoint.setNetworkServiceId(nsr.getId());
        //The eventEndpoint param of EventEndpoint will be set in the RestWaiter or JMSWaiter
        eventEndpoint.setName(name);
        eventEndpoint.setType(EndpointType.REST);

        if(subscribe(eventEndpoint))
        {
            log.debug(name + ": --- registration complete, start waiting for "+getAction().toString()+" of nsr with id:"+nsr.getId()+"....");
            if(waitForEvent())
                if(unSubscribe())
                {
                    //log.debug(name + ": --- unSubscription complete");
                    return param;
                }
        }
        //log.debug(name + ": --- forward the param: " + param.toString());
        return param;
    }
    @Override
    protected void handleException(Exception e) {
        log.error("Exception " + name + " : there was an exception: " + e.getMessage());
        e.printStackTrace();
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action a) {
        action=a;
    }
}
