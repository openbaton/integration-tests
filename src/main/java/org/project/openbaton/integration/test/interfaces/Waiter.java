package org.project.openbaton.integration.test.interfaces;

import org.project.openbaton.catalogue.nfvo.EndpointType;
import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.jms.JMSWaiter;
import org.project.openbaton.integration.test.rest.RestWaiter;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.sdk.api.exception.SDKException;

import java.util.Properties;

/**
 * Created by mob on 31.07.15.
 */
public abstract class Waiter extends Tester {
    /**
     * @param properties : IntegrationTest properties containing:
     *                   nfvo-usr
     *                   nfvo-pwd
     *                   nfvo-ip
     *                   nfvo-port
     * @param aClass     : example VimInstance.class
     * @param filePath   : example "/etc/json_file/vim_instances/vim-instance.json"
     * @param basePath
     */
    private int timeout;

    private WaiterInterface waiter;

    public Waiter(Properties properties, Class aClass, String filePath, String basePath) {
        super(properties, aClass, filePath, basePath);
        waiter=null;
    }

    public boolean subscribe(EventEndpoint eventEndpoint) {
        if(eventEndpoint!=null){
            if(eventEndpoint.getType()==EndpointType.JMS)
                waiter = new JMSWaiter();
            else if(eventEndpoint.getType()==EndpointType.REST)
                waiter = new RestWaiter(eventEndpoint.getName(),requestor,mapper,log);
        }
        else throw new NullPointerException("EventEndpoint is null");

        return waiter.subscribe(eventEndpoint);
    }

    public boolean waitForEvent() {
        if(waiter==null)
            throw new NullPointerException("Waiter is null (use subscribe before waitForEvent)");
        return waiter.waitForEvent(getTimeout());
    }

    public boolean unSubscribe() {
        if(waiter==null)
            throw new NullPointerException("Waiter is null (use subscribe and waitForEvent before unSubscribe)");
        return waiter.unSubscribe();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
