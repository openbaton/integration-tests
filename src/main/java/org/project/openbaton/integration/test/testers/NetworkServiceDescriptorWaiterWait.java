package org.project.openbaton.integration.test.testers;

import org.project.openbaton.integration.test.interfaces.Waiter;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by mob on 31.07.15.
 */
public class NetworkServiceDescriptorWaiterWait extends Waiter {

    private static final String name="NetworkServiceDescriptorWaiterWait";
    public NetworkServiceDescriptorWaiterWait(Properties properties) {
        super(properties, NetworkServiceRecordWaiterWait.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        return null;
    }

    @Override
    protected void handleException(Exception e) {

    }
}
