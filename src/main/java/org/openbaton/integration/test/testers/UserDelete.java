package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.security.User;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.UserAgent;

import java.util.Properties;

/**
 * Created by tbr on 02.08.16.
 *
 * Class used to delete a User.
 *
 */
public class UserDelete extends Tester<User> {

  private boolean expectedToFail =
      false; // if the creating of the user is expected to fail this field should be set to true
  private String
      asUser; // if another user than specified in the integration-test.properties file should try to delete the user
  private String userPassword;
  private String userToDelete;

  private Properties properties = null;

  public UserDelete(Properties p) {
    super(p, User.class, "", "/users");
    this.properties = p;
  }

  @Override
  protected User prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws SDKException, IntegrationTestException {
    if (asUser != null && !"".equals(asUser))
      log.info("Try to delete user " + userToDelete + " while logged in as " + asUser);
    else log.info("Try to delete user " + userToDelete);

    try {
      String userId = Utils.getUserIdByName(requestor, userToDelete);

      UserAgent userAgent;
      if (asUser != null && !"".equals(asUser))
        userAgent =
            new UserAgent(
                asUser,
                userPassword,
                properties.getProperty("nfvo-project-id"),
                Boolean.parseBoolean(properties.getProperty("nfvo-ssl-enabled")),
                properties.getProperty("nfvo-ip"),
                properties.getProperty("nfvo-port"),
                "/users",
                "1");
      else userAgent = requestor.getUserAgent();
      userAgent.delete(userId);
    } catch (SDKException e) {
      if (expectedToFail) {
        log.info("Deletion of user " + userToDelete + " failed as expected");
        return param;
      } else {
        log.error("Deletion of user " + userToDelete + " failed");
        throw e;
      }
    }
    if (expectedToFail)
      throw new IntegrationTestException(
          "The deletion of user " + userToDelete + " was expected to fail but it did not.");
    log.info("Successfully deleted user " + userToDelete);
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

  public String getUserToDelete() {
    return userToDelete;
  }

  public void setUserToDelete(String userToDelete) {
    this.userToDelete = userToDelete;
  }
}
