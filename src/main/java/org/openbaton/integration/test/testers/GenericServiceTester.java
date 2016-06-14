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
package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.VNFCRepresentation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tbr on 25.11.15.
 */
public class GenericServiceTester extends Tester {

    private static final String LOCAL_PATH_NAME_SCRIPTS = "/etc/scripts/";
    private static final String EXTERNAL_PATH_NAME_SCRIPTS = "/etc/openbaton/integration-test/scripts/";
    private static final String EXTERNAL_PATH_NAME_PEM = "/etc/openbaton/integration-test/integration-test.pem";

    private List<File> scripts = new LinkedList<File>();
    private Map<String, List<VNFCRepresentation>> vnfrVnfc = new HashMap<>();
    private String vnfrType = "";
    private String virtualLink = "";
    private String userName = "";
    private String vmScriptsPath = "";

    public GenericServiceTester(Properties properties) {
        super(properties, GenericServiceTester.class, LOCAL_PATH_NAME_SCRIPTS, "");
    }


    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();
        fillVnfrVnfc(nsr);
        List<String> floatingIps = new LinkedList<>();

        File pemFile = new File(EXTERNAL_PATH_NAME_PEM);
        Runtime r = Runtime.getRuntime();

        for (String k : vnfrVnfc.keySet()) {
            for (VNFCRepresentation v : vnfrVnfc.get(k)) {
            }
        }

        if (virtualLink.equals("")) {
            if (vnfrVnfc.containsKey(vnfrType)) {
                for (VNFCRepresentation vnfc : vnfrVnfc.get(vnfrType)) {
                    if (vnfc.getAllFips() != null)
                        floatingIps.addAll(vnfc.getAllFips());

                }
            }
        } else {
            if (vnfrVnfc.containsKey(vnfrType)) {
                for (VNFCRepresentation vnfc : vnfrVnfc.get(vnfrType)) {
                    if (vnfc.getFipsByNet(virtualLink) != null)
                        floatingIps.addAll(vnfc.getFipsByNet(virtualLink));
                }
            }
        }


        Iterator<String> floatingIpIt = floatingIps.iterator();
        if (!virtualLink.equals("")) {
            if (floatingIps.size() == 0) {
                log.warn("Found no floating IPs for virtual machines of vnf-type " + vnfrType + " with virtual_link: " + virtualLink + ".");
            } else {
                log.info("Start testing the virtual machines of vnf-type " + vnfrType + " with virtual_link " + virtualLink + ".");
            }
        } else {
            if (floatingIps.size() == 0) {
                log.warn("Found no floating IPs for virtual machines of vnf-type " + vnfrType + ".");
            } else {
                log.info("Start testing the virtual machines of vnf-type " + vnfrType + ".");
            }
        }


