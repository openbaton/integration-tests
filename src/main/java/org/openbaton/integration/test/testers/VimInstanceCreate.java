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
 * Created by lto on 24/06/15.
 *
 * <p>Class used to create a VimInstance. It can be specified which user should delete the
 * VimInstance and in which project he should try to attempt it.
 */
public class VimInstanceCreate extends Tester<BaseVimInstance> {

  private String fileName;
  private boolean expectedToFail = false;
  private String
      asUser; // if another user than specified in the integration-tests.properties file should try to create the user
  private String asUserPassword;
  private String inProject; // specifies the project in which to create the vim instance
  private Properties properties;

  /**
   * @param properties : IntegrationTest properties containing: nfvo-usr nfvo-pwd nfvo-ip nfvo-port
   */
  public VimInstanceCreate(Properties properties) {
    super(properties, BaseVimInstance.class);
    this.properties = properties;
  }

  @Override
  protected Object doWork() throws SDKException, IntegrationTestException, FileNotFoundException {
    this.setAbstractRestAgent(requestor.getVimInstanceAgent());
    Object result;
    try {
      if (asUser != null && !"".equals(asUser)) {
        if (inProject != null && !"".equals(inProject)) {
          log.info(
              "Upload vim instance "
                  + fileName
                  + " as user "
                  + asUser
                  + " in project "
                  + inProject);
          projectId = Utils.getProjectIdByName(requestor, inProject);
        } else {
          log.info("Upload vim instance " + fileName + " as user " + asUser);
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
        GenericVimInstance expected = (GenericVimInstance) prepareObject();
        if (expected == null) throw new IntegrationTestException();
        result = vimAgent.create(expected);
      } else {
        log.info("Upload vim instance " + fileName);
        try {
          result = create();
        } catch (SDKException sdkEx) {
          log.error("Exception during the instantiation of VimInstance");
          throw sdkEx;
        }
      }
    } catch (SDKException e) {
      if (expectedToFail) {
        log.info("Creation of Vim Instance " + fileName + " failed as expected");
        return param;
      } else {
        log.error("Creation of Vim Instance " + fileName + " failed");
        throw e;
      }
    }
    if (expectedToFail)
      throw new IntegrationTestException(
          "The creation of Vim Instance " + fileName + " was expected to fail but it did not.");

    log.debug("--- upload of vim instance " + fileName + " successful");
    return result;
  }

  @Override
  public void configureSubTask(Profile.Section currentSection) {
    this.setFileName(currentSection.get("name-file"));
    this.setAsUser(currentSection.get("as-user-name"));
    this.setAsUserPassword(currentSection.get("as-user-password"));
    this.setExpectedToFail(currentSection.get("expected-to-fail"));
    this.setInProject(currentSection.get("in-project"));
  }

  @Override
  protected BaseVimInstance prepareObject() throws FileNotFoundException {
    String body = Utils.getContent(properties.getProperty("vim-path") + fileName);
    return mapper.fromJson(body, aClass);
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getAsUserPassword() {
    return asUserPassword;
  }

  public void setAsUserPassword(String asUserPassword) {
    this.asUserPassword = asUserPassword;
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

  public void setInProject(String inProject) {
    this.inProject = inProject;
  }

  public String getInProject() {
    return inProject;
  }
}
