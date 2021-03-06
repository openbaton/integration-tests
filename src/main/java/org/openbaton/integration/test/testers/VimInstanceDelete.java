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
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.GenericVimInstance;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.VimInstanceAgent;

/**
 * Created by mob on 04.08.15.
 *
 * <p>Class used to delete a VimInstance. It can be specified which user should delete the
 * VimInstance and in which project he should try to attempt it.
 */
public class VimInstanceDelete extends Tester<GenericVimInstance> {

  private boolean expectedToFail = false;
  private String
      asUser; // if another user than specified in the integration-tests.properties file should try to create the user
  private String asUserPassword;
  private String inProject; // specifies the project in which to delete the vim instance
  private Properties properties;

  /**
   * @param properties : IntegrationTest properties containing: nfvo-usr nfvo-pwd nfvo-ip nfvo-port
   */
  public VimInstanceDelete(Properties properties) {
    super(properties, GenericVimInstance.class);
    this.properties = properties;
  }

  @Override
  protected GenericVimInstance prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws SDKException, IntegrationTestException, FileNotFoundException {
    this.setAbstractRestAgent(requestor.getVimInstanceAgent());
    BaseVimInstance vi = (BaseVimInstance) param;
    if (asUser == null || "".equals(asUser)) log.info("Delete vim instance " + vi.getName());

    try {
      if (asUser != null && !"".equals(asUser)) {
        if (inProject != null && !"".equals(inProject)) {
          log.info(
              "Delete Vim Instance "
                  + vi.getName()
                  + " as user "
                  + asUser
                  + " in project "
                  + inProject);
          projectId = Utils.getProjectIdByName(requestor, inProject);
        } else {
          log.info("Delete Vim Instance " + vi.getName() + " as user " + asUser);
        }

        VimInstanceAgent vimAgent =
            new VimInstanceAgent(
                asUser,
                asUserPassword,
                projectId,
                Boolean.parseBoolean(properties.getProperty("nfvo-ssl-enabled")),
                properties.getProperty("nfvo-ip"),
                properties.getProperty("nfvo-port"),
                "1");
        vimAgent.delete(vi.getId());
      } else {
        try {
          delete(vi.getId());
        } catch (SDKException sdkEx) {
          log.error(
              "Exception during deletion of VimInstance "
                  + vi.getName()
                  + " with id: "
                  + vi.getId(),
              sdkEx);
          throw sdkEx;
        }
      }
    } catch (SDKException e) {
      if (expectedToFail) {
        log.info("Deletion of Vim Instance " + vi.getName() + " failed as expected");
        return param;
      } else {
        log.error("Deletion of Vim Instance " + vi.getName() + " failed");
        throw e;
      }
    }
    if (expectedToFail)
      throw new IntegrationTestException(
          "The deletion of Vim Instance " + vi.getName() + " was expected to fail but it did not.");

    log.debug("--- VimInstanceDelete has deleted the vimInstance:" + vi.getName());
    return null;
  }

  @Override
  public void configureSubTask(Profile.Section currentSection) {
    this.setAsUser(currentSection.get("as-user-name"));
    this.setAsUserPassword(currentSection.get("as-user-password"));
    this.setExpectedToFail(currentSection.get("expected-to-fail"));
    this.setInProject(currentSection.get("in-project"));
  }

  public boolean isExpectedToFail() {
    return expectedToFail;
  }

  public void setExpectedToFail(String expectedToFail) {
    this.expectedToFail = Boolean.parseBoolean(expectedToFail);
  }

  public String getAsUser() {
    return asUser;
  }

  public void setAsUser(String asUser) {
    this.asUser = asUser;
  }

  public String getAsUserPassword() {
    return asUserPassword;
  }

  public void setAsUserPassword(String asUserPassword) {
    this.asUserPassword = asUserPassword;
  }

  public String getInProject() {
    return inProject;
  }

  public void setInProject(String inProject) {
    this.inProject = inProject;
  }
}
