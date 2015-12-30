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

import org.ini4j.Profile;
import org.openbaton.integration.test.testers.*;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.integration.test.utils.SubTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class MainIntegrationTest {


    private static Logger log = LoggerFactory.getLogger(MainIntegrationTest.class);

    private static String nfvoIp;
    private static String nfvoPort;
    private static String nfvoUsr;
    private static String nfvoPsw;
    private final static String SCENARIO_PATH = "/integration-test-scenarios/";

    private static Properties loadProperties() throws IOException {
        Properties properties = Utils.getProperties();
        nfvoIp = properties.getProperty("nfvo-ip");
        nfvoPort = properties.getProperty("nfvo-port");
        nfvoUsr = properties.getProperty("nfvo-usr");
        nfvoPsw = properties.getProperty("nfvo-psw");
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
            if (i > 50) {
                return false;
            }

        }
        return true;
    }


    public static void main(String[] args) throws IOException {

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
            log.error("After 150 sec the Nfvo is not started yet. Is there an error?");
            System.exit(1); // 1 stands for the error in running nfvo
        }

        log.info("Nfvo is started");


        /******************************
         * Now create the VIM		  *
         ******************************/

        log.debug("Properties: " + properties);


        List<URL> iniFileURLs = loadFileIni();
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
                else if (subTask instanceof PackageUpload)
                    configurePackageUpload(subTask, currentSection);
                else if (subTask instanceof PackageDelete)
                    configurePackageDelete(subTask, currentSection);
            }
        };
        itm.setLogger(log);
        long startTime, stopTime;
        for (URL url : iniFileURLs) {
            String[] splittedUrl = url.toString().split("/");
            String name = splittedUrl[splittedUrl.length - 1];
            startTime = System.currentTimeMillis();
            if (itm.runTestScenario(properties, url, name)) {
                stopTime = System.currentTimeMillis() - startTime;
                log.info("Test: " + name + " finished correctly :) in " +
                        String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(stopTime), TimeUnit.MILLISECONDS.toSeconds(stopTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(stopTime))) + "\n");
            } else log.info("Test: " + name + " completed with errors :(\n");
        }
        System.exit(0);
    }


    private static List<URL> loadFileIni() throws FileNotFoundException {
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
        String vnfName = currentSection.get("vnf-name");
        if (action == null || action.isEmpty()) {
            log.error("action for VirtualNetworkFunctionRecordWait not setted");
            exit(3);
        }
        if (vnfName == null || vnfName.isEmpty()) {
            log.error("vnf-name property not setted not setted");
            exit(3);
        }
        w.setAction(Action.valueOf(action));
        w.setVnfrName(vnfName);
    }

    private static void configureNetworkServiceRecordWait(SubTask instance, Profile.Section currentSection) {
        NetworkServiceRecordWait w = (NetworkServiceRecordWait) instance;
        w.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));

        String action = currentSection.get("action");
        if (action == null) {
            log.error("action for NetworkServiceRecordWait not setted");
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
        String vnfrName = currentSection.get("vnf-name");
        String user = currentSection.get("user-name");
        if (vnfrType != null)
            t.setVnfrType(vnfrType);

        if (vnfrName != null)
            t.setVnfrName(vnfrName);

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