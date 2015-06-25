package org.project.neutrino.integration.test;

import org.json.JSONObject;
import org.project.neutrino.integration.test.exceptions.IntegrationTestException;
import org.project.neutrino.integration.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class NetworkServiceRecordTest {

	private static Logger log = LoggerFactory.getLogger(NetworkServiceRecordTest.class);
	private static String path = "ns-records";

	public static String create(String nfvoIp, String nfvoPort, String id) throws URISyntaxException {

		JSONObject jsonObject;

		String url = "http://" + nfvoIp + ":" + nfvoPort+ "/api/v1/" + path + "/" + id;
		log.info("Sending request create NetworkServiceRecord on url: " + url);

		try {
			jsonObject = Utils.executePostCall(nfvoIp, nfvoPort, path + "/" + id);
			log.debug("received: " + jsonObject.toString());

		} catch (IntegrationTestException e) {
			e.printStackTrace();
			return null;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}

		/**
		 * TODO do the checks
		 */

		return jsonObject.getString("id");
	}

	public static boolean delete(String nfvoIp, String nfvoPort, String id){
		String url = "http://" + nfvoIp + ":" + nfvoPort+ "/api/v1/" + path + "/" + id;
		log.info("Sending request delete NetworkServiceRecord on url: " + url);
		try {
			Utils.executeDeleteCall(nfvoIp, nfvoPort, path + "/" + id);
			log.debug("delete executed");
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static String create(String nsd_id) throws IOException, URISyntaxException {
		Properties properties = Utils.getProperties();
		return NetworkServiceRecordTest.create(properties.getProperty("nfvo-ip"), properties.getProperty("nfvo-port"), nsd_id);
	}

	public static void delete(String nsr_id) throws IOException {
		Properties properties = Utils.getProperties();
		NetworkServiceRecordTest.delete(properties.getProperty("nfvo-ip"), properties.getProperty("nfvo-port"), nsr_id);
	}
}
