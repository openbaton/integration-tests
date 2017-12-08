/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openbaton.integration.test.testers;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Properties;
import org.ini4j.Profile;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.interfaces.Waiter;
import org.openbaton.sdk.api.exception.SDKException;

/**
 * Created by mob on 02.10.15.
 *
 * <p>Class used to wait for an event on VirtualNetworkFunctionRecord level.
 */
public class VirtualNetworkFunctionRecordWait extends Waiter {

  private String name = VirtualNetworkFunctionRecordWait.class.getSimpleName();
  private String vnfrType;

  public VirtualNetworkFunctionRecordWait(Properties properties) throws FileNotFoundException {
    super(properties, VirtualNetworkFunctionRecordWait.class);
    this.setAbstractRestAgent(requestor.getVirtualNetworkFunctionDescriptorRestAgent());
  }

  @Override
  protected Serializable prepareObject() {
    return null;
  }

  @Override
  protected Object doWork()
      throws SDKException, InterruptedException, IntegrationTestException, FileNotFoundException {

    NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();
    EventEndpoint eventEndpoint = createEventEndpoint(name);
    String vnfrId = getVnfrIdFromNsr(nsr, getVnfrType());
    eventEndpoint.setVirtualNetworkFunctionId(vnfrId);
    //The eventEndpoint param of EventEndpoint will be set in the RestWaiter

    try {
      subscribe(eventEndpoint);
      log.debug(
          name
              + ": --- registration complete, start waiting for "
              + getAction().toString()
              + " of vnfr with type:"
              + getVnfrType()
              + " and id:"
              + vnfrId
              + "....");
      if (waitForEvent())
        log.debug(
            name
                + ": --- waiting complete for "
                + getAction().toString()
                + " of vnfr with type:"
                + getVnfrType()
                + " and id:"
                + vnfrId);
      else {
        log.error(
            name
                + ": --- timeout elapsed for "
                + getAction().toString()
                + " of vnfr with type:"
                + getVnfrType()
                + " and id:"
                + vnfrId);
        throw new IntegrationTestException("Timeout elapsed.");
      }
      unSubscribe();

    } catch (SubscriptionException e) {
      log.error(
          "Subscription failed for event "
              + getAction()
              + " of vnfr with type:"
              + getVnfrType()
              + " and id:"
              + vnfrId);
      throw e;
    } catch (InterruptedException | SDKException e) {
      log.error(
          "Wait failed for event "
              + getAction()
              + " of vnfr with type:"
              + getVnfrType()
              + " and id:"
              + vnfrId);
      throw e;
    }
    //I can choose what to return:
    //-nsr
    //-vnfr received and available invoking getPayload()
    return nsr;
  }

  @Override
  public void configureSubTask(Profile.Section currentSection) {
    this.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));

    String action = currentSection.get("action");
    String vnfType = currentSection.get("vnf-type");
    if (action == null || action.isEmpty()) {
      try {
        throw new IntegrationTestException("action for VirtualNetworkFunctionRecordWait not set");
      } catch (IntegrationTestException e) {
        e.printStackTrace();
        log.error(e.getMessage());
        System.exit(42);
      }
    }
    if (vnfType == null || vnfType.isEmpty()) {
      try {
        throw new IntegrationTestException("vnf-type property not set");
      } catch (IntegrationTestException e) {
        e.printStackTrace();
        log.error(e.getMessage());
        System.exit(42);
      }
    }
    this.setAction(Action.valueOf(action));
    this.setVnfrType(vnfType);
  }

  public void setVnfrType(String type) {
    vnfrType = type;
  }

  public String getVnfrType() {
    return vnfrType;
  }

  private String getVnfrIdFromNsr(NetworkServiceRecord networkServiceRecord, String vnfrType) {
    if (networkServiceRecord == null)
      throw new NullPointerException("NetworkServiceRecord is null");
    if (vnfrType == null || vnfrType.isEmpty())
      throw new NullPointerException("vnfrType is null or empty");
    log.debug("" + networkServiceRecord);
    for (VirtualNetworkFunctionRecord vnfr : networkServiceRecord.getVnfr()) {
      log.debug("VNFR type found: " + vnfr.getType());
      if (vnfr.getType().equalsIgnoreCase(vnfrType)) return vnfr.getId();
    }
    throw new NullPointerException("No vnfr found for type: " + vnfrType);
  }
}
