package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.interfaces.Waiter;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.NetworkServiceRecordRestAgent;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.Set;

/**
 * Created by tbr on 15.01.16.
 */
public class ScaleIn extends Tester {

    private String vnfrType = "";

    public ScaleIn(Properties properties) {
        super(properties, ScaleIn.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        Thread.sleep(8000);
        log.info("~~start ScaleIn on VNFR type " + vnfrType+"~~");
        NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();

        Properties p = Utils.getProperties();
        NetworkServiceRecordRestAgent agent = new NetworkServiceRecordRestAgent(p.getProperty("nfvo-usr"),
                p.getProperty("nfvo-pwd"),
                p.getProperty("nfvo-ip"),
                p.getProperty("nfvo-port"),
                "/ns-records",
                "1");

        boolean found = false;
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
            if (vnfr.getType().equals(vnfrType)) {
                found = true;
                try {
                    agent.deleteVNFCInstance(nsr.getId(), vnfr.getId());
                } catch (SDKException e) {
                    log.warn("Exception while triggering the scale in: " + e.getMessage());
                }
            }
        }

        if (!found)
            log.warn("did not find a VNFR of type " + vnfrType);

        return param;
    }

    public void setVnfrType(String vnfrType) {
        this.vnfrType = vnfrType;
    }
}
