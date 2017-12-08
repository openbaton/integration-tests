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

import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.User;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.UserAgent;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by tbr on 02.08.16.
 *
 * Class used to create a new user.
 */
public class UserCreate extends Tester<User> {

  private boolean expectedToFail =
      false; // if the creating of the user is expected to fail this field should be set to true
  private String
      asUser; // if another user than specified in the integration-tests.properties file should try to create the user
  private String asUserPassword;
  private String newUserName;
  private String newUserPwd;
  private boolean userIsAdmin;
  private List<String> userProjects = new LinkedList<>();
  private List<String> guestProjects = new LinkedList<>();
  private boolean enabled;

  private Properties properties = null;

  public UserCreate(Properties p) throws FileNotFoundException {
    super(p, User.class);
    properties = p;
    this.setAbstractRestAgent(requestor.getUserAgent());
  }

  @Override
  protected User prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws SDKException, IntegrationTestException, FileNotFoundException {
    if (asUser != null && !"".equals(asUser))
      log.info("Try to create a new user " + newUserName + " while logged in as " + asUser);
    else log.info("Try to create a new user " + newUserName);
    User user = new User();
    user.setUsername(newUserName);
    user.setPassword(newUserPwd);
    user.setEnabled(enabled);
    Set<Role> roles = new HashSet<>();
    if (userIsAdmin) {
      Role adminRole = new Role();
      adminRole.setRole(Role.RoleEnum.ADMIN);
      adminRole.setProject("default");
      roles.add(adminRole);
    }
    for (String userProject : userProjects) {
      Role userRole = new Role();
      userRole.setRole(Role.RoleEnum.USER);
      userRole.setProject(userProject);
      roles.add(userRole);
    }
    for (String guestProject : guestProjects) {
      Role guestRole = new Role();
      guestRole.setRole(Role.RoleEnum.GUEST);
      guestRole.setProject(guestProject);
      roles.add(guestRole);
    }

    user.setRoles(roles);
    try {
      log.debug("Creating new user " + user.toString());
      UserAgent userAgent;
      if (asUser != null && !"".equals(asUser))
        userAgent =
            new UserAgent(
                asUser,
                asUserPassword,
                projectId,
                Boolean.parseBoolean(properties.getProperty("nfvo-ssl-enabled")),
                properties.getProperty("nfvo-ip"),
                properties.getProperty("nfvo-port"),
                "1");
      else userAgent = requestor.getUserAgent();
      userAgent.create(user);
    } catch (SDKException e) {
      if (expectedToFail) {
        log.info("Creation of user " + newUserName + " failed as expected");
        return param;
      } else {
        log.error("Creation of user " + newUserName + " failed");
        throw e;
      }
    }
    if (expectedToFail)
      throw new IntegrationTestException(
          "The creation of user " + newUserName + " was expected to fail but it did not.");

    log.info("Successfully created the new user " + newUserName);
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

  public String getNewUserName() {
    return newUserName;
  }

  public void setNewUserName(String newUserName) {
    this.newUserName = newUserName;
  }

  public boolean isUserIsAdmin() {
    return userIsAdmin;
  }

  public void setUserIsAdmin(String userIsAdmin) {
    this.userIsAdmin = Boolean.parseBoolean(userIsAdmin);
  }

  public List<String> getUserProjects() {
    return userProjects;
  }

  public void setUserProjects(String userProjects) {
    if (userProjects != null && !"".equals(userProjects))
      this.userProjects = Arrays.asList(userProjects.split(",[ ]*"));
  }

  public List<String> getGuestProjects() {
    return guestProjects;
  }

  public void setGuestProjects(String guestProjects) {
    if (guestProjects != null && !"".equals(guestProjects))
      this.guestProjects = Arrays.asList(guestProjects.split(",[ ]*"));
  }

  public String getNewUserPwd() {
    return newUserPwd;
  }

  public void setNewUserPwd(String newUserPwd) {
    this.newUserPwd = newUserPwd;
  }

  public String getAsUserPassword() {
    return asUserPassword;
  }

  public void setAsUserPassword(String asUserPassword) {
    this.asUserPassword = asUserPassword;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(String enabled) {
    this.enabled = Boolean.parseBoolean(enabled);
  }
}
