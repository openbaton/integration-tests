package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.sdk.api.exception.SDKException;

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
    protected Object doWork() throws SDKException {
        NetworkServiceRecord nsr = (NetworkServiceRecord) param;
        //log.debug(" --- Executing DELETE of " + nsr.getId());
        try{
            delete(nsr.getId());
        } catch (SDKException sdkEx) {
            log.error("Exception during deleting of NetworkServiceRecord with id: "+nsr.getId(), sdkEx);
            throw sdkEx;
        }
        // nsr.getDescriptor_reference() no longer needed
        return nsr;
    }
}
