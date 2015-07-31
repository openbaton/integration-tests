package org.project.openbaton.integration.test.interfaces;

import org.project.openbaton.catalogue.nfvo.EventEndpoint;

/**
 * Created by mob on 31.07.15.
 */
public interface WaiterInterface {
    boolean subscribe(EventEndpoint endpoint);
    boolean unSubscribe();
    boolean waitForEvent(int timeOut);

}
