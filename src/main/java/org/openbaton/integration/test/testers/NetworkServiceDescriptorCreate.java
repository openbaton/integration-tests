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

import org.openbaton.integration.test.parser.Parser;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class NetworkServiceDescriptorCreate extends Tester<NetworkServiceDescriptor>{
    private static final String LOCAL_PATH_NAME_NSD = "/etc/json_file/network_service_descriptors/";
    private static final String EXTERNAL_PATH_NAME_NSD = "/etc/openbaton/integration-test/network-service-descriptors/";
    private static final String EXTERNAL_PATH_NAME_PARSER_NSD = "/etc/openbaton/integration-test/parser-properties/nsd.properties";
    private static Logger log = LoggerFactory.getLogger(NetworkServiceDescriptor.class);
    private static String fileName;

    public NetworkServiceDescriptorCreate(Properties p){
        super(p, NetworkServiceDescriptor.class, LOCAL_PATH_NAME_NSD,"/ns-descriptors");
    }

    @Override
    protected Object doWork() throws SDKException {
        log.info("Upload NSD "+fileName);
        Object received = null;
        VimInstance vimInstance= (VimInstance) param;
        try {
            received = create();
        }catch(SDKException sdkEx){
            log.error("Exception during the on-boarding of NetworkServiceDescriptor from vim with name: "+vimInstance.getName()+" and id: "+vimInstance.getId());
            throw sdkEx;
        }
        NetworkServiceDescriptor nsd = (NetworkServiceDescriptor) received;
        log.debug("Stored NSD with id: " + nsd.getId());
        return received;
    }

    @Override
    protected NetworkServiceDescriptor prepareObject() {
            String body = null;
            File f = new File(EXTERNAL_PATH_NAME_NSD + fileName);
            if (f != null && f.exists()) {
                try {
                    body = Utils.getStringFromInputStream(new FileInputStream(f));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                log.debug("No file: " + f.getName() + " found, we will use " + LOCAL_PATH_NAME_NSD + fileName);
                body = Utils.getStringFromInputStream(Tester.class.getResourceAsStream(LOCAL_PATH_NAME_NSD + fileName));
            }
            String nsdRandom = null;
            File parserPropertiesFile = new File(EXTERNAL_PATH_NAME_PARSER_NSD);
            if (parserPropertiesFile != null && parserPropertiesFile.exists()) {
                try {
                    nsdRandom = Parser.randomize(body, EXTERNAL_PATH_NAME_PARSER_NSD);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                log.debug("NetworkServiceDescriptor (old): " + body.trim());
                log.debug("NetworkServiceDescriptor (random): " + nsdRandom);
                return mapper.fromJson(nsdRandom, aClass);
            }
            else {
                log.debug("Missing /etc/openbaton/integration-test-parser-properties/nsd.properties file");
                log.debug("If you want to use the parser for the NSD, create the file nsd.properties in the path /etc/openbaton/integration-test-parser-properties/");
            }
            return mapper.fromJson(body, aClass);
    }
    public void setFileName(String fileName){
        this.fileName=fileName;
    }
}
