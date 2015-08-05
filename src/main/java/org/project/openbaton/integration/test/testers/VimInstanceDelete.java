package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.integration.test.utils.Tester;

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
    protected Object doWork() throws Exception {
        VimInstance vi = (VimInstance) param;
        delete(vi.getId());
        log.debug(" --- VimInstanceDelete has deleted the vimInstance:"+vi.getId());
        return null;
    }

    @Override
    protected void handleException(Exception e) {

    }
}
