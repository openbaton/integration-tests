package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.Status;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.NetworkServiceRecordRestAgent;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by tbr on 20.01.16.
 */

/**
 * This class tests if the expected number of VNFC-Instances of a VNFR exist and
 * if the VNFR is in an active state.
 * After a sclaing operation it passes the updated NSR to the next tester!
 * Therefore it should always be executed after the VirtualNetworkFunctionRecordWait of a
 * scaling operation finished.
 */
public class ScalingTester extends Tester {

    private String vnfrType = "";
    private int vnfcCount = 0;

    public ScalingTester(Properties properties) {
        super(properties, ScaleOut.class, "", "");
    }

    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        log.info("Start ScalingTester");
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

                Status state = getVNFRState(nsr.getId(), vnfr.getId());
                if (!state.equals(Status.ACTIVE))
                    throw new IntegrationTestException("State of VNFR "+vnfr.getName()+" of type "+vnfr.getType()+" is not ACTIVE but "+state+".");

                int numInstances = getNumberOfVNFCInstances(nsr.getId(), vnfr.getId());
                log.info("Found " + numInstances + " VNFC instance/s of VNFR "+vnfr.getType()+" with id "+vnfr.getId());
                if (numInstances == vnfcCount) {
                    log.info("ScalingTester finished successfully");
                } else {
                    log.error("Expected number of VNFCInstances was "+vnfcCount+" but ScalingTester found "+numInstances);
                    throw new IntegrationTestException("ScalingTester did not finish successfully");
                }
            }
        }
        if (!found)
            log.warn("did not find a VNFR of type " + vnfrType);

        // get the updated nsr
        NetworkServiceRecord nsrUpdated = agent.findById(nsr.getId());
        if (nsrUpdated == null)
            log.warn("Could not retrieve the updated NSR. This may cause errors in following tasks");

        return nsrUpdated;
    }

    private int getNumberOfVNFCInstances(String nsrId, String vnfrId) throws IOException, SDKException, IntegrationTestException {
        int num = 0;
        Properties p = Utils.getProperties();
        NetworkServiceRecordRestAgent agent = new NetworkServiceRecordRestAgent(p.getProperty("nfvo-usr"),
                p.getProperty("nfvo-pwd"),
                p.getProperty("nfvo-ip"),
                p.getProperty("nfvo-port"),
                "/ns-records/" + nsrId + "/vnfrecords",
                "1");
        VirtualNetworkFunctionRecord vnfr = (VirtualNetworkFunctionRecord) agent.requestGet(vnfrId, VirtualNetworkFunctionRecord.class);

        for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
                num+=vdu.getVnfc_instance().size();
        }
        return num;
    }

    private Status getVNFRState(String nsrId, String vnfrId) throws IOException, SDKException {
        Properties p = Utils.getProperties();
        NetworkServiceRecordRestAgent agent = new NetworkServiceRecordRestAgent(p.getProperty("nfvo-usr"),
                p.getProperty("nfvo-pwd"),
                p.getProperty("nfvo-ip"),
                p.getProperty("nfvo-port"),
                "/ns-records/" + nsrId + "/vnfrecords",
                "1");
        VirtualNetworkFunctionRecord vnfr = (VirtualNetworkFunctionRecord) agent.requestGet(vnfrId, VirtualNetworkFunctionRecord.class);
        return vnfr.getStatus();
    }

    public void setVnfrType(String vnfrType) {
        this.vnfrType = vnfrType;
    }

    public void setVnfcCount(String vnfcCount) {
        try {
            this.vnfcCount = Integer.parseInt(vnfcCount);
        } catch (NumberFormatException e) {
            log.warn("The field vnfc-count in the ini file is not a number. This will probably cause this test to fail.");
        }
    }
}
