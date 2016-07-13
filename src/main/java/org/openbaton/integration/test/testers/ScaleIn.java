package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.NetworkServiceRecordRestAgent;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by tbr on 15.01.16.
 */

/**
 * This class triggers one scale in on a specified VNFR.
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
    log.info("Start ScaleIn on VNFR type " + vnfrType);
    NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();

    Properties p = Utils.getProperties();
    NetworkServiceRecordRestAgent agent = requestor.getNetworkServiceRecordAgent();

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

    if (!found) log.warn("did not find a VNFR of type " + vnfrType);

    log.debug("--- Triggered ScaleIn on VNFR of type " + vnfrType);
    return param;
  }

  public void setVnfrType(String vnfrType) {
    this.vnfrType = vnfrType;
  }
}
