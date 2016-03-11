package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.api.rest.NetworkServiceRecordRestAgent;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by tbr on 25.01.16.
 */
public class VNFRStatusTester extends Tester {
    private String vnfrType = "";
    private Status status = null;

    public VNFRStatusTester(Properties properties) {
        super(properties, VNFRStatusTester.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        log.info("Start VNFRStatusTester on VNFR type "+vnfrType);
        NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();

        Properties p = Utils.getProperties();
        NetworkServiceRecordRestAgent agent = new NetworkServiceRecordRestAgent(p.getProperty("nfvo-usr"),
                p.getProperty("nfvo-pwd"),
                p.getProperty("nfvo-ip"),
                p.getProperty("nfvo-port"),
                "ns-records",
                "1");

        if (status == null)
            throw new IntegrationTestException("Status to test is not declared. Specify it in the .ini file.");
        boolean found = false;
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
            if (vnfr.getType().equals(vnfrType)) {
                found = true;
                log.debug("The status of the VNFR of type "+vnfrType+" is "+vnfr.getStatus());
                if (!vnfr.getStatus().equals(status)) {
                    log.error("The status of VNFR " + vnfr.getName() + " of type " + vnfr.getType() + " is not " + status + " but " + vnfr.getStatus() + ".");
                    throw new IntegrationTestException("The status of VNFR " + vnfr.getName() + " of type " + vnfr.getType() + " is not " + status + " but " + vnfr.getStatus() + ".");
                }
            }
        }

        if (!found)
            log.warn("did not find a VNFR of type "+vnfrType);

        log.debug("--- VNFRStatusTester finished successfully");
        return param;
    }

    public void setVnfrType(String vnfrType) {
        this.vnfrType = vnfrType;
    }
    public void setStatus(String status) {
        try {
            this.status = Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.error("The status "+status+" does not exist. Please change it in the .ini file.");
        }
    }
}
