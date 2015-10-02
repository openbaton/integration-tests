package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;

import java.util.Properties;

/**
 * Created by mob on 04.08.15.
 */
public class VimInstanceDelete extends Tester<VimInstance> {

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
    public VimInstanceDelete(Properties properties) {
        super(properties, VimInstance.class, "", "");
    }

    @Override
    protected VimInstance prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws SDKException {
        VimInstance vi = (VimInstance) param;
        try {
            delete(vi.getId());
        } catch (SDKException sdkEx) {
            log.error("Exception during deleting of VimInstance with id: "+vi.getId(), sdkEx);
            throw sdkEx;
        }
        //log.debug(" --- VimInstanceDelete has deleted the vimInstance:"+vi.getId());
        return null;
    }
}
