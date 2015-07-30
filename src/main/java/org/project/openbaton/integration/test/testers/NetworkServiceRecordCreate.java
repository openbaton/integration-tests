package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class NetworkServiceRecordCreate extends Tester<NetworkServiceRecord> {

	private static final String FILE_NAME = "/etc/json_file/network_service_descriptors/NetworkServiceDescriptor.json";
	private static Logger log = LoggerFactory.getLogger(NetworkServiceRecordCreate.class);
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
	public NetworkServiceRecordCreate(Properties properties) {
		super(properties, NetworkServiceRecord.class, FILE_NAME, "/ns-records");
	}

	@Override
	protected Object doWork() throws Exception {
		return create();
	}

	@Override
	protected void handleException(Exception e) {
		e.printStackTrace();
		log.error("Exception NetworkServiceRecordCreate: there was an exception: " + e.getMessage());
	}

	@Override
	public NetworkServiceRecord create() throws SDKException {

		NetworkServiceDescriptor nsd = (NetworkServiceDescriptor) this.param;

		log.debug("Obtained param: " + param);
		log.debug("Obtained id: " + nsd.getId());

		NetworkServiceRecord networkServiceRecord = this.requestor.getNetworkServiceRecordAgent().create(nsd.getId());
		log.debug("Created: " + networkServiceRecord);
		return networkServiceRecord;

	}

	@Override
	protected NetworkServiceRecord prepareObject() {
		return null;
	}
}
