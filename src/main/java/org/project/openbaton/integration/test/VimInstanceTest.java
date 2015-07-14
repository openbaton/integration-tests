package org.project.openbaton.integration.test;

import org.project.openbaton.common.catalogue.nfvo.VimInstance;
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
public class VimInstanceTest {

    private static final String FILE_NAME = "/etc/json_file/vim_instances/vim-instance.json";
    private static Logger log = LoggerFactory.getLogger(VimInstanceTest.class);
    private static NFVORequestor requestor = new NFVORequestor("1");
    private static Gson mapper;    
    /**
     *
     * @param nfvoIp
     * @param nfvoPort
     * @return
     * @throws URISyntaxException
     */
        
        
        
    public static boolean create(String nfvoIp, String nfvoPort) throws URISyntaxException {
    	
       String body = Utils.getStringFromInputStream(VimInstanceTest.class.getResourceAsStream(FILE_NAME)).trim();        
       GsonBuilder builder = new GsonBuilder(); 
       mapper = builder.create();
                
       VimInstance vimInstance = mapper.fromJson(body, VimInstance.class);
		
	   VimInstance obtained;
		 try {
			obtained = requestor.getVimInstanceAgent().create(vimInstance);
		} catch (SDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		log.trace("Received: " + obtained.toString());
		
		boolean resultEvaluate = Utils.evaluateObjects(vimInstance,obtained);
	
		if(resultEvaluate == false)
			log.debug("VIM RESPONSE - FALSE");
		else
			log.debug("VIM RESPONSE - TRUE");
        

        /**
         * TODO assert everything is created!
         */

        return true;
    }

    


	public static boolean create() throws IOException, URISyntaxException {
        Properties properties = Utils.getProperties();
        return VimInstanceTest.create(properties.getProperty("nfvo-ip"), properties.getProperty("nfvo-port"));
    }
	
	

}




