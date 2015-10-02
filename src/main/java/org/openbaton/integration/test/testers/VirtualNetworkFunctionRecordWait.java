package org.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.exceptions.SubscriptionException;
import org.project.openbaton.integration.test.interfaces.Waiter;
import org.project.openbaton.sdk.api.exception.SDKException;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by mob on 02.10.15.
 */
public class VirtualNetworkFunctionRecordWait extends Waiter {

    private String name = VirtualNetworkFunctionRecordWait.class.getSimpleName();
    private String vnfrName;

    public VirtualNetworkFunctionRecordWait(Properties properties) {
        super(properties, VirtualNetworkFunctionRecordWait.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws SDKException, SubscriptionException, InterruptedException {

        NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();
        EventEndpoint eventEndpoint = createEventEndpoint(name, EndpointType.REST);
        String vnfrId=getVnfrIdFromNsr(nsr, getVnfrName());
        eventEndpoint.setVirtualNetworkFunctionId(vnfrId);
        //The eventEndpoint param of EventEndpoint will be set in the RestWaiter or JMSWaiter

        try {
            subscribe(eventEndpoint);
            log.debug(name + ": --- registration complete, start waiting for " + getAction().toString() + " of vnfr with name:" + getVnfrName() + " and id:" + vnfrId + "....");
            if(waitForEvent())
                log.debug(name + ": --- waiting complete for " + getAction().toString() + " of vnfr with name:"+getVnfrName()+" and id:" + vnfrId);
            else log.debug(name + ": --- timeout elapsed for " + getAction().toString() + " of vnfr with name:"+getVnfrName()+" and id:" + vnfrId);
            unSubscribe();

        } catch (SubscriptionException e) {
            log.error("Subscription failed for event " + getAction() + " of vnfr with name:"+getVnfrName()+" and id:" + vnfrId);
            throw e;
        } catch (InterruptedException | SDKException e) {
            log.error("Wait failed for event " + getAction() + " of vnfr with name:"+getVnfrName()+" and id:" + vnfrId);
            throw e;
        }
        return param;
    }

    public void setVnfrName(String name){
        vnfrName=name;
    }

    public String getVnfrName(){
        return vnfrName;
    }

    private String getVnfrIdFromNsr(NetworkServiceRecord networkServiceRecord, String vnfrName){
        if(networkServiceRecord==null)
            throw new NullPointerException("NetworkServiceRecord is null");
        if(vnfrName==null || vnfrName.isEmpty())
            throw new NullPointerException("vnfrName is null or empty");
        for(VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr())
            if(vnfr.getName().equalsIgnoreCase(vnfrName))
                return vnfr.getId();
        throw new NullPointerException("No vnfr found for name: "+vnfrName);
    }
}
