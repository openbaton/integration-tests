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
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.sdk.api.exception.SDKException;

import java.io.FileNotFoundException;

/**
 * Created by mob on 31.07.15.
 */
public interface WaiterInterface {
  void subscribe(EventEndpoint endpoint)
      throws SubscriptionException, SDKException, FileNotFoundException;

  void unSubscribe() throws SDKException, FileNotFoundException;

  boolean waitForEvent(int timeOut) throws InterruptedException;

  Action getAction();

  String getPayload();
}
