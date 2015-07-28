package org.project.openbaton.integration.test.testers;

import org.project.openbaton.integration.test.utils.Tester;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by mob on 28.07.15.
 */
public class WaiterWait extends Tester {

    public WaiterWait(Properties properties) {
        super(properties, WaiterWait.class, "" , "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {

        log.debug("WaiterWait starts to wait...");
        try {
            Thread.sleep(5000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        log.debug("WaiterWait ended the wait and forward the param: "+param.toString());
        return param;
    }

    @Override
    protected void handleException(Exception e) {
        e.printStackTrace();
        log.error("there was an exception: " + e.getMessage());
    }
}
