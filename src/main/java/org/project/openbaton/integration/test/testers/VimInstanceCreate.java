package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.integration.test.parser.Parser;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.integration.test.utils.Utils;
import org.project.openbaton.sdk.api.exception.SDKException;

import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class VimInstanceCreate extends Tester<VimInstance> {

    private final static String FILE_NAME = "/etc/json_file/vim_instances/vim-instance.json";
    /**
     * @param properties : IntegrationTest properties containing:
     *                   nfvo-usr
     *                   nfvo-pwd
     *                   nfvo-ip
     *                   nfvo-port
     */
    public VimInstanceCreate(Properties properties) {
        super(properties, VimInstance.class, FILE_NAME, "/datacenters");
    }


    @Override
    protected Object doWork() throws Exception {
        return create();
    }

    @Override
    protected void handleException(Exception e) {
        e.printStackTrace();
        log.error("there was an exception: " + e.getMessage());
    }

    @Override
    protected VimInstance prepareObject() {
        String body = Utils.getStringFromInputStream(Tester.class.getResourceAsStream(FILE_NAME));

        String vimRandom = new Parser("/etc/json_file/parser_configuration_properties/vim.properties").randomize(body);
        log.debug("vim-instance.json (old): " + body);
        log.debug("vim-instance.json (random): " + vimRandom);

        return mapper.fromJson(vimRandom, aClass);
    }
}




