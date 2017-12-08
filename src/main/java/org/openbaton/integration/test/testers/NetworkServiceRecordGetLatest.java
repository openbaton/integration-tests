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
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.rest.NetworkServiceRecordAgent;

/**
 * Created by tbr on 15.02.16.
 *
 * <p>Class used to get the latest version of a NetworkServiceRecord from the NFVO.
 */
public class NetworkServiceRecordGetLatest extends Tester {

  /**
   * @param properties : IntegrationTest properties containing: nfvo-usr nfvo-pwd nfvo-ip nfvo-port
   */
  public NetworkServiceRecordGetLatest(Properties properties) throws FileNotFoundException {
    super(properties, NetworkServiceRecordGetLatest.class);
    this.setAbstractRestAgent(requestor.getNetworkServiceRecordAgent());
  }

  @Override
  protected Serializable prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws Exception {
    NetworkServiceRecord nsr = (NetworkServiceRecord) param;
    if (nsr == null) {
      log.error(
          "The passed NSR is null. This task needs to be placed behind a task that passes a NSR.");
      throw new NullPointerException("The passed NSR was null.");
    }
    log.debug("Try retrieving the latest state of the NSR with id " + nsr.getId());
    NetworkServiceRecordAgent agent = this.requestor.getNetworkServiceRecordAgent();
    nsr = agent.findById(nsr.getId());
    log.debug("The latest NSR is: \n" + nsr);
    return nsr;
  }

  @Override
  public void configureSubTask(Profile.Section currentSection) {}
}
