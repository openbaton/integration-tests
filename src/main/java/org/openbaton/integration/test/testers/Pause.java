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

  int duration = 0;

  /**
   * @param properties : IntegrationTest properties containing: nfvo-usr nfvo-pwd nfvo-ip nfvo-port
   */
  public Pause(Properties properties) {
    super(properties, Pause.class, "", "");
  }

  @Override
  protected Serializable prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws Exception {
    log.debug("Start pausing for " + duration + " seconds");
    Thread.sleep(duration);
    log.debug("Paused for " + duration + " seconds");
    return param;
  }

  public void setDuration(int duration) {
    if (duration >= 0) this.duration = duration * 1000;
    else log.warn("Chosen duration was less than 0 so we will not use it");
  }
}
