package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.util.AbstractRestAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by tbr on 30.11.15.
 */
public class NetworkServiceDescriptorCreateFromPackage extends NetworkServiceDescriptorCreate {
    private static Logger log = LoggerFactory.getLogger(NetworkServiceDescriptor.class);

    public NetworkServiceDescriptorCreateFromPackage(Properties p) {
        super(p);
    }

    @Override
    protected NetworkServiceDescriptor prepareObject() {

        NetworkServiceDescriptor nsd = super.prepareObject();
        Set<VirtualNetworkFunctionDescriptor> vnfds = nsd.getVnfd();
        Iterator<String> idIt = getVnfdIds().iterator();
        for (VirtualNetworkFunctionDescriptor vnfd : vnfds) {
            if (idIt.hasNext()) {
                if (vnfd.getId()==null || vnfd.getId().equals(""))
                    vnfd.setId(idIt.next());
            }
            else {
                log.error("Not enough VNFDs exist for the NSD.");
            }
        }
        return nsd;
    }

    private List<String> getVnfdIds() {
        AbstractRestAgent abstractRestAgent = requestor.abstractRestAgent(VirtualNetworkFunctionDescriptor.class, "/vnf-descriptors");
        List<VirtualNetworkFunctionDescriptor> obtained = null;
        try {
            obtained = abstractRestAgent.findAll();
        } catch (SDKException e) {
            log.error("Error trying to get all VNFDs.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        List<String> ids = new LinkedList<>();
        for (VirtualNetworkFunctionDescriptor vnfd : obtained) {
            ids.add(vnfd.getId());
        }

        return ids;
    }
}