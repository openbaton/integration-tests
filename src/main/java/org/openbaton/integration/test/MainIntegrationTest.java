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
package org.openbaton.integration.test;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.NetworkServiceDescriptorAgent;
import org.openbaton.sdk.api.rest.NetworkServiceRecordAgent;
import org.openbaton.sdk.api.rest.VNFPackageAgent;
import org.openbaton.sdk.api.rest.VimInstanceAgent;
import org.openbaton.sdk.api.rest.VirtualNetworkFunctionDescriptorAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainIntegrationTest {
  private static final String EXTERNAL_PATH = "/etc/openbaton/integration-tests/";
  private static final String PROPERTIES_FILE = "integration-tests.properties";
  private static final String SCENARIOS_PATH = "integration-test-scenarios/";
  private static final String NSD_PATH = "network-service-descriptors/";
  private static final String VIM_PATH = "vim-instances/";
  private static final String SCRIPTS_PATH = "scripts/";
  private static final String VNF_PACKAGES_PATH = "vnf-packages/";

  private static final Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);
  private static String nfvoIp;
  private static String nfvoPort;
  private static String nfvoUsr;
  private static String nfvoPwd;
  private static String projectName;
  private static boolean sslEnabled;
  private static boolean clearAfterTest = false;

  /**
   * Load properties from configuration file
   *
   * @return
   * @throws IOException
   * @throws SDKException
   * @throws ClassNotFoundException
   */
  private static Properties loadProperties()
      throws IOException, SDKException, ClassNotFoundException {

    String propertiesFile;
    // Checking whether external properties file exists
    if (Utils.checkFileExists(EXTERNAL_PATH + PROPERTIES_FILE)) {
      propertiesFile = EXTERNAL_PATH + PROPERTIES_FILE;
    } else {
      propertiesFile = PROPERTIES_FILE;
    }
    log.debug("Using properties file: " + propertiesFile);

    Properties properties = Utils.getProperties(propertiesFile);
    properties.setProperty("nfvo-ip", properties.getProperty("nfvo-ip", "localhost"));
    properties.setProperty("nfvo-port", properties.getProperty("nfvo-port", "8080"));
    properties.setProperty("nfvo-usr", properties.getProperty("nfvo-usr", "admin"));
    properties.setProperty("nfvo-pwd", properties.getProperty("nfvo-pwd", "openbaton"));
    properties.setProperty(
        "nfvo-project-name", properties.getProperty("nfvo-project-name", "default"));
    properties.setProperty("nfvo-ssl-enabled", properties.getProperty("nfvo-ssl-enabled", "false"));
    properties.setProperty("local-ip", properties.getProperty("local-ip", "localhost"));
    properties.setProperty("rest-waiter-port", properties.getProperty("rest-waiter-port", "8181"));
    properties.setProperty("clear-after-test", properties.getProperty("clear-after-test", "true"));

    nfvoIp = properties.getProperty("nfvo-ip");
    nfvoPort = properties.getProperty("nfvo-port");
    nfvoUsr = properties.getProperty("nfvo-usr");
    nfvoPwd = properties.getProperty("nfvo-pwd");
    projectName = properties.getProperty("nfvo-project-name");
    sslEnabled = Boolean.parseBoolean(properties.getProperty("nfvo-ssl-enabled"));
    clearAfterTest = Boolean.parseBoolean(properties.getProperty("clear-after-test"));

    // default folders where scenario, nsd, and script files are placed
    if (!Utils.checkFileExists(properties.getProperty("integration-test-scenarios")))
      properties.setProperty("integration-test-scenarios", SCENARIOS_PATH);

    if (!Utils.checkFileExists(properties.getProperty("nsd-path")))
      properties.setProperty("nsd-path", NSD_PATH);

    if (!Utils.checkFileExists(properties.getProperty("vim-path")))
      properties.setProperty("vim-path", VIM_PATH);

    if (!Utils.checkFileExists(properties.getProperty("scripts-path")))
      properties.setProperty("scripts-path", SCRIPTS_PATH);

    if (!Utils.checkFileExists(properties.getProperty("scripts-path")))
      properties.setProperty("vnf-packages-path", VNF_PACKAGES_PATH);

    return properties;
  }

  /**
   * This method tries to remove every NSD, VNFD, VNFPackage and VIM from the orchestrator.
   *
   * @param requestor
   */
  private static void clearOrchestrator(NFVORequestor requestor) {
    try {
      NetworkServiceRecordAgent nsrAgent = requestor.getNetworkServiceRecordAgent();
      List<NetworkServiceRecord> nsrList = nsrAgent.findAll();
      for (NetworkServiceRecord nsr : nsrList) {
        try {
          nsrAgent.delete(nsr.getId());
        } catch (SDKException se) {
          log.error("Could not remove NSR " + nsr.getName() + " with ID " + nsr.getId());
        }
      }
      Thread.sleep(1000);
      NetworkServiceDescriptorAgent nsdAgent = requestor.getNetworkServiceDescriptorAgent();
      List<NetworkServiceDescriptor> nsdList = nsdAgent.findAll();
      for (NetworkServiceDescriptor nsd : nsdList) {
        try {
          nsdAgent.delete(nsd.getId());
        } catch (SDKException se) {
          log.error("Could not remove NSD " + nsd.getName() + " with ID " + nsd.getId());
        }
      }
      Thread.sleep(1000);
      VirtualNetworkFunctionDescriptorAgent vnfdAgent =
          requestor.getVirtualNetworkFunctionDescriptorAgent();

      List<VirtualNetworkFunctionDescriptor> vnfdList = vnfdAgent.findAll();
      for (VirtualNetworkFunctionDescriptor vnfd : vnfdList) {
        vnfdAgent.delete(vnfd.getId());
      }
      Thread.sleep(1000);
      VNFPackageAgent packageAgent = requestor.getVNFPackageAgent();
      List<VNFPackage> packageList = packageAgent.findAll();
      for (VNFPackage p : packageList) {
        try {
          packageAgent.delete(p.getId());
        } catch (SDKException se) {
          log.error("Could not remove VNFPackage " + p.getName() + " with ID " + p.getId());
        }
      }
      VimInstanceAgent vimAgent = requestor.getVimInstanceAgent();
      List<BaseVimInstance> vimList = vimAgent.findAll();
      for (BaseVimInstance vim : vimList) {
        try {
          vimAgent.delete(vim.getId());
        } catch (SDKException se) {
          log.error("Could not remove VIM " + vim.getName() + " with ID " + vim.getId());
        }
      }
    } catch (InterruptedException ie) {
      log.error("Could not clear the NFVO due to an InterruptedException");
    } catch (Exception e) {
      log.error("Could not clear the NFVO. \nException message is: " + e.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    Properties properties = null;
    try {
      properties = loadProperties();
    } catch (IOException e) {
      e.printStackTrace();
      log.error(e.getMessage());
      System.exit(42);
    }

    log.debug("Current properties set: " + properties);

    // Checking if the NFVO is running
    if (!Utils.isNfvoStarted(nfvoIp, nfvoPort)) {
      log.error("After 120 sec the NFVO is not started yet. Is there an error?");
      System.exit(1);
    }

    NFVORequestor requestor =
        new NFVORequestor(nfvoUsr, nfvoPwd, sslEnabled, projectName, nfvoIp, nfvoPort, "1");

    if (args.length > 0 && args[0].equals("clean")) {
      log.info("Executing clean up of existing descriptors/records ");
      clearOrchestrator(requestor);
      System.exit(0);
    }

    // Checking command line arguments
    List<String> clArgs = Arrays.asList(args);
    List<URL> iniFileURLs =
        Utils.getURLFileList(properties.getProperty("integration-test-scenarios"));

    // Check if arguments passed are correct
    if (clArgs.size() > 0) {
      List<String> fileNames = Utils.getFileNames(iniFileURLs);
      for (String arg : clArgs) {
        if (!fileNames.contains(arg)) {
          log.warn(
              "The scenario name passed as argument "
                  + arg
                  + " does not refer to any existing scenarios in integration-test-scenarios folder");
        }
      }
    }

    log.info("NFVO is reachable at " + nfvoIp + ":" + nfvoPort + ". Loading tests");

    IntegrationTestManager itm =
        new IntegrationTestManager(
            "org.openbaton.integration.test.testers", requestor, projectName);

    long startTime, stopTime;
    boolean allTestsPassed = true;
    Map<String, String> results = new HashMap<>();
    boolean executedTests =
        false; // shows that there was at least one test executed by the integration test
    for (URL url : iniFileURLs) {
      String[] splittedUrl = url.toString().split("/");
      String name = splittedUrl[splittedUrl.length - 1];
      if (clArgs.size() > 0
          && !clArgs.contains(
              name)) // if test names are passed through the command line, only these will be executed
      {
        continue;
      }
      executedTests = true;
      startTime = System.currentTimeMillis();
      if (itm.runTestScenario(properties, url, name)) {
        stopTime = System.currentTimeMillis() - startTime;
        log.info(
            "Test: "
                + name
                + " finished correctly :) in "
                + String.format(
                    "%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(stopTime),
                    TimeUnit.MILLISECONDS.toSeconds(stopTime)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(stopTime)))
                + "\n");
        results.put(name, "SUCCESS");
      } else {
        log.error("Test: " + name + " completed with errors :(\n");
        allTestsPassed = false;
        results.put(name, "FAILED");
      }
      if (clearAfterTest) {
        clearOrchestrator(requestor);
      }
    }
    if (!executedTests) {
      log.warn("No tests were executed.");
      System.exit(1);
    }
    log.info("Final results of the execution of the tests: \n");
    String[] columns = {"Scenario Name", "Result"};
    Utils.getResultsTable(columns, results).printTable();
    System.out.println();
    if (allTestsPassed) {
      System.exit(0);
    } else {
      System.exit(99);
    }
  }
}
