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
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.interfaces.Waiter;
import org.openbaton.sdk.api.exception.SDKException;

/**
 * Created by mob on 28.07.15.
 *
 * <p>Class used to wait for an event on NetworkServiceRecord level.
 */
public class NetworkServiceRecordWait extends Waiter {

  private String name = NetworkServiceRecordWait.class.getSimpleName();

  public NetworkServiceRecordWait(Properties properties) throws FileNotFoundException {
    super(properties, NetworkServiceRecordWait.class);
  }

  @Override
  protected Serializable prepareObject() {
    return null;
  }

  @Override
  protected Object doWork()
      throws SDKException, SubscriptionException, InterruptedException, IntegrationTestException,
          FileNotFoundException {

    NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();
    this.setAbstractRestAgent(requestor.getNetworkServiceRecordAgent());

    EventEndpoint eventEndpoint = createEventEndpoint(name);
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
    } finally {
      unSubscribe();
    }
    nsr = mapper.fromJson(getPayload(), NetworkServiceRecord.class);
    return nsr;
  }

  @Override
  public void configureSubTask(Profile.Section currentSection) {
    this.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));

    String action = currentSection.get("action");
    if (action == null) {
      try {
        throw new IntegrationTestException("action for NetworkServiceRecordWait not set");
      } catch (IntegrationTestException e) {
        e.printStackTrace();
        log.error(e.getMessage());
        System.exit(42);
      }
    }
    this.setAction(Action.valueOf(action));
  }
}
