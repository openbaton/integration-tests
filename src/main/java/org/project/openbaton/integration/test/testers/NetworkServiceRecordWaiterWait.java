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
public class NetworkServiceRecordWaiterWait extends Waiter {

    private static final String name="NetworkServiceRecordWaiterWait";
    public NetworkServiceRecordWaiterWait(Properties properties) {
        super(properties, NetworkServiceRecordWaiterWait.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {

        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setEvent(Action.INSTANTIATE_FINISH);
        NetworkServiceRecord nsr = (NetworkServiceRecord) param;
        eventEndpoint.setNetworkServiceId(nsr.getId());
        //The eventEndpoint param of EventEndpoint will be set in the RestWaiter or JMSWaiter
        eventEndpoint.setName(name);
        eventEndpoint.setType(EndpointType.REST);

        if(this.subscribe(eventEndpoint))
        {
            log.debug(name + ": registration complete, start waiting...");
            if(this.waitForEvent())
                if(this.unSubscribe())
                {
                    log.debug(name + ": unSubscription complete");
                    return param;
                }
        }
        log.debug(name + ": forward the param: " + param.toString());
        return param;
    }

    @Override
    protected void handleException(Exception e) {
        log.error("Exception "+name+" : there was an exception: " + e.getMessage());
        e.printStackTrace();
    }

}
