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

import dnl.utils.text.table.TextTable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.User;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/** Created by lto on 24/06/15. */
public class Utils {

  private static Logger log = LoggerFactory.getLogger(Utils.class);

  public static Properties getProperties(String propertiesFile) throws IOException {
    Properties properties = new Properties();
    log.info("Reading properties from file " + propertiesFile);
    properties.load(getInputStream(propertiesFile));
    return properties;
  }

  public static InputStream getInputStream(String fileName) throws FileNotFoundException {
    if (checkFileExists(fileName)) {
      return new FileInputStream(new File(fileName));
    } else {
      log.debug("Loading file " + fileName + " from classpath");
      InputStream is = Utils.class.getClassLoader().getResourceAsStream(fileName);
      if (is == null)
        throw new FileNotFoundException("File " + fileName + " was not found in the classpath");
      return is;
    }
  }

  public static String getContent(String fileName) throws FileNotFoundException {
    InputStream is = getInputStream(fileName);
    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is), 65728);
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

  private static boolean available(String host, int port) {
    try {
      Socket s = new Socket(host, port);
      log.info("Server is listening on port " + port + " of " + host);
      s.close();
      return true;
    } catch (IOException ex) {
      // The remote host is not listening on this port
      log.warn("Server is not listening on port " + port + " of " + host);
      return false;
    }
  }

  public static boolean isNfvoStarted(String nfvoIp, int nfvoPort) {
    int i = 0;
    while (!available(nfvoIp, nfvoPort)) {
      i++;
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (i > 40) {
        return false;
      }
    }
    return true;
  }

  /*
   * Get .ini files from an external directory.
   */
  public static LinkedList<URL> getURLFileList(String location) {
    File dir = new File(location);
    if (!dir.exists()) return getURLFileListLocal(location);
    log.trace("Found dir " + dir.getName());
    log.trace("Found dir " + dir.getAbsolutePath());

    File[] iniFiles =
        dir.listFiles((dir1, name) -> name.endsWith(".ini"));
    LinkedList<URL> urls = new LinkedList<>();
    if (iniFiles == null) return urls;
    for (File f : iniFiles) {
      log.trace("Found file " + f.getName());
      try {
        urls.add(f.toURI().toURL());
      } catch (MalformedURLException e) {
        log.error(
            "Could not add file " + f.getName() + " because its URL is not formatted correctly.");
      }
    }
    return urls;
  }

  private static LinkedList<URL> getURLFileListLocal(String location) {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = {};
    try {
      resources = resolver.getResources(location + "*.ini");
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    LinkedList<URL> urls = new LinkedList<>();
    for (Resource resource : resources) {
      log.trace("Found resource " + resource);
      try {
        urls.add(resource.getURL());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return urls;
  }

  public static List<String> getFileNames(List<URL> iniFileURLs) {
    List<String> fileNames = new LinkedList<>();
    for (URL url : iniFileURLs) {
      String[] splittedUrl = url.toString().split("/");
      String name = splittedUrl[splittedUrl.length - 1];
      fileNames.add(name);
    }
    return fileNames;
  }

  public static boolean checkFileExists(String filename) {
    File f = new File(filename);
    if (f.exists()) {
      log.debug("File or folder " + filename + " exists");
      return true;
    }
    log.debug("File or folder " + filename + " does not exist");
    return false;
  }

  public static String getProjectIdByName(NFVORequestor requestor, String projectName) throws SDKException {
    return requestor.getProjectAgent().findAll().stream().filter(p -> p.getName().equals(projectName)).findFirst().orElseThrow(() -> new SDKException("Did not find a project named " + projectName)).getId();
  }

  public static String getUserIdByName(NFVORequestor requestor, String userName) throws SDKException {
    User u = getUserByName(requestor, userName);
    if (u == null) return "";
    else return u.getId();
  }

  public static User getUserByName(NFVORequestor requestor, String userName) throws SDKException {
    return requestor.getUserAgent().findAll().stream().filter(u -> u.getUsername().equals(userName)).findFirst().orElseThrow(() -> new SDKException("Did not find a user named " + userName));
  }

  public static TextTable getResultsTable(String[] columns, Map<String, String> content) {
    String[][] results = new String[content.size()][2];
    int count = 0;
    for (Map.Entry<String, String> entry : content.entrySet()) {
      results[count][0] = entry.getKey();
      results[count][1] = entry.getValue();
      count++;
    }
    return new TextTable(columns, results);
  }
}
