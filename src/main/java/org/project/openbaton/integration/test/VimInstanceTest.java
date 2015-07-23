package org.project.openbaton.integration.test;

import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.sdk.api.exception.SDKException;

import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class VimInstanceTest extends Tester<VimInstance> {


    /**
     * @param properties : IntegrationTest properties containing:
     *                   nfvo-usr
     *                   nfvo-pwd
     *                   nfvo-ip
     *                   nfvo-port
     */
    public VimInstanceTest(Properties properties) {
        super(properties, VimInstance.class, "/etc/json_file/vim_instances/vim-instance.json", "/datacenters");
    }


    @Override
    public VimInstance create() throws SDKException {
        VimInstance obtained = super.create();

        /**
         * TODO check whenever obtained is right
         */

        return obtained;
    }
}




