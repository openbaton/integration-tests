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
package org.openbaton.integration.test.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.util.AbstractRestAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by lto on 15/07/15.
 *
 * Abstract class that represents the tasks defined in the .ini files.
 */
public abstract class Tester<T extends Serializable> extends SubTask {
  protected Properties properties;
  protected final Class<T> aClass;
  protected String sshPrivateKeyFilePath;

  public void setAbstractRestAgent(AbstractRestAgent abstractRestAgent) {
    this.abstractRestAgent = abstractRestAgent;
  }

  private AbstractRestAgent abstractRestAgent;
  protected NFVORequestor requestor;
  protected Gson mapper;
  protected static final Logger log = LoggerFactory.getLogger(Tester.class);

  /**
   * @param properties : IntegrationTest properties containing: nfvo-usr nfvo-pwd nfvo-ip nfvo-port
   * @param aClass : example VimInstance.class
   * @param filePath : example "/etc/json_file/vim_instances/vim-instance.json"
   */
  public Tester(Properties properties, Class<T> aClass, String filePath) {
    GsonBuilder builder = new GsonBuilder();
    mapper = builder.create();
    this.properties = properties;
    this.sshPrivateKeyFilePath =
        properties.getProperty(
            "ssh-private-key-file-path", "/etc/openbaton/integration-test/integration-test.key");
    //log.debug("using properties: " + properties.getProperty("nfvo-usr") + properties.getProperty("nfvo-pwd") + properties.getProperty("nfvo-ip") + properties.getProperty("nfvo-port") + "1");
    requestor =
        new NFVORequestor(
            properties.getProperty("nfvo-usr"),
            properties.getProperty("nfvo-pwd"),
            properties.getProperty("nfvo-project-id"),
            Boolean.parseBoolean(properties.getProperty("nfvo-ssl-enabled")),
            properties.getProperty("nfvo-ip"),
            properties.getProperty("nfvo-port"),
            "1");
    this.aClass = aClass;
  }

  public T create() throws SDKException, FileNotFoundException {
    T expected = prepareObject();
    if (expected == null) throw new NullPointerException();
    T obtained;
    obtained = (T) abstractRestAgent.create(expected);

    log.trace("Received: " + obtained.toString());

    return obtained;
  }

  public void delete(String id) throws SDKException {

    abstractRestAgent.delete(id);

    log.debug("Deleted: " + id);
  }

  protected abstract T prepareObject() throws FileNotFoundException;
}
