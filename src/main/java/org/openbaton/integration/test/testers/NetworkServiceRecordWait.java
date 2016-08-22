/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.interfaces.Waiter;
import org.openbaton.sdk.api.exception.SDKException;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by mob on 28.07.15.
 *
 * Class used to wait for an event on NetworkServiceRecord level.
 */
public class NetworkServiceRecordWait extends Waiter {

  private String name = NetworkServiceRecordWait.class.getSimpleName();

  public NetworkServiceRecordWait(Properties properties) {
    super(properties, NetworkServiceRecordWait.class, "", "");
  }

  @Override
  protected Serializable prepareObject() {
    return null;
  }

  @Override
  protected Object doWork()
      throws SDKException, SubscriptionException, InterruptedException, IntegrationTestException {

    NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();

    EventEndpoint eventEndpoint = createEventEndpoint(name, EndpointType.REST);
    eventEndpoint.setNetworkServiceId(nsr.getId());
    //The eventEndpoint param of EventEndpoint will be set in the RestWaiter

    try {
      subscribe(eventEndpoint);
      log.debug(
          name
              + ": --- registration complete, start waiting for "
              + getAction().toString()
              + " of nsr with id:"
              + nsr.getId()
              + "....");
      if (waitForEvent())
        log.debug(
            name
                + ": --- waiting complete for "
                + getAction().toString()
                + " of nsr with id:"
                + nsr.getId());
      else {
        log.error(
            name
                + ": --- timeout elapsed for "
                + getAction().toString()
                + " of nsr with id:"
                + nsr.getId());
        throw new IntegrationTestException("Timeout elapsed.");
      }
      unSubscribe();

    } catch (SubscriptionException e) {
      log.error(
          "Subscription failed for event "
              + getAction()
              + " nsr id: "
              + nsr.getId()
              + " nsr name: "
              + nsr.getName());
      throw e;
    } catch (InterruptedException | SDKException e) {
      log.error(
          "Wait failed for event "
              + getAction()
              + " nsr id: "
              + nsr.getId()
              + " nsr name: "
              + nsr.getName());
      throw e;
    }
    nsr = mapper.fromJson(getPayload(), NetworkServiceRecord.class);
    return nsr;
  }
}
