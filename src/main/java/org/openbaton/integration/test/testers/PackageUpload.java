package org.openbaton.integration.test.testers;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.api.exception.SDKException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Created by tbr on 30.11.15.
 */
public class PackageUpload extends Tester<VNFPackage> {
    private static final String EXTERNAL_PATH_NAME = "/etc/openbaton/integration-test/vnf-packages/";
    private String packageName = "";
    private String nfvoUrl = "";


    public PackageUpload(Properties p){
        super(p, VNFPackage.class, EXTERNAL_PATH_NAME, "/vnf-packages");
        nfvoUrl = "http://"+p.getProperty("nfvo-ip") + ":" + p.getProperty("nfvo-port") + "/api/v1/vnf-packages/";
    }

    @Override
    protected VNFPackage prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {

        File f = new File(EXTERNAL_PATH_NAME+packageName);
        if (f == null || !f.exists()) {
            log.error("No package: "+f.getName()+" found!");
            throw new Exception("No package: "+f.getName()+" found!");
        }

        try {
            Unirest.post(nfvoUrl)
                    .header("accept", "application/json")
                    .field("file", f)
                    .asJson();
        } catch (UnirestException e) {
            log.error("Could not store VNFPackage "+packageName);
            throw e;
        }
        log.info("Successfully stored VNFPackage "+packageName);
        return param;
    }

    public void setPackageName(String name) {
        this.packageName=name;
    }
}
