/*
 * Copyright (c) 2015 Fraunhofer FOKUS
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
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class Utils {

  private static final String PROPERTIES_FILE = "/integration-test.properties";
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

  public static List<URL> getFilesAsURL(String location) {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = {};
    try {
      resources = resolver.getResources(location);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    List<URL> urls = new LinkedList<>();
    for (Resource resource : resources) {
      try {
        urls.add(resource.getURL());
      } catch (IOException e) {
        e.printStackTrace();
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
              + error.error_description);
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
            "Error during authentication: " + jobj.get("error_description").getAsString(), e);
      }
    }
    return bearerToken;
  }

  private class ParseComError implements Serializable {
    String error_description;
    String error;
  }
}
