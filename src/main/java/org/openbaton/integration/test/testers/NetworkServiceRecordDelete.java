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

import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;

import java.util.Properties;

/**
 * Created by mob on 28.07.15.
 *
 * Class used to delete a networkServiceRecord.
 */
public class NetworkServiceRecordDelete extends Tester<NetworkServiceRecord> {

  public NetworkServiceRecordDelete(Properties properties) {
    super(properties, NetworkServiceRecord.class, "", "/ns-records");
    this.setAbstractRestAgent(requestor.getNetworkServiceRecordAgent());
  }

  @Override
  protected NetworkServiceRecord prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws SDKException {
    NetworkServiceRecord nsr = (NetworkServiceRecord) param;
    log.info("Delete NSR " + nsr.getName() + " with id " + nsr.getId());
    try {
      delete(nsr.getId());
    } catch (SDKException sdkEx) {
      log.error("Exception during deleting of NetworkServiceRecord with id: " + nsr.getId(), sdkEx);
      throw sdkEx;
    }
    log.debug("--- deleted NSR " + nsr.getName() + " with id " + nsr.getId());
    return nsr;
  }
}
