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
import java.util.Properties;
import org.ini4j.Profile;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class used to create a new NetworkServiceRecord from a NetworkServiceDescriptor. */
public class NetworkServiceRecordCreate extends Tester<NetworkServiceRecord> {

  private static Logger log = LoggerFactory.getLogger(NetworkServiceRecordCreate.class);

  /**
   * @param properties : IntegrationTest properties containing: nfvo-usr nfvo-pwd nfvo-ip nfvo-port
   */
  public NetworkServiceRecordCreate(Properties properties) throws FileNotFoundException {
    super(properties, NetworkServiceRecord.class);
    this.setAbstractRestAgent(requestor.getNetworkServiceRecordAgent());
  }

  @Override
  protected Object doWork() throws SDKException, FileNotFoundException {
    return create();
  }

  @Override
  public void configureSubTask(Profile.Section currentSection) {}

  @Override
  public NetworkServiceRecord create() throws SDKException, FileNotFoundException {

    NetworkServiceDescriptor nsd = (NetworkServiceDescriptor) this.param;
    log.info("Launch NSR from NSD " + nsd.getName() + " with id " + nsd.getId());
    NetworkServiceRecord networkServiceRecord = null;
    try {
      networkServiceRecord =
          this.requestor.getNetworkServiceRecordAgent().create(nsd.getId(), null, null, null);
    } catch (SDKException sdkEx) {
      log.error(
          "Exception during the instantiation of NetworkServiceRecord from nsd of id: "
              + nsd.getId());
      throw sdkEx;
    }
    log.debug(
        " --- Created nsr with id: "
            + networkServiceRecord.getId()
            + " from nsd with id: "
            + nsd.getId());

    return networkServiceRecord;
  }

  @Override
  protected NetworkServiceRecord prepareObject() {
    return null;
  }
}
