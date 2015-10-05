package org.openbaton.integration.test.interfaces;

import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.jms.JMSWaiter;
import org.openbaton.integration.test.rest.RestWaiter;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;

import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private Action action;

    public Waiter(Properties properties, Class aClass, String filePath, String basePath) {
        super(properties, aClass, filePath, basePath);
    }

    public void subscribe(EventEndpoint eventEndpoint) throws SubscriptionException, SDKException {
        if (eventEndpoint == null)
            throw new NullPointerException("EventEndpoint is null");
        if (eventEndpoint.getType() == EndpointType.JMS)
            waiter = new JMSWaiter();
        else if (eventEndpoint.getType() == EndpointType.REST)
            waiter = new RestWaiter(eventEndpoint.getName(), requestor, mapper, log);
        waiter.subscribe(eventEndpoint);
    }

    protected EventEndpoint createEventEndpoint(String name, EndpointType type){
        EventEndpoint eventEndpoint = new EventEndpoint();
        eventEndpoint.setEvent(getAction());
        eventEndpoint.setName(name);
        eventEndpoint.setType(type);
        return eventEndpoint;
    }
    public boolean waitForEvent() throws InterruptedException {
        if(waiter==null)
            throw new NullPointerException("Waiter is null (use subscribe before waitForEvent)");
        return waiter.waitForEvent(getTimeout());
    }

    public void unSubscribe() throws SDKException {
        if (waiter == null)
            throw new NullPointerException("Waiter is null (use subscribe and waitForEvent before unSubscribe)");
        waiter.unSubscribe();
    }

    public String getPayload(){
        return waiter.getPayload();
    }
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action a) {
        action=a;
    }
}
