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
package org.openbaton.integration.test.interfaces;

import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.rest.RestWaiter;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;

import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Created by mob on 31.07.15.
 *
 * An abstract class that should be extended by Tester classes with the intention to implement a
 * Tester that waits for a specific event.
 */
public abstract class Waiter extends Tester {
  /**
   * @param properties : IntegrationTest properties containing: nfvo-usr nfvo-pwd nfvo-ip nfvo-port
   * @param aClass : example VimInstance.class
   * @param filePath : example "/etc/json_file/vim_instances/vim-instance.json"
   * @param basePath
   */
  private int timeout;
  private WaiterInterface waiter;
  private Action action;

  public Waiter(Properties properties, Class aClass, String filePath, String basePath) {
    super(properties, aClass);
  }

  /**
   * Subscribe for a specific event.
   *
   * @param eventEndpoint
   * @throws SubscriptionException
   * @throws SDKException
   */
  public void subscribe(EventEndpoint eventEndpoint)
      throws SubscriptionException, SDKException, FileNotFoundException {
    if (eventEndpoint == null) throw new NullPointerException("EventEndpoint is null");
    if (eventEndpoint.getType() == EndpointType.REST)
      waiter = new RestWaiter(eventEndpoint.getName(), requestor, mapper, log, properties);
    waiter.subscribe(eventEndpoint);
  }

  protected EventEndpoint createEventEndpoint(String name) {
    EventEndpoint eventEndpoint = new EventEndpoint();
    eventEndpoint.setEvent(getAction());
    eventEndpoint.setName(name);
    eventEndpoint.setType(EndpointType.REST);
    return eventEndpoint;
  }

  /**
   * Start waiting for the previously subscribed event.
   *
   * @return
   * @throws InterruptedException
   */
  public boolean waitForEvent() throws InterruptedException {
    if (waiter == null)
      throw new NullPointerException("Waiter is null (use subscribe before waitForEvent)");
    return waiter.waitForEvent(getTimeout());
  }

  /**
   * Unsubscribe from the previously subscribed event.
   *
   * @throws SDKException
   */
  public void unSubscribe() throws SDKException, FileNotFoundException {
    if (waiter == null)
      throw new NullPointerException(
          "Waiter is null (use subscribe and waitForEvent before unSubscribe)");
    waiter.unSubscribe();
  }

  public String getPayload() {
    return waiter.getPayload();
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action a) {
    action = a;
  }
}
