package org.project.openbaton.integration.test.jms;

import org.project.openbaton.catalogue.nfvo.EventEndpoint;
import org.project.openbaton.integration.test.interfaces.WaiterInterface;


/**
 * Created by mob on 31.07.15.
 */
public class JMSWaiter implements WaiterInterface {
    @Override
    public boolean subscribe(EventEndpoint endpoint) {
        //TODO
        return false;
    }

    @Override
    public boolean unSubscribe() {
        //TODO
        return false;
    }

    @Override
    public boolean waitForEvent(int timeOut) {
        //TODO
        return false;
    }
}
