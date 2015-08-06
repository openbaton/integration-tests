package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.integration.test.parser.Parser;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.integration.test.utils.Utils;
import org.project.openbaton.sdk.api.exception.SDKException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class VimInstanceCreate extends Tester<VimInstance> {

    private static final String LOCAL_PATH_NAME = "/etc/json_file/vim_instances/";
    private static final String EXTERNAL_PATH_NAME = "/etc/openbaton/json_files/vim_instances/";
    private static String fileName;

    /**
     * @param properties : IntegrationTest properties containing:
     *                   nfvo-usr
     *                   nfvo-pwd
     *                   nfvo-ip
     *                   nfvo-port
     */
    public VimInstanceCreate(Properties properties) {
        super(properties, VimInstance.class, LOCAL_PATH_NAME, "/datacenters");
    }


    @Override
    protected Object doWork() throws SDKException {
        Object result;
        try {
            result= create();
        } catch (SDKException sdkEx) {
            log.error("Exception during the instantiation of VimInstance", sdkEx);
            throw sdkEx;
        }
        return result;
    }
    @Override
    protected VimInstance prepareObject() {
        String body=null;
        File f = new File(EXTERNAL_PATH_NAME+fileName);
        if (f != null && f.exists()) {
            try {
                body = Utils.getStringFromInputStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else{
            log.info("No file: "+f.getName()+" found, we will use "+LOCAL_PATH_NAME+fileName);
            body = Utils.getStringFromInputStream(Tester.class.getResourceAsStream(LOCAL_PATH_NAME+fileName));
        }
        String vimRandom = Parser.randomize(body,"/etc/json_file/parser_configuration_properties/vim.properties");
        //log.debug("vim-instance.json (old): " + body);
        //log.debug("vim-instance.json (random): " + vimRandom);

        return mapper.fromJson(vimRandom, aClass);
    }
    public void setFileName(String fileName){
        this.fileName=fileName;
    }
}




