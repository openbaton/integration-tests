package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.common.VNFDependency;
import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.project.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.project.openbaton.catalogue.nfvo.VimInstance;
import org.project.openbaton.integration.test.utils.Tester;
import org.project.openbaton.integration.test.utils.Utils;
import org.project.openbaton.sdk.NFVORequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * Created by lto on 24/06/15.
 */
public class NetworkServiceDescriptorTest extends Tester<NetworkServiceDescriptor>{
    private static final String FILE_NAME = "/etc/json_file/network_service_descriptors/NetworkServiceDescriptor-with-dependencies-without-allacation.json";
    private static Logger log = LoggerFactory.getLogger(NetworkServiceDescriptorTest.class);

    public NetworkServiceDescriptorTest(Properties p){
        super(p, NetworkServiceDescriptor.class,FILE_NAME,"/ns-descriptors");
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
    protected NetworkServiceDescriptor prepareObject() {
        {
            String body = Utils.getStringFromInputStream(Tester.class.getResourceAsStream(FILE_NAME));

            log.debug("Casting " + body.trim() + " into " + aClass.getName());

            NetworkServiceDescriptor networkServiceDescriptor = mapper.fromJson(body, aClass);

            networkServiceDescriptor.setVnf_dependency(new HashSet<VNFDependency>());
            //TODO jason parser che prende in ingresso un NSD e ne restituisce uno random
            for (VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor : networkServiceDescriptor.getVnfd()){
                double random=Math.random();
                virtualNetworkFunctionDescriptor.setName(virtualNetworkFunctionDescriptor.getName() + "-" + random);
                log.debug("NSD -> VNF name:" + virtualNetworkFunctionDescriptor.getName());
                for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionDescriptor.getVdu()){
                    virtualDeploymentUnit.getVimInstance().setName(((VimInstance)this.param).getName());
                    log.debug("NSD -> VNF -> VimInstanceName:" + virtualDeploymentUnit.getVimInstance().getName());
                }

            }
            return networkServiceDescriptor;
        }
    }
}
