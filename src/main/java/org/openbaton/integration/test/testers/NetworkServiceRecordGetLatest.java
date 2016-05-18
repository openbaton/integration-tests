package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.util.AbstractRestAgent;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by tbr on 15.02.16.
 */
public class NetworkServiceRecordGetLatest extends Tester {

    /**
     * @param properties : IntegrationTest properties containing:
     *                   nfvo-usr
     *                   nfvo-pwd
     *                   nfvo-ip
     *                   nfvo-port
     */
    public NetworkServiceRecordGetLatest(Properties properties) {
        super(properties, NetworkServiceRecordGetLatest.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        NetworkServiceRecord nsr = (NetworkServiceRecord) param;
        if (nsr == null) {
            log.error("The passed NSR is null. This task needs to be placed behind a task that passes a NSR.");
            throw new NullPointerException("The passed NSR was null.");
        }
        log.debug("Try retrieving the latest state of the NSR with id "+nsr.getId());
        AbstractRestAgent<NetworkServiceRecord> agent = this.requestor.abstractRestAgent(NetworkServiceRecord.class, "/ns-records");
        nsr = agent.findById(nsr.getId());
        log.debug("The latest NSR is: \n"+nsr);
        return nsr;
    }

}
