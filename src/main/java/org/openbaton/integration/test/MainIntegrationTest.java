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
package org.openbaton.integration.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.ini4j.Profile;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.integration.test.testers.*;
import org.openbaton.integration.test.utils.SubTask;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.NetworkServiceDescriptorRestAgent;
import org.openbaton.sdk.api.rest.NetworkServiceRecordRestAgent;
import org.openbaton.sdk.api.rest.VimInstanceRestAgent;
import org.openbaton.sdk.api.util.AbstractRestAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainIntegrationTest {


    private final static String SCENARIO_PATH = "/integration-test-scenarios/";
    private static Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);
    private static String nfvoIp;
    private static String nfvoPort;
    private static String nfvoUsr;
    private static String nfvoPsw;
    private static boolean clearAfterTest = false;

    private static Properties loadProperties() throws IOException {
        Properties properties = Utils.getProperties();
        nfvoIp = properties.getProperty("nfvo-ip");
        nfvoPort = properties.getProperty("nfvo-port");
        nfvoUsr = properties.getProperty("nfvo-usr");
        nfvoPsw = properties.getProperty("nfvo-psw");
        String clear = properties.getProperty("clear-after-test");
        if (clear != null) {
            try {
                clearAfterTest = Boolean.parseBoolean(clear);
            } catch (Exception e) {
                log.warn("The property field 'clear-after-test' is not 'true' or 'false' and will not be taken into account.");
            }
        }
        return properties;
    }


    private static boolean isNfvoStarted(String nfvoIp, String nfvoPort) {
        int i = 0;
        while (!Utils.available(nfvoIp, nfvoPort)) {
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

    private static boolean areVnfmsRegistered(String nfvoIp, String nfvoPort) {
        int i = 0;
        boolean registered = false;
        while (!registered) {
            HttpResponse<String> r = null;
            try {
                r = Unirest.get("http://" + nfvoIp + ":" + nfvoPort + "/api/v1/vnfmanagers").asString();
            } catch (UnirestException e) {
                log.error("Could not reach NFVO. Is it really running and are the integration-test.properties correct?");
                return false;
            }
            Gson mapper = new GsonBuilder().setPrettyPrinting().create();
            JsonArray vnfmArray = null;
            String body = null;
            try {
                body = r.getBody();
            } catch (NullPointerException e) {
                log.error("Something went wrong asking the NFVO for the registrated VNFMs.");
                return false;
            }
            vnfmArray = mapper.fromJson(body, JsonArray.class);
            if (vnfmArray.size() > 0)
                registered = true;
            if (i >= 20) {
                if (!registered)
                    log.error("After 60 seconds no VNFM is registered yet to the NFVO. Is there an error?");
                return false;
            }
            i++;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    public static void main(String[] args) throws Exception {

        System.out.println(log.getClass());
        Properties properties = null;
        try {
            properties = loadProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> clArgs = Arrays.asList(args);

        /******************************
         * Running NFVO				  *
         ******************************/

        if (!isNfvoStarted(nfvoIp, nfvoPort)) {
            log.error("After 120 sec the Nfvo is not started yet. Is there an error?");
            System.exit(1);
        }

        log.info("Nfvo is started");

        /******************************
         * Running VNFMs				  *
         ******************************/

        if (!areVnfmsRegistered(nfvoIp, nfvoPort)) {
            log.error("No VNFM is registered yet.");
            System.exit(1);
        }

        log.info("A VNFM is registered");

        /******************************
         * Now create the VIM		  *
         ******************************/

        log.debug("Properties: " + properties);


        List<URL> iniFileURLs = loadFileIni(properties);

        // check if arguments are wrong
        if (clArgs.size() > 0) {
            List<String> fileNames = new LinkedList<>();
            for (URL url : iniFileURLs) {
                String[] splittedUrl = url.toString().split("/");
                String name = splittedUrl[splittedUrl.length - 1];
                fileNames.add(name);
            }
            for (String arg : clArgs) {
                if (!fileNames.contains(arg))
                    log.warn("The argument " + arg + " does not specify an existing test scenario.");
            }
        }

        IntegrationTestManager itm = new IntegrationTestManager("org.openbaton.integration.test.testers") {
            @Override
            protected void configureSubTask(SubTask subTask, Profile.Section currentSection) {
                if (subTask instanceof VimInstanceCreate)
                    configureVimInstanceCreate(subTask, currentSection);
                else if (subTask instanceof NetworkServiceDescriptorCreate)
                    configureNetworkServiceDescriptorCreate(subTask, currentSection);
                else if (subTask instanceof NetworkServiceDescriptorDelete)
                    configureNetworkServiceDescriptorDelete(subTask, currentSection);
                else if (subTask instanceof VirtualNetworkFunctionDescriptorDelete)
                    configureVirtualNetworkFunctionDescriptorDelete(subTask, currentSection);
                else if (subTask instanceof NetworkServiceDescriptorWait)
                    configureNetworkServiceDescriptorWaiterWait(subTask, currentSection);
                else if (subTask instanceof NetworkServiceRecordDelete)
                    configureNetworkServiceRecordDelete(subTask, currentSection);
                else if (subTask instanceof NetworkServiceRecordCreate)
                    configureNetworkServiceRecordCreate(subTask, currentSection);
                else if (subTask instanceof NetworkServiceRecordWait)
                    configureNetworkServiceRecordWait(subTask, currentSection);
                else if (subTask instanceof VirtualNetworkFunctionRecordWait)
                    configureVirtualNetworkFunctionRecordWait(subTask, currentSection);
                else if (subTask instanceof GenericServiceTester)
                    configureGenericServiceTester(subTask, currentSection);
                else if (subTask instanceof ScaleOut)
                    configureScaleOut(subTask, currentSection);
                else if (subTask instanceof ScaleIn)
                    configureScaleIn(subTask, currentSection);
                else if (subTask instanceof ScalingTester)
                    configureScalingTester(subTask, currentSection);
                else if (subTask instanceof PackageUpload)
                    configurePackageUpload(subTask, currentSection);
                else if (subTask instanceof PackageDelete)
                    configurePackageDelete(subTask, currentSection);
                else if (subTask instanceof VNFRStatusTester)
                    configureVnfrStatusTester(subTask, currentSection);
                else if (subTask instanceof Pause)
                    configurePause(subTask, currentSection);
            }
        };
        itm.setLogger(log);
        long startTime, stopTime;
        boolean allTestsPassed = true;
        boolean executedTests = false; // shows that there was at least one test executed by the integration test
        for (URL url : iniFileURLs) {
            String[] splittedUrl = url.toString().split("/");
            String name = splittedUrl[splittedUrl.length - 1];
            if (clArgs.size() > 0 && !clArgs.contains(name)) // if test names are passed through the command line, only these will be executed
                continue;
            executedTests = true;
            startTime = System.currentTimeMillis();
            if (itm.runTestScenario(properties, url, name)) {
                stopTime = System.currentTimeMillis() - startTime;
                log.info("Test: " + name + " finished correctly :) in " +
                        String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(stopTime), TimeUnit.MILLISECONDS.toSeconds(stopTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(stopTime))) + "\n");
            } else {
                log.error("Test: " + name + " completed with errors :(\n");
                allTestsPassed = false;
            }
            if (clearAfterTest)
                clearOrchestrator();

        }
        if (!executedTests) {
            log.warn("No tests were executed.");
            System.exit(1);
        }
        if (allTestsPassed) {
            log.info("All tests passed successfully.");
            System.exit(0);
        } else {
            log.info("Some tests failed.");
            System.exit(99);
        }
    }

    /**
     * This method tries to remove every NSD, VNFD, VNFPackage and VIM from the orchestrator.
     */
    private static void clearOrchestrator() {
        try {
            NFVORequestor requestor = new NFVORequestor(nfvoUsr, nfvoPsw, nfvoIp, nfvoPort, "1");
            NetworkServiceRecordRestAgent nsrAgent = requestor.getNetworkServiceRecordAgent();
            List<NetworkServiceRecord> nsrList = nsrAgent.findAll();
            for (NetworkServiceRecord nsr : nsrList) {
                try {
                    nsrAgent.delete(nsr.getId());
                } catch (SDKException se) {
                    log.error("Could not remove NSR " + nsr.getName() + " with ID " + nsr.getId());
                }
            }
            Thread.sleep(1000);
            NetworkServiceDescriptorRestAgent nsdAgent = requestor.getNetworkServiceDescriptorAgent();
            List<NetworkServiceDescriptor> nsdList = nsdAgent.findAll();
            for (NetworkServiceDescriptor nsd : nsdList) {
                try {
                    nsdAgent.delete(nsd.getId());
                } catch (SDKException se) {
                    log.error("Could not remove NSD " + nsd.getName() + " with ID " + nsd.getId());
                }
            }
            Thread.sleep(1000);
            AbstractRestAgent vnfdAgent = requestor.abstractRestAgent(VirtualNetworkFunctionDescriptor.class, "/vnf-descriptors");
            List<VirtualNetworkFunctionDescriptor> vnfdList = vnfdAgent.findAll();
            for (VirtualNetworkFunctionDescriptor vnfd : vnfdList) {
                vnfdAgent.delete(vnfd.getId());
            }
            Thread.sleep(1000);
            AbstractRestAgent packageAgent = requestor.abstractRestAgent(VNFPackage.class, "/vnf-packages");
            List<VNFPackage> packageList = packageAgent.findAll();
            for (VNFPackage p : packageList) {
                try {
                    packageAgent.delete(p.getId());
                } catch (SDKException se) {
                    log.error("Could not remove VNFPackage " + p.getName() + " with ID " + p.getId());
                }
            }
            VimInstanceRestAgent vimAgent = requestor.getVimInstanceAgent();
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


    private static List<URL> loadFileIni(Properties properties) throws FileNotFoundException {
        List<URL> external = Utils.getFilesAsURL(properties.getProperty("integration-test-scenarios", SCENARIO_PATH) + "*.ini");
        if (external.size() > 0)
            return external;
        //if there are no files on the machine, us the scenarios in the project's resource folder
        return Utils.getFilesAsURL(SCENARIO_PATH + "*.ini");
    }

    private static void configureNetworkServiceDescriptorWaiterWait(SubTask instance, Profile.Section currentSection) {
        //cast and get specific properties
    }

    private static void configureNetworkServiceDescriptorDelete(SubTask instance, Profile.Section currentSection) {
        //cast and get specific properties
    }

    private static void configureVirtualNetworkFunctionDescriptorDelete(SubTask subtask, Profile.Section currentSection) {
        VirtualNetworkFunctionDescriptorDelete w = (VirtualNetworkFunctionDescriptorDelete) subtask;
        String vnfdType = currentSection.get("vnf-type");
        String vnfdName = currentSection.get("vnf-name");
        if (vnfdType != null)
            w.setVnfdType(vnfdType);
        if (vnfdName != null)
            w.setVnfdName(vnfdName);
    }

    private static void configureVirtualNetworkFunctionRecordWait(SubTask subTask, Profile.Section currentSection) {
        VirtualNetworkFunctionRecordWait w = (VirtualNetworkFunctionRecordWait) subTask;
        w.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));

        String action = currentSection.get("action");
        String vnfType = currentSection.get("vnf-type");
        if (action == null || action.isEmpty()) {
            log.error("action for VirtualNetworkFunctionRecordWait not set");
            exit(3);
        }
        if (vnfType == null || vnfType.isEmpty()) {
            log.error("vnf-type property not set");
            exit(3);
        }
        w.setAction(Action.valueOf(action));
        w.setVnfrType(vnfType);
    }

    private static void configureNetworkServiceRecordWait(SubTask instance, Profile.Section currentSection) {
        NetworkServiceRecordWait w = (NetworkServiceRecordWait) instance;
        w.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));

        String action = currentSection.get("action");
        if (action == null) {
            log.error("action for NetworkServiceRecordWait not set");
            exit(3);
        }
        w.setAction(Action.valueOf(action));
    }

    private static void configureNetworkServiceRecordCreate(SubTask instance, Profile.Section currentSection) {
        //cast and get specific properties
    }

    private static void configureNetworkServiceRecordDelete(SubTask instance, Profile.Section currentSection) {
        //cast and get specific properties
    }

    private static void configureNetworkServiceDescriptorCreate(SubTask instance, Profile.Section currentSection) {
        NetworkServiceDescriptorCreate w = (NetworkServiceDescriptorCreate) instance;
        w.setFileName(currentSection.get("name-file"));
        w.setExpectedToFail(currentSection.get("expected-to-fail"));
    }


    private static void configureVimInstanceCreate(SubTask instance, Profile.Section currentSection) {
        VimInstanceCreate w = (VimInstanceCreate) instance;
        w.setFileName(currentSection.get("name-file"));
    }

    private static void configureGenericServiceTester(SubTask subTask, Profile.Section currentSection) {
        GenericServiceTester t = (GenericServiceTester) subTask;
        Boolean stop = false;
        String vnfrType = currentSection.get("vnf-type");
        String vmScriptsPath = currentSection.get("vm-scripts-path");
        String user = currentSection.get("user-name");
        if (vnfrType != null)
            t.setVnfrType(vnfrType);

        if (vmScriptsPath != null)
            t.setVmScriptsPath(vmScriptsPath);

        String netName = currentSection.get("net-name");
        if (netName != null)
            t.setVirtualLink(netName);

        if (user != null)
            t.setUserName(user);

        for (int i = 1; !stop; i++) {
            String scriptName = currentSection.get("script-" + i);
            if (scriptName == null || scriptName.isEmpty()) {
                stop = true;
                continue;
            }
            t.addScript(scriptName);
        }
    }

    private static void configureScaleOut(SubTask subTask, Profile.Section currentSection) {
        ScaleOut t = (ScaleOut) subTask;
        String vnfrType = currentSection.get("vnf-type");
        String virtualLink = currentSection.get("virtual-link");
        String floatingIp = currentSection.get("floating-ip");
        if (vnfrType != null)
            t.setVnfrType(vnfrType);

        if (virtualLink != null)
            t.setVirtualLink(virtualLink);

        if (floatingIp != null)
            t.setFloatingIp(floatingIp);
    }

    private static void configureScaleIn(SubTask subTask, Profile.Section currentSection) {
        ScaleIn t = (ScaleIn) subTask;
        String vnfrType = currentSection.get("vnf-type");
        if (vnfrType != null)
            t.setVnfrType(vnfrType);
    }

    private static void configureScalingTester(SubTask subTask, Profile.Section currentSection) {
        ScalingTester t = (ScalingTester) subTask;
        String vnfrType = currentSection.get("vnf-type");
        String vnfcCount = currentSection.get("vnfc-count");
        if (vnfrType != null)
            t.setVnfrType(vnfrType);

        if (vnfcCount != null)
            t.setVnfcCount(vnfcCount);
    }

    private static void configurePackageUpload(SubTask instance, Profile.Section currentSection) {
        PackageUpload p = (PackageUpload) instance;
        p.setPackageName(currentSection.get("package-name"));
    }

    private static void configurePackageDelete(SubTask instance, Profile.Section currentSection) {
        PackageDelete p = (PackageDelete) instance;
        p.setPackageName(currentSection.get("package-name"));
    }

    private static void configureVnfrStatusTester(SubTask instance, Profile.Section currentSection) {
        VNFRStatusTester t = (VNFRStatusTester) instance;
        String status = currentSection.get("status");
        if (status != null)
            t.setStatus(status);

        String vnfrType = currentSection.get("vnf-type");
        if (vnfrType != null)
            t.setVnfrType(vnfrType);
    }

    private static void configurePause(SubTask instance, Profile.Section currentSection) {
        Pause p = (Pause) instance;
        String d = currentSection.get("duration");
        try {
            int duration = Integer.parseInt(d);
            p.setDuration(duration);
        } catch (NumberFormatException e) {
            log.warn("The duration field of Pause is not an integer so we cannot use it");
        }
    }

    private static void exit(int i) {
        System.exit(i);
    }
}