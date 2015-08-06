package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.integration.test.parser.Parser;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.integration.test.utils.Utils;
import org.project.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class NetworkServiceDescriptorCreate extends Tester<NetworkServiceDescriptor>{
    private static final String LOCAL_PATH_NAME = "/etc/json_file/network_service_descriptors/";
    private static final String EXTERNAL_PATH_NAME = "/etc/openbaton/json_files/network_service_descriptors/";
    private static Logger log = LoggerFactory.getLogger(NetworkServiceDescriptor.class);
    private static String fileName;

    public NetworkServiceDescriptorCreate(Properties p){
        super(p, NetworkServiceDescriptor.class, LOCAL_PATH_NAME,"/ns-descriptors");
    }

    @Override
    protected Object doWork() throws SDKException {

        Object received = null;
        VimInstance vimInstance= (VimInstance) param;
        try {
            received = create();
        }catch(SDKException sdkEx){
            log.error("Exception during the instantiation of NetworkServiceDescription from vim of id: "+vimInstance.getId(),sdkEx);
            throw sdkEx;
        }
        NetworkServiceDescriptor nsd = (NetworkServiceDescriptor) received;
        //log.debug(" --- Creating nsd with id: " + nsd.getId());
        return received;
    }

    @Override
    protected NetworkServiceDescriptor prepareObject() {
        {
            String body=null;
            File f = new File(EXTERNAL_PATH_NAME+fileName);
            if (f != null && f.exists()) {
                try {
                    body = Utils.getStringFromInputStream(new FileInputStream(f));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else{
                log.info("No file: "+f.getName()+" found, we will use "+LOCAL_PATH_NAME+fileName);
                body = Utils.getStringFromInputStream(Tester.class.getResourceAsStream(LOCAL_PATH_NAME+fileName));
            }
            String nsdRandom = Parser.randomize(body,"/etc/json_file/parser_configuration_properties/nsd.properties");
            //log.debug("NetworkServiceDescriptor (old): " + body);
            //log.debug("NetworkServiceDescriptor (random): " + nsdRandom);

            return mapper.fromJson(nsdRandom, aClass);
        }
    }
    public void setFileName(String fileName){
        this.fileName=fileName;
    }
}
