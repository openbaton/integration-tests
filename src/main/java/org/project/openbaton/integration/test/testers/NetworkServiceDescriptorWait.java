package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.interfaces.Waiter;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by mob on 31.07.15.
 */
public class NetworkServiceDescriptorWait extends Waiter {

    private static final String name="NetworkServiceDescriptorWait";
    private int nsrCreated=0;

    public NetworkServiceDescriptorWait(Properties properties) {
        super(properties, NetworkServiceRecordWait.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {

        /**Endpoint creation**/
        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setEvent(Action.RELEASE_RESOURCES_FINISH);
        NetworkServiceDescriptor nsd = (NetworkServiceDescriptor) param;
        //eventEndpoint.setNetworkServiceId(nsd.getId());
        //The eventEndpoint param of EventEndpoint will be set in the RestWaiter or JMSWaiter
        eventEndpoint.setName(name);
        eventEndpoint.setType(EndpointType.REST);
        /*********************/


        if(this.subscribe(eventEndpoint))
        {
            log.debug(name + ": --- Registration complete, start waiting for deleting the nsd with id:"+nsd.getId());
            int counter=0;
            while(counter<nsrCreated){
                if(this.waitForEvent())
                    counter++;
                else break;
            }
            if(this.unSubscribe())
            {
                log.debug(name + ": --- unSubscription complete it's time to delete the nsd with id :"+ nsd.getId());
            }
        }
        log.debug(name + ": --- forward the param: " + param.toString());
        return nsd.getId();
    }

    @Override
    protected void handleException(Exception e) {

    }

    public void setNSRCreated(int nSRCreated){
        nsrCreated=nSRCreated;
    }
}
