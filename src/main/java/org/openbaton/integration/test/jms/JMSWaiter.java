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
package org.openbaton.integration.test.jms;

import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.interfaces.WaiterInterface;
import org.openbaton.sdk.api.exception.SDKException;


/**
 * Created by mob on 31.07.15.
 */
public class JMSWaiter implements WaiterInterface {
    @Override
    public void subscribe(EventEndpoint endpoint) throws SubscriptionException, SDKException {

    }

    @Override
    public void unSubscribe() throws SDKException {

    }

    @Override
    public boolean waitForEvent(int timeOut) throws InterruptedException {
        return false;
    }

    @Override
    public Action getAction() {
        return null;
    }

    @Override
    public String getPayload() {
        return null;
    }
}
