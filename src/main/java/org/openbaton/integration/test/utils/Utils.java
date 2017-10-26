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
import com.google.gson.JsonObject;
import dnl.utils.text.table.TextTable;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.User;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by lto on 24/06/15.
 */
public class Utils {

  private static final String PROPERTIES_FILE = "/integration-tests.properties";
  private static Logger log = LoggerFactory.getLogger(Utils.class);

  public static Properties getProperties() throws IOException {
    Properties properties = new Properties();
    properties.load(Utils.class.getResourceAsStream(PROPERTIES_FILE));
    if (properties.getProperty("external-properties-file") != null) {
      File externalPropertiesFile = new File(properties.getProperty("external-properties-file"));
      if (externalPropertiesFile.exists()) {
        log.debug(
            "Loading properties from external-properties-file: "
                + properties.getProperty("external-properties-file"));
        InputStream is = new FileInputStream(externalPropertiesFile);
        properties.load(is);
      } else {
        log.debug(
            "external-properties-file: "
                + properties.getProperty("external-properties-file")
                + " doesn't exist");
      }
    }
    log.debug("Loaded properties: " + properties);
    return properties;
  }

  public static String getStringFromInputStream(InputStream stream) {
    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream), 65728);
      String line = null;

      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return sb.toString();
  }

  public static boolean available(String host, String port) {
    try {
      Socket s = new Socket(host, Integer.parseInt(port));
      log.info("Server is listening on port " + port + " of " + host);
      s.close();
      return true;
    } catch (IOException ex) {
      // The remote host is not listening on this port
      log.warn("Server is not listening on port " + port + " of " + host);
      return false;
    }
  }

  public static LinkedList<URL> getFilesAsURL(String location) {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = {};
    try {
      resources = resolver.getResources(location);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    LinkedList<URL> urls = new LinkedList<>();
    for (Resource resource : resources) {
      try {
        urls.add(resource.getURL());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return urls;
  }

  /*
   * Get .ini files from an external directory.
   */
  public static LinkedList<URL> getExternalFilesAsURL(String location) {
    File dir = new File(location);
    File[] iniFiles =
        dir.listFiles(
            new FilenameFilter() {
              @Override
              public boolean accept(File dir, String name) {
                return name.endsWith(".ini");
              }
            });
    LinkedList<URL> urls = new LinkedList<>();
    if (iniFiles == null) return urls;
    for (File f : iniFiles) {
      try {
        urls.add(f.toURI().toURL());
      } catch (MalformedURLException e) {
        log.error(
            "Could not add file " + f.getName() + " because its URL is not formatted correctly.");
      }
    }
    return urls;
  }

  // taken from the sdks RestRequest class and slightly modified
  public static String getAccessToken(
      String nfvoIp, String nfvoPort, String username, String password)
      throws IOException, SDKException {
    HttpClient httpClient = HttpClientBuilder.create().build();

    HttpPost httpPost = new HttpPost("http://" + nfvoIp + ":" + nfvoPort + "/oauth/token");

    httpPost.setHeader(
        "Authorization",
        "Basic " + Base64.encodeBase64String("openbatonOSClient:secret".getBytes()));
    List<BasicNameValuePair> parametersBody = new ArrayList<>();
    parametersBody.add(new BasicNameValuePair("grant_type", "password"));
    parametersBody.add(new BasicNameValuePair("username", username));
    parametersBody.add(new BasicNameValuePair("password", password));

    log.debug("Username is: " + username);
    log.debug("Password is: " + password);

    httpPost.setEntity(new UrlEncodedFormEntity(parametersBody, StandardCharsets.UTF_8));

    org.apache.http.HttpResponse response = null;
    log.debug("httpPost is: " + httpPost.toString());
    response = httpClient.execute(httpPost);

    String responseString = null;
    responseString = EntityUtils.toString(response.getEntity());
    int statusCode = response.getStatusLine().getStatusCode();
    log.trace(statusCode + ": " + responseString);

    if (statusCode != 200) {
      ParseComError error = new Gson().fromJson(responseString, ParseComError.class);
      log.error(
          "Status Code ["
              + statusCode
              + "]: Error signing-in ["
              + error.error
              + "] - "
              + error.error_description);
      throw new SDKException(
          "Status Code ["
              + statusCode
              + "]: Error signing-in ["
              + error.error
              + "] - "
              + error.error_description,
          new StackTraceElement[0],
          "");
    }
    JsonObject jobj = new Gson().fromJson(responseString, JsonObject.class);
    log.trace("JsonTokeAccess is: " + jobj.toString());
    String bearerToken = null;
    try {
      String token = jobj.get("value").getAsString();
      log.trace(token);
      bearerToken = "Bearer " + token;
    } catch (NullPointerException e) {
      String error = jobj.get("error").getAsString();
      if (error.equals("invalid_grant")) {
        throw new SDKException(
            "Error during authentication",
            e.getStackTrace(),
            jobj.get("error_description").getAsString());
      }
    }
    return bearerToken;
  }

  private class ParseComError implements Serializable {
    String error_description;
    String error;
  }

  public static String getProjectIdByName(NFVORequestor requestor, String projectName) {
    List<Project> projectList = null;
    try {
      projectList = requestor.getProjectAgent().findAll();
    } catch (Exception e) {
      log.warn("Could not connect to NFVO and retrieve the project id of project " + projectName);
    }
    for (Project project : projectList) {
      if (project.getName().equals(projectName)) return project.getId();
    }
    log.warn("Did not find a project named " + projectName);
    return "";
  }

  public static String getUserIdByName(NFVORequestor requestor, String userName) {
    User u = getUserByName(requestor, userName);
    if (u == null) return "";
    else return u.getId();
  }

  public static User getUserByName(NFVORequestor requestor, String userName) {
    List<User> userList = null;
    try {
      userList = requestor.getUserAgent().findAll();
    } catch (Exception e) {
      log.warn("Could not connect to NFVO and retrieve the user id of user " + userName);
    }
    for (User user : userList) {
      if (user.getUsername().equals(userName)) return user;
    }
    log.warn("Did not find a user named " + userName);
    return null;
  }

  public static TextTable getResultsTable(String[] columns, Map<String, String> content) {
    String[][] results = new String[content.size()][2];
    int count = 0;
    for (Map.Entry<String, String> entry : content.entrySet()) {
      results[count][0] = entry.getKey();
      results[count][1] = entry.getValue();
      count++;
    }
    TextTable tt = new TextTable(columns, results);
    return tt;
  }
}
