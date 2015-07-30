package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.integration.test.parser.Parser;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.integration.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class NetworkServiceDescriptorCreate extends Tester<NetworkServiceDescriptor>{
    private static final String FILE_NAME = "/etc/json_file/network_service_descriptors/NetworkServiceDescriptor.json";
    private static Logger log = LoggerFactory.getLogger(NetworkServiceDescriptorCreate.class);

    public NetworkServiceDescriptorCreate(Properties p){
        super(p, NetworkServiceDescriptor.class,FILE_NAME,"/ns-descriptors");
    }

    @Override
    protected Object doWork() throws Exception {
        return create();
    }

    @Override
    protected void handleException(Exception e) {
        e.printStackTrace();
        log.error("Exception NetworkServiceDescriptionCreate: there was an exception: " + e.getMessage());
    }

    @Override
    protected NetworkServiceDescriptor prepareObject() {
        {
            String body = Utils.getStringFromInputStream(Tester.class.getResourceAsStream(FILE_NAME));

            String nsdRandom = new Parser("/etc/json_file/parser_configuration_properties/nsd.properties").randomize(body);
            log.debug("NetworkServiceDescriptor (old): " + body);
            log.debug("NetworkServiceDescriptor (random): " + nsdRandom);

            return mapper.fromJson(nsdRandom, aClass);
        }
    }
}
