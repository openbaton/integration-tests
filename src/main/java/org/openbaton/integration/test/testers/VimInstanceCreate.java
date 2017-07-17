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

import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.parser.Parser;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.VimInstanceAgent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 *
 * Class used to create a VimInstance. It can be specified which user should delete the VimInstance
 * and in which project he should try to attempt it.
 */
public class VimInstanceCreate extends Tester<VimInstance> {

  private static final String LOCAL_PATH_NAME = "/etc/json_file/vim_instances/";
  private static final String EXTERNAL_PATH_NAME =
      "/etc/openbaton/integration-tests/vim-instances/";
  private static final String EXTERNAL_PATH_NAME_PARSER_VIM =
      "/etc/openbaton/integration-tests/parser-properties/vim.properties";
  private String fileName;
  private boolean expectedToFail = false;
  private String
      asUser; // if another user than specified in the integration-tests.properties file should try to create the user
  private String asUserPassword;
  private String inProject; // specifies the project in which to create the vim instance
  private Properties properties = null;

  /**
   * @param properties : IntegrationTest properties containing: nfvo-usr nfvo-pwd nfvo-ip nfvo-port
   */
  public VimInstanceCreate(Properties properties) {
    super(properties, VimInstance.class, LOCAL_PATH_NAME, "/datacenters");
    this.properties = properties;
    this.setAbstractRestAgent(requestor.getVimInstanceAgent());
  }

  @Override
  protected Object doWork() throws SDKException, IntegrationTestException {
    Object result;
    try {
      if (asUser != null && !"".equals(asUser)) {
        String projectId = properties.getProperty("nfvo-project-id");
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
        VimInstance expected = prepareObject();
        if (expected == null) throw new NullPointerException();
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
  protected VimInstance prepareObject() {
    String body = null;
    File f = new File(EXTERNAL_PATH_NAME + fileName);
    if (f != null && f.exists()) {
      try {
        body = Utils.getStringFromInputStream(new FileInputStream(f));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      log.debug("No file: " + f.getName() + " found, we will use " + LOCAL_PATH_NAME + fileName);
      body =
          Utils.getStringFromInputStream(
              Tester.class.getResourceAsStream(LOCAL_PATH_NAME + fileName));
    }
    String vimRandom = null;
    File parserPropertiesFile = new File(EXTERNAL_PATH_NAME_PARSER_VIM);
    if (parserPropertiesFile != null && parserPropertiesFile.exists()) {
      try {
        vimRandom = Parser.randomize(body, EXTERNAL_PATH_NAME_PARSER_VIM);
      } catch (IOException e) {
        e.printStackTrace();
      }
      log.debug("vim-instance.json (old): " + body);
      log.debug("vim-instance.json (random): " + vimRandom);
      return mapper.fromJson(vimRandom, aClass);
    } else {
      log.debug(
          "If you want to use the parser for the VIM, create the file vim.properties in the path /etc/openbaton/integration-test-parser-properties/");
    }
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
