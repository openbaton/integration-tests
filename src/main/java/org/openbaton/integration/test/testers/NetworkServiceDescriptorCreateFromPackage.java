/*
 * Copyright (c) 2015 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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