package org.project.openbaton.integration.test;

import org.json.JSONObject;
import org.project.openbaton.integration.test.exceptions.IntegrationTestException;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.integration.test.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;

public class NetworkServiceRecordTest extends Tester<NetworkServiceRecord> {

	private static Logger log = LoggerFactory.getLogger(NetworkServiceRecordTest.class);
	private static String path = "ns-records";

	/**
	 * @param properties : IntegrationTest properties containing:
	 *                   nfvo-usr
	 *                   nfvo-pwd
	 *                   nfvo-ip
	 *                   nfvo-port
	 * @param aClass     : example VimInstance.class
	 * @param filePath   : example "/etc/json_file/vim_instances/vim-instance.json"
	 * @param basePath
	 */
	public NetworkServiceRecordTest(Properties properties, String filePath, String basePath) {
		super(properties, NetworkServiceRecord.class, filePath, basePath);
	}
}
