package org.openbaton.integration.test.testers;

import org.openbaton.integration.test.parser.Parser;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.catalogue.nfvo.VimInstance;
import org.openbaton.sdk.api.exception.SDKException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by lto on 24/06/15.
 */
public class VimInstanceCreate extends Tester<VimInstance> {

    private static final String LOCAL_PATH_NAME = "/etc/json_file/vim_instances/";
    private static final String EXTERNAL_PATH_NAME = "/etc/openbaton/integration-test/vim-instances/";
    private static final String EXTERNAL_PATH_NAME_PARSER_VIM = "/etc/openbaton/integration-test/parser-properties/vim.properties";
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
            log.error("Exception during the instantiation of VimInstance");
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
            log.warn("No file: "+f.getName()+" found, we will use "+LOCAL_PATH_NAME+fileName);
            body = Utils.getStringFromInputStream(Tester.class.getResourceAsStream(LOCAL_PATH_NAME+fileName));
        }
        String vimRandom = null;
        File parserPropertiesFile = new File(EXTERNAL_PATH_NAME_PARSER_VIM);
        if (parserPropertiesFile != null && parserPropertiesFile.exists()) {
            try {
                vimRandom = Parser.randomize(body, EXTERNAL_PATH_NAME_PARSER_VIM);
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.debug("vim-instance.json (old): " + body);
            log.debug("vim-instance.json (random): " + vimRandom);
            return mapper.fromJson(vimRandom, aClass);
        }else {
            log.warn("Missing /etc/openbaton/integration-test-parser-properties/vim.properties file");
            log.warn("If you want to use the parser for the VIM, create the file vim.properties in the path /etc/openbaton/integration-test-parser-properties/");
        }
        return mapper.fromJson(body, aClass);
    }
    public void setFileName(String fileName){
        this.fileName=fileName;
    }
}




