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
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.interfaces.Waiter;
import org.openbaton.sdk.api.exception.SDKException;

/**
 * Created by mob on 31.07.15.
 *
 * <p>Class used to wait for an event on NetworkServiceDescriptor level.
 */
public class NetworkServiceDescriptorWait extends Waiter {

  private static final String name = "NetworkServiceDescriptorWait";

  public NetworkServiceDescriptorWait(Properties properties) throws FileNotFoundException {
    super(properties, NetworkServiceRecordWait.class);
  }

  @Override
  protected Serializable prepareObject() {
    return null;
  }

  @Override
  protected Object doWork()
      throws SubscriptionException, SDKException, InterruptedException, FileNotFoundException {
    this.setAbstractRestAgent(requestor.getNetworkServiceDescriptorAgent());
    EventEndpoint eventEndpoint = createEventEndpoint(name);
    eventEndpoint.setEvent(getAction());
    NetworkServiceDescriptor nsd = (NetworkServiceDescriptor) param;
    //The eventEndpoint param of EventEndpoint will be set in the RestWaiter

    try {
      subscribe(eventEndpoint);
      log.debug(
          name
              + ": --- Registration complete, start waiting for "
              + getAction()
              + " on nsd with id:"
              + nsd.getId());
      waitForEvent();
      this.unSubscribe();
      log.debug(name + ": --- Finished waiting for " + getAction() + " on nsd " + nsd.getId());
    } catch (SubscriptionException e) {
      log.error(
          "Subscription failed for the event "
              + eventEndpoint.getEvent().toString()
              + " nsd id: "
              + nsd.getId()
              + " nsd name: "
              + nsd.getName());
      throw e;
    } catch (SDKException e) {
      log.error(
          "Wait failed for the event "
              + eventEndpoint.getEvent().toString()
              + " nsd id: "
              + nsd.getId()
              + " nsd name: "
              + nsd.getName());
      throw e;
    } catch (InterruptedException e) {
      log.error(
          "Wait failed for the event "
              + eventEndpoint.getEvent().toString()
              + " nsd id: "
              + nsd.getId()
              + " nsd name: "
              + nsd.getName());
      throw e;
    }
    return nsd.getId();
  }

  @Override
  public void configureSubTask(Profile.Section currentSection) {
    this.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));

    String action = currentSection.get("action");
    if (action == null || action.isEmpty()) {
      try {
        throw new IntegrationTestException("action for VirtualNetworkFunctionRecordWait not set");
      } catch (IntegrationTestException e) {
        e.printStackTrace();
        log.error(e.getMessage());
        System.exit(42);
      }
    }
    this.setAction(Action.valueOf(action));
  }
}
