package org.project.neutrino.integration.test;

import org.json.JSONObject;
import org.project.neutrino.integration.test.exceptions.IntegrationTestException;
import org.project.neutrino.integration.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class VimInstanceTest {

    private static final String FILE_NAME = "/etc/json_file/vim_instances/vim-instance.json";
    private static Logger log = LoggerFactory.getLogger(VimInstanceTest.class);
        private static String path = "datacenters";
    /**
     *
     * @param nfvoIp
     * @param nfvoPort
     * @return
     * @throws URISyntaxException
     */
        
        
        
    public static boolean create(String nfvoIp, String nfvoPort) throws URISyntaxException {
        String body = Utils.getStringFromInputStream(VimInstanceTest.class.getResourceAsStream(FILE_NAME));
        JSONObject obtained;
        JSONObject expected = new JSONObject(body);
        String url = "http://" + nfvoIp + ":" + nfvoPort+ "/api/v1/" + path;
        log.info("Sending request create vim on url: " + url);
        try {
            obtained = Utils.executePostCall(nfvoIp, nfvoPort, body, path);
            //log.debug("received: " + obtained.toString());
            
            Utils.evaluateJSONObject(expected, obtained);
            
            

        } catch (IntegrationTestException e) {
            e.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        
        
       
        

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
