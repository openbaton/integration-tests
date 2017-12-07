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

import org.openbaton.integration.test.utils.Tester;

import java.io.Serializable;
import java.util.Properties;

/**
 * Created by tbr on 12.02.16.
 *
 * Class used to pause the execution of the integration tests for a specific time.
 */
public class Pause extends Tester {

  private int duration = 0;

  /**
   * @param properties : IntegrationTest properties containing: nfvo-usr nfvo-pwd nfvo-ip nfvo-port
   */
  public Pause(Properties properties) {
    super(properties, Pause.class, "");
  }

  @Override
  protected Serializable prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws Exception {
    log.debug("Start pausing for " + duration + " milliseconds");
    Thread.sleep(duration);
    log.debug("Paused for " + duration + " milliseconds");
    return param;
  }

  public void setDuration(int duration) {
    if (duration >= 0) this.duration = duration * 1000;
    else log.warn("Chosen duration was less than 0 so we will not use it");
  }
}
