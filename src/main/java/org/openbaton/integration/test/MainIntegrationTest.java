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
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.integration.test.testers.*;
import org.openbaton.integration.test.utils.SubTask;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.rest.NetworkServiceDescriptorRestAgent;
import org.openbaton.sdk.api.rest.NetworkServiceRecordRestAgent;
import org.openbaton.sdk.api.rest.VimInstanceRestAgent;
import org.openbaton.sdk.api.util.AbstractRestAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
        boolean generic = false;
        boolean dummy = false;
        while (!generic || !dummy) {
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
            Iterator<JsonElement> vnfmIt = vnfmArray.iterator();
            while (vnfmIt.hasNext()) {
                JsonElement type = vnfmIt.next().getAsJsonObject().get("type");
                if (type.getAsString().equals("generic"))
                    generic = true;
                if (type.getAsString().equals("dummy"))
                    dummy = true;
            }
            if (i >= 20) {
                if (!generic)
                    log.error("After 60 seconds the Generic VNFM is not yet registered to the NFVO. Is there an error?");
                if (!dummy)
                    log.error("After 60 seconds the Dummy VNFM is not yet registered to the NFVO. Is there an error?");
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
            log.error("The Generic or the Dummy VNFM are not registered yet.");
            System.exit(1);
        }

        log.info("VNFMs are registered");

        /******************************
         * Now create the VIM		  *
         ******************************/

        log.debug("Properties: " + properties);


        List<URL> iniFileURLs = loadFileIni(properties);
        System.out.println(iniFileURLs);
        IntegrationTestManager itm = new IntegrationTestManager("org.openbaton.integration.test.testers") {
            @Override
            protected void configureSubTask(SubTask subTask, Profile.Section currentSection) {
                if (subTask instanceof VimInstanceCreate)
                    configureVimInstanceCreate(subTask, currentSection);
                else if (subTask instanceof NetworkServiceDescriptorCreate)
                    configureNetworkServiceDescriptorCreate(subTask, currentSection);
                else if (subTask instanceof NetworkServiceDescriptorDelete)
                    configureNetworkServiceDescriptorDelete(subTask, currentSection);
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
            }
        };
        itm.setLogger(log);
        long startTime, stopTime;
        boolean allTestsPassed = true;
        for (URL url : iniFileURLs) {
            String[] splittedUrl = url.toString().split("/");
            String name = splittedUrl[splittedUrl.length - 1];
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
        if (allTestsPassed) {
            log.info("All tests passed successfully.");
            System.exit(0);
        } else {
            log.info("Some tests failed.");
            System.exit(99);
        }
    }

    private static void clearOrchestrator() {
        try {
            NFVORequestor requestor = new NFVORequestor(nfvoUsr, nfvoPsw, nfvoIp, nfvoPort, "1");
            NetworkServiceRecordRestAgent nsrAgent = requestor.getNetworkServiceRecordAgent();
            List<NetworkServiceRecord> nsrList = nsrAgent.findAll();
            for (NetworkServiceRecord nsr : nsrList)
                nsrAgent.delete(nsr.getId());
            NetworkServiceDescriptorRestAgent nsdAgent = requestor.getNetworkServiceDescriptorAgent();
            List<NetworkServiceDescriptor> nsdList = nsdAgent.findAll();
            for (NetworkServiceDescriptor nsd : nsdList)
                nsdAgent.delete(nsd.getId());
            AbstractRestAgent packageAgent = requestor.abstractRestAgent(VNFPackage.class, "/vnf-packages");
            List<VNFPackage> packageList = packageAgent.findAll();
            for (VNFPackage p : packageList)
                packageAgent.delete(p.getId());
            VimInstanceRestAgent vimAgent = requestor.getVimInstanceAgent();
            List<VimInstance> vimList = vimAgent.findAll();
            for (VimInstance vim : vimList)
                vimAgent.delete(vim.getId());
        } catch (Exception e) {
            log.warn("Could not clear the NFVO.");
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

    private static void exit(int i) {
        System.exit(i);
    }
}