package org.project.openbaton.integration.test;

import org.project.openbaton.common.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.integration.test.utils.Utils;
import org.project.openbaton.sdk.NFVORequestor;
import org.project.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class NetworkServiceDescriptorTest {
    private static final String FILE_NAME = "/etc/json_file/network_service_descriptors/NetworkServiceDescriptor-with-dependencies-without-allacation.json";
    private static Logger log = LoggerFactory.getLogger(VimInstanceTest.class);
    private static NFVORequestor requestor = new NFVORequestor("1");
    private static Gson mapper;  

    public static String create(String nfvoIp, String nfvoPort) throws URISyntaxException {
        String body = Utils.getStringFromInputStream(NetworkServiceDescriptorTest.class.getResourceAsStream(FILE_NAME));
        GsonBuilder builder = new GsonBuilder(); 
        mapper = builder.create();

		NetworkServiceDescriptor networkServiceDescriptor = mapper.fromJson(body, NetworkServiceDescriptor.class);
		
		NetworkServiceDescriptor obtained = null;
		try {
			obtained = requestor.getNetworkServiceDescriptorAgent().create(networkServiceDescriptor);
		} catch (SDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
 		log.trace("Received: " + obtained.toString());
 		
 		boolean resultEvaluate = Utils.evaluateObjects(networkServiceDescriptor,obtained);
 				
		if(resultEvaluate == false)
			log.debug("NSD RESPONSE - FALSE");
		else
			log.debug("NSD RESPONSE - TRUE");

        /**
         * TODO assert everything is created!
         */
        
        return obtained.getId();
    }

    public static String create() throws IOException, URISyntaxException {
        Properties properties = Utils.getProperties();
        return NetworkServiceDescriptorTest.create(properties.getProperty("nfvo-ip"), properties.getProperty("nfvo-port"));
    }
}
