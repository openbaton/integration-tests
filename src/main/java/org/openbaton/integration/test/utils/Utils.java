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

  private static InputStream getInputStream(String fileName) throws FileNotFoundException {
    if (checkFileExists(fileName)) {
      return new FileInputStream(new File(fileName));
    } else {
      return Utils.class.getClassLoader().getResourceAsStream(fileName);
    }
  }

  public static String getStringFromInputStream(String fileName) throws FileNotFoundException {
    InputStream is = getInputStream(fileName);

    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is), 65728);
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

  public static boolean isNfvoStarted(String nfvoIp, String nfvoPort) {
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

  public static List<URL> loadFileIni(Properties properties) throws IOException {
    String scenarioPath = properties.getProperty("integration-test-scenarios");
    // scenario files stored on the host machine
    log.info("Loading files from " + scenarioPath);
    LinkedList<URL> externalFiles = new LinkedList<>();
    if (scenarioPath != null) {
      externalFiles = getExternalFilesAsURL(scenarioPath);
    }
    return externalFiles;
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

  public static LinkedList<URL> getFilesAsURL(String location) throws IOException {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources(location);

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
    if (f != null && f.exists()) {
      log.debug("File or folder " + filename + " exists");
      return true;
    }
    log.debug("File or folder " + filename + " does not exist");
    return false;
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
    return new TextTable(columns, results);
  }
}