        while (floatingIpIt.hasNext()) {
            String floatingIp = floatingIpIt.next();
            Iterator<File> scriptIterator = scripts.iterator();
            while (scriptIterator.hasNext()) {
                File script = scriptIterator.next();
                String scriptContent = getContentOfScript(script.getPath());
                List<String> preScripts = getPreScripts(scriptContent);
                if (preScripts.size() == 0)
                    preScripts.add("");
                Iterator<String> preScriptIt = preScripts.iterator();
                while (preScriptIt.hasNext()) {
                    String preScript = preScriptIt.next();
                    log.info("Executing script " + script.getName() + " on the virtual machine with floating ip: " + floatingIp + "\nwith environment: \n" + preScript);

                    //store script on VM
                    ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "scp -o \"StrictHostKeyChecking no\"" + " -i " + pemFile.getPath() + " " + script.getPath() +
                            " " + userName + "@" + floatingIp + ":"+ vmScriptsPath);

                    Process copy = pb.start();
                    int exitStatus = copy.waitFor();
                    if (exitStatus != 0) {
                        log.error("Script " + script.getName() + " could not be sent.");
                        throw new Exception("Script " + script.getName() + " could not be sent.");
                    }
                    //execute script on VM
                    pb = new ProcessBuilder("/bin/bash", "-c", "ssh -o \"StrictHostKeyChecking no\" " + userName + "@" +
                            floatingIp + " -i " + pemFile.getPath() + " \"" + preScript + " source " + vmScriptsPath + "/" + script.getName() + "\"");

                    Process execute = pb.redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                    exitStatus = execute.waitFor();
                    if (exitStatus != 0) {
                        log.error("Script " + script.getName() + " exited with status " + exitStatus + ".");
                        throw new Exception("Script " + script.getName() + " exited with status " + exitStatus + " on " + floatingIp + ".");
                    } else {
                        log.info("Script " + script.getName() + " exited successfully on " + floatingIp + ".");
                    }
                }
            }
        }
        return param;
    }

    private String getContentOfScript(String script) throws IOException {
        byte[] encoded = new byte[0];
        encoded = Files.readAllBytes(Paths.get(script));
        String content = new String(encoded, StandardCharsets.UTF_8);
        return content;
    }

    private boolean findInText(String text, String variable) {
        Matcher matcher = Pattern.compile("\\$[{]" + variable + "[}]").matcher(text);
        return matcher.find();
    }


    private void fillVnfrVnfc(NetworkServiceRecord nsr) {
        Iterator<VirtualNetworkFunctionRecord> vnfrIt = nsr.getVnfr().iterator();
        while (vnfrIt.hasNext()) {
            VirtualNetworkFunctionRecord vnfr = vnfrIt.next();
            List<VNFCRepresentation> representationList = new LinkedList<>();
            Configuration conf = vnfr.getConfigurations();
            Map<String, String> confMap = new HashMap<>();
            for (ConfigurationParameter confPar : conf.getConfigurationParameters()) {
                confMap.put(confPar.getConfKey(), confPar.getValue());
            }
            for (VirtualDeploymentUnit vdu : vnfr.getVdu()) {
                for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()) {
                    VNFCRepresentation vnfcRepresentation = new VNFCRepresentation();
                    vnfcRepresentation.setVnfrName(vnfr.getName());
                    vnfcRepresentation.setHostname(vnfcInstance.getHostname());
                    vnfcRepresentation.setConfiguration(confMap);
                    for (Ip ip : vnfcInstance.getIps()) {
                        vnfcRepresentation.addNetIp(ip.getNetName(), ip.getIp());
                    }
                    for (Ip fIp : vnfcInstance.getFloatingIps()) {
                        vnfcRepresentation.addNetFip(fIp.getNetName(), fIp.getIp());
                    }
                    representationList.add(vnfcRepresentation);
                }
            }
            if (!vnfrVnfc.containsKey(vnfr.getType())) {
                vnfrVnfc.put(vnfr.getType(), representationList);
            } else {
                List<VNFCRepresentation> l = vnfrVnfc.get(vnfr.getType());
                l.addAll(representationList);
            }
        }
    }


    private List<String> getPreScripts(String script) {
        List<String> preScripts = new LinkedList<>();
        Map<String, LinkedList<String>> variableValuesMap = new HashMap<>();
        // handle variable of type ${vnfrtype_configurationkey}
        String configurationDeclarations = "";

        for (String vnfr : vnfrVnfc.keySet()) {


            for (String confKey : vnfrVnfc.get(vnfr).get(0).getAllConfigurationKeys()) {
                configurationDeclarations += vnfr + "_" + confKey + "=" + vnfrVnfc.get(vnfr).get(0).getConfiguration(confKey) + "; ";
            }

            // handle variables of type ${vnfrtype_fip}
            if (findInText(script, vnfr + "_fip")) {
                LinkedList<String> fips = new LinkedList<>();
                for (VNFCRepresentation vnfc : vnfrVnfc.get(vnfr))
                    fips.addAll(vnfc.getAllFips());
                variableValuesMap.put(vnfr + "_fip", fips);
            }
            // handle variables of type ${vnfrtype_ip}
            if (findInText(script, vnfr + "_ip")) {
                LinkedList<String> ips = new LinkedList<>();
                for (VNFCRepresentation vnfc : vnfrVnfc.get(vnfr)) {
                    ips.addAll(vnfc.getAllIps());
                }
                variableValuesMap.put(vnfr + "_ip", ips);
            }
            // store all the network names occurring in that vnfr in a Set
            Set<String> netnames = new HashSet<>();
            for (VNFCRepresentation vnfc : vnfrVnfc.get(vnfr)) {
                for (String net : vnfc.getAllNetNames()) {
                    if (!netnames.contains(net))
                        netnames.add(net);
                }
            }
            for (String net : netnames) {
                // handle variables of type ${vnfrtype_network_fip}
                if (findInText(script, vnfr + "_" + net + "_fip")) {
                    LinkedList<String> fips = new LinkedList<>();
                    for (VNFCRepresentation vnfc : vnfrVnfc.get(vnfr))
                        fips.addAll(vnfc.getFipsByNet(net));
                    variableValuesMap.put(vnfr + "_" + net + "_fip", fips);
                }
                // handle variables of type ${vnfrtype_network_ip}
                if (findInText(script, vnfr + "_" + net + "_ip")) {
                    LinkedList<String> ips = new LinkedList<>();
                    for (VNFCRepresentation vnfc : vnfrVnfc.get(vnfr))
                        ips.addAll(vnfc.getIpsByNet(net));
                    variableValuesMap.put(vnfr + "_" + net + "_ip", ips);
                }
            }

        }

        // create the prescripts
        boolean noMoreElements = false;
        while (!noMoreElements) {
            String prescript = "";
            noMoreElements = true;
            for (String varName : variableValuesMap.keySet()) {
                String value = "";
                if (variableValuesMap.get(varName).size() > 1) {
                    value = variableValuesMap.get(varName).poll();
                    noMoreElements = false;
                } else {
                    value = variableValuesMap.get(varName).peek();
                }
                prescript += varName + "=" + value + "; ";
            }
            prescript += configurationDeclarations;
            preScripts.add(prescript);
        }
        return preScripts;
    }


    public void addScript(String scriptName) {
        File f = new File(EXTERNAL_PATH_NAME_SCRIPTS + scriptName);
        if (!f.exists()) {
            log.debug("The script " + scriptName + " does not exist in " + EXTERNAL_PATH_NAME_SCRIPTS + ".");
            log.debug("Will use " + scriptName + " in " + LOCAL_PATH_NAME_SCRIPTS + ".");
            InputStream is = GenericServiceTester.class.getResourceAsStream(LOCAL_PATH_NAME_SCRIPTS + scriptName);
            File t = null;
            try {
                t = new File("/tmp/" + scriptName);
                OutputStream os = new FileOutputStream(t);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.flush();
                os.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            f = t;
        }
        if (!f.exists())
            log.warn(scriptName + " does not exist.");
        else
            scripts.add(f);
    }

    public void setVnfrType(String vnfrType) {
        this.vnfrType = vnfrType;
    }

    public void setVirtualLink(String virtualLink) {
        this.virtualLink = virtualLink;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public void setVmScriptsPath(String vmScriptsPath) {
        this.vmScriptsPath = vmScriptsPath;
    }

}
