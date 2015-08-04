package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.integration.test.utils.Tester;

import java.util.Properties;

/**
 * Created by mob on 28.07.15.
 */
public class NetworkServiceRecordDelete extends Tester<NetworkServiceRecord> {

    public NetworkServiceRecordDelete(Properties properties) {
        super(properties, NetworkServiceRecord.class, "", "/ns-records");
    }

    @Override
    protected NetworkServiceRecord prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        NetworkServiceRecord nsr = (NetworkServiceRecord) param;
        log.debug(" --- Executing DELETE of " + nsr.getId());
        delete(nsr.getId());
        // nsr.getDescriptor_reference() no longer needed
        return nsr;
    }

    @Override
    protected void handleException(Exception e) {
        e.printStackTrace();
        log.error("Exception NetworkServiceRecordDelete: there was an exception: " + e.getMessage());
    }
}
