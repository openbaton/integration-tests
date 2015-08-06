package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.sdk.api.exception.SDKException;

import java.util.Properties;

/**
 * Created by mob on 29.07.15.
 */
public class NetworkServiceDescriptorDelete extends Tester<NetworkServiceDescriptor> {
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

    public NetworkServiceDescriptorDelete(Properties p) {
        super(p, NetworkServiceDescriptor.class,"","/ns-descriptors");
    }

    @Override
    protected NetworkServiceDescriptor prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws SDKException {
        NetworkServiceDescriptor nsd = (NetworkServiceDescriptor) param;
        try {
            delete(nsd.getId());
        } catch (SDKException sdkEx) {
            log.error("Exception during deleting of NetworkServiceDescription with id: "+nsd.getId(), sdkEx);
            throw sdkEx;
        }
        log.debug(" --- NetworkServiceDescriptorDelete has deleted the nsd:"+nsd.getId());
        return null;
    }
}
