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
import org.openbaton.catalogue.security.Project;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.ProjectAgent;

/**
 * Created by tbr on 02.08.16.
 *
 * <p>Class used to create a new project.
 */
public class ProjectCreate extends Tester<Project> {

  private boolean expectedToFail = false;
  // if the creating of the user is expected to fail this field should be set to true
  private String asUser;
  // if another user than specified in the integration-tests.properties file should try to create the project
  private String userPassword;
  private String projectName;

  private Properties properties = null;

  public ProjectCreate(Properties p) throws FileNotFoundException {
    super(p, Project.class);
    properties = p;
    this.setAbstractRestAgent(requestor.getProjectAgent());
  }

  @Override
  protected Project prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws SDKException, IntegrationTestException, FileNotFoundException {
    if (asUser != null && !"".equals(asUser)) {
      log.info("Try to create a new project " + projectName + " while logged in as " + asUser);
    } else {
      log.info("Try to create a new project " + projectName);
    }
    Project project = new Project();
    project.setName(projectName);

    try {
      log.debug("Creating new project " + project.toString());
      ProjectAgent projectAgent;
      if (asUser != null && !"".equals(asUser)) {
        projectAgent =
            new ProjectAgent(
                asUser,
                userPassword,
                projectId,
                Boolean.parseBoolean(properties.getProperty("nfvo-ssl-enabled")),
                properties.getProperty("nfvo-ip"),
                properties.getProperty("nfvo-port"),
                "1");
      } else {
        projectAgent = requestor.getProjectAgent();
      }
      projectAgent.create(project);
    } catch (SDKException e) {
      if (expectedToFail) {
        log.info("Creation of project " + projectName + " failed as expected");
        return param;
      } else {
        log.error("Creation of project " + projectName + " failed");
        throw e;
      }
    }
    if (expectedToFail) {
      throw new IntegrationTestException(
          "The creation of project " + projectName + " was expected to fail but it did not.");
    }

    log.info("Successfully created the new project " + projectName);
    return param;
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

  public String getUserPassword() {
    return userPassword;
  }

  public void setUserPassword(String userPassword) {
    this.userPassword = userPassword;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }
}
