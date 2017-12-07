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

import org.ini4j.Profile;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.testers.GenericServiceTester;
import org.openbaton.integration.test.testers.NetworkServiceDescriptorCreate;
import org.openbaton.integration.test.testers.NetworkServiceDescriptorDelete;
import org.openbaton.integration.test.testers.NetworkServiceDescriptorWait;
import org.openbaton.integration.test.testers.NetworkServiceRecordCreate;
import org.openbaton.integration.test.testers.NetworkServiceRecordDelete;
import org.openbaton.integration.test.testers.NetworkServiceRecordWait;
import org.openbaton.integration.test.testers.PackageDelete;
import org.openbaton.integration.test.testers.PackageUpload;
import org.openbaton.integration.test.testers.Pause;
import org.openbaton.integration.test.testers.ProjectCreate;
import org.openbaton.integration.test.testers.ProjectDelete;
import org.openbaton.integration.test.testers.ScaleIn;
import org.openbaton.integration.test.testers.ScaleOut;
import org.openbaton.integration.test.testers.ScalingTester;
import org.openbaton.integration.test.testers.UserCreate;
import org.openbaton.integration.test.testers.UserDelete;
import org.openbaton.integration.test.testers.UserUpdate;
import org.openbaton.integration.test.testers.VNFRStatusTester;
import org.openbaton.integration.test.testers.VimInstanceCreate;
import org.openbaton.integration.test.testers.VimInstanceDelete;
import org.openbaton.integration.test.testers.VirtualNetworkFunctionDescriptorDelete;
import org.openbaton.integration.test.testers.VirtualNetworkFunctionRecordWait;
import org.openbaton.integration.test.utils.SubTask;
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

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);
  private static String nfvoIp;
  private static String nfvoPort;
  private static String nfvoUsr;
  private static String nfvoPwd;
  private static String projectId;
  private static boolean sslEnabled;
  private static boolean clearAfterTest = false;

  private static Properties loadProperties()
      throws IOException, SDKException, ClassNotFoundException {
    Properties properties = Utils.getProperties();
    nfvoIp = properties.getProperty("nfvo-ip");
    nfvoPort = properties.getProperty("nfvo-port");
    nfvoUsr = properties.getProperty("nfvo-usr");
    nfvoPwd = properties.getProperty("nfvo-pwd");
    projectId = properties.getProperty("nfvo-project-id", null);
    sslEnabled = Boolean.parseBoolean(properties.getProperty("nfvo-ssl-enabled"));
    if (projectId == null) {
      projectId = Utils.findProjectId(nfvoIp, nfvoPort, nfvoUsr, nfvoPwd, sslEnabled);
      properties.setProperty("nfvo-project-id", projectId);
    }
    String clear = properties.getProperty("clear-after-test");
    clearAfterTest = Boolean.parseBoolean(clear);
    return properties;
  }

  public static void main(String[] args) throws Exception {
    Properties properties = null;
    try {
      properties = loadProperties();
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<String> clArgs = Arrays.asList(args);

    /******************************
     * Running NFVO *
     ******************************/
    if (!Utils.isNfvoStarted(nfvoIp, nfvoPort)) {
      log.error("After 120 sec the Nfvo is not started yet. Is there an error?");
      System.exit(1);
    }

    log.info("Nfvo is started");

    /******************************
     * Now create the VIM *
     ******************************/
    log.debug("Properties: " + properties);

    List<URL> iniFileURLs = Utils.loadFileIni(properties);

    List<String> fileNames = new LinkedList<>();
    for (URL url : iniFileURLs) {
      String[] splittedUrl = url.toString().split("/");
      String name = splittedUrl[splittedUrl.length - 1];
      fileNames.add(name);
    }

    // check if arguments are wrong
    if (clArgs.size() > 0) {
      for (String arg : clArgs) {
        if (arg.equals("clean")) {
          log.info("Execute clean up of existing descriptors/records and exit");
          clearOrchestrator();
          System.exit(0);
        }
        if (!fileNames.contains(arg)) {
          log.warn("The argument " + arg + " does not specify an existing test scenario.");
        }
      }
    }

    IntegrationTestManager itm =
        new IntegrationTestManager("org.openbaton.integration.test.testers") {
          @Override
          protected void configureSubTask(SubTask subTask, Profile.Section currentSection) {
            subTask.setProjectId(projectId);
            if (subTask instanceof VimInstanceCreate) {
              TaskConfigurator.configureVimInstanceCreate(subTask, currentSection);
            }
            if (subTask instanceof VimInstanceDelete) {
              TaskConfigurator.configureVimInstanceDelete(subTask, currentSection);
            } else if (subTask instanceof NetworkServiceDescriptorCreate) {
              TaskConfigurator.configureNetworkServiceDescriptorCreate(subTask, currentSection);
            } else if (subTask instanceof NetworkServiceDescriptorDelete) {
              TaskConfigurator.configureNetworkServiceDescriptorDelete(subTask, currentSection);
            } else if (subTask instanceof VirtualNetworkFunctionDescriptorDelete) {
              TaskConfigurator.configureVirtualNetworkFunctionDescriptorDelete(
                  subTask, currentSection);
            } else if (subTask instanceof NetworkServiceDescriptorWait) {
              try {
                TaskConfigurator.configureNetworkServiceDescriptorWait(subTask, currentSection);
              } catch (IntegrationTestException e) {
                log.error(e.getMessage());
                System.exit(42);
              }
            } else if (subTask instanceof NetworkServiceRecordDelete) {
              TaskConfigurator.configureNetworkServiceRecordDelete(subTask, currentSection);
            } else if (subTask instanceof NetworkServiceRecordCreate) {
              TaskConfigurator.configureNetworkServiceRecordCreate(subTask, currentSection);
            } else if (subTask instanceof NetworkServiceRecordWait) {
              try {
                TaskConfigurator.configureNetworkServiceRecordWait(subTask, currentSection);
              } catch (IntegrationTestException e) {
                log.error(e.getMessage());
                System.exit(42);
              }
            } else if (subTask instanceof VirtualNetworkFunctionRecordWait) {
              try {
                TaskConfigurator.configureVirtualNetworkFunctionRecordWait(subTask, currentSection);
              } catch (IntegrationTestException e) {
                log.error(e.getMessage());
                System.exit(42);
              }
            } else if (subTask instanceof GenericServiceTester) {
              TaskConfigurator.configureGenericServiceTester(subTask, currentSection);
            } else if (subTask instanceof ScaleOut) {
              TaskConfigurator.configureScaleOut(subTask, currentSection);
            } else if (subTask instanceof ScaleIn) {
              TaskConfigurator.configureScaleIn(subTask, currentSection);
            } else if (subTask instanceof ScalingTester) {
              TaskConfigurator.configureScalingTester(subTask, currentSection);
            } else if (subTask instanceof PackageUpload) {
              TaskConfigurator.configurePackageUpload(subTask, currentSection);
            } else if (subTask instanceof PackageDelete) {
              TaskConfigurator.configurePackageDelete(subTask, currentSection);
            } else if (subTask instanceof VNFRStatusTester) {
              TaskConfigurator.configureVnfrStatusTester(subTask, currentSection);
            } else if (subTask instanceof Pause) {
              try {
                TaskConfigurator.configurePause(subTask, currentSection);
              } catch (IntegrationTestException e) {
                log.error(e.getMessage());
                System.exit(42);
              }
            } else if (subTask instanceof UserCreate) {
              TaskConfigurator.configureUserCreate(subTask, currentSection);
            } else if (subTask instanceof UserDelete) {
              TaskConfigurator.configureUserDelete(subTask, currentSection);
            } else if (subTask instanceof UserUpdate) {
              TaskConfigurator.configureUserUpdate(subTask, currentSection);
            } else if (subTask instanceof ProjectCreate) {
              TaskConfigurator.configureProjectCreate(subTask, currentSection);
            } else if (subTask instanceof ProjectDelete) {
              TaskConfigurator.configureProjectDelete(subTask, currentSection);
            }
          }
        };
    itm.setLogger(log);
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
        clearOrchestrator();
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

  /**
   * This method tries to remove every NSD, VNFD, VNFPackage and VIM from the orchestrator.
   */
  private static void clearOrchestrator() {
    try {
      NFVORequestor requestor =
          new NFVORequestor(nfvoUsr, nfvoPwd, projectId, sslEnabled, nfvoIp, nfvoPort, "1");
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
      List<VimInstance> vimList = vimAgent.findAll();
      for (VimInstance vim : vimList) {
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

  private static void exit(int i) {
    System.exit(i);
  }
}
