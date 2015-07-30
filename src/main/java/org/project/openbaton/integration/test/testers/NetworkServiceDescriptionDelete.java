package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.integration.test.utils.Tester;

import java.util.Properties;

/**
 * Created by mob on 29.07.15.
 */
public class NetworkServiceDescriptionDelete extends Tester<NetworkServiceDescriptor> {
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
    public NetworkServiceDescriptionDelete(Properties p) {
        super(p, NetworkServiceDescriptor.class,"","/ns-descriptors");
    }

    @Override
    protected NetworkServiceDescriptor prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        String nsdId = (String) param;
        log.debug("--Deleting NSD: "+nsdId);
        delete(nsdId);
        return null;
    }

    @Override
    protected void handleException(Exception e) {

    }
}
