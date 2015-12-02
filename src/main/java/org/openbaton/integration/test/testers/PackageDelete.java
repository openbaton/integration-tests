package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.util.AbstractRestAgent;

import java.util.List;
import java.util.Properties;

/**
 * Created by tbr on 01.12.15.
 */
public class PackageDelete extends Tester<VirtualNetworkFunctionDescriptor> {
    private String packageName = "";


    public PackageDelete(Properties properties) {
        super(properties, VirtualNetworkFunctionDescriptor.class, "", "/vnf-descriptors");
    }

    @Override
    protected VirtualNetworkFunctionDescriptor prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        for (VirtualNetworkFunctionDescriptor vnfd : getVnfds()) {
            if (vnfd.getVnfPackage().getName().equals(packageName)) {
                try {
                    delete(vnfd.getId());
                } catch (Exception e) {
                    log.error("Error while deleting vnfd with id "+vnfd.getId()+" from VNFPackage " + packageName+".");
                    throw e;
                }
            }
        }
        log.info("Successfully deleted the package " + packageName);
        return null;
    }


    private List<VirtualNetworkFunctionDescriptor> getVnfds() throws Exception {
        AbstractRestAgent abstractRestAgent = requestor.abstractRestAgent(VirtualNetworkFunctionDescriptor.class, "/vnf-descriptors");
        List<VirtualNetworkFunctionDescriptor> obtained = null;
        try {
            obtained = abstractRestAgent.findAll();
        } catch (SDKException e) {
            log.error("Error trying to get all VNFDs.");
            throw e;
        }
        return obtained;
    }

    public void setPackageName(String packageName) {
        this.packageName=packageName;
    }
}
