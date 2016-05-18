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

import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.exceptions.SubscriptionException;
import org.openbaton.integration.test.interfaces.Waiter;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.integration.test.utils.Utils;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.NetworkServiceRecordRestAgent;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by tbr on 11.01.16.
 */

/**
 * This class triggers one scale out on a specified VNFR.
 */
public class ScaleOut extends Tester {

    private String vnfrType = "";
    private String virtualLink = "";
    private String floatingIp = "random";

    public ScaleOut(Properties properties) {
        super(properties, ScaleOut.class, "", "");
    }


    @Override
    protected Serializable prepareObject() {
        return null;
    }

    @Override
    protected Object doWork() throws Exception {
        log.info("Start ScaleOut on VNFR type "+vnfrType);
        NetworkServiceRecord nsr = (NetworkServiceRecord) getParam();

        Properties p = Utils.getProperties();
        NetworkServiceRecordRestAgent agent = new NetworkServiceRecordRestAgent(p.getProperty("nfvo-usr"),
                p.getProperty("nfvo-pwd"),
                p.getProperty("nfvo-ip"),
                p.getProperty("nfvo-port"),
                "/ns-records",
                "1");

        boolean found = false;
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
            if (vnfr.getType().equals(vnfrType)) {
                found = true;

                VNFComponent vnfc = createVNFComponent();
                try {
                    agent.createVNFCInstance(nsr.getId(), vnfr.getId(), vnfc);
                } catch (SDKException e) {
                    log.warn("Exception while triggering the scale out: "+e.getMessage());
                }
                log.debug("Triggered ScaleOut on VNFR type "+vnfrType);
            }
        }
        if (!found)
            log.warn("did not find a VNFR of type " + vnfrType);

        return nsr;
    }

    private VNFComponent createVNFComponent() {
        VNFComponent vnfc = new VNFComponent();
        Set<VNFDConnectionPoint> vnfdConnectionPointSet = new HashSet<>();
        VNFDConnectionPoint vnfdConnectionPoint = new VNFDConnectionPoint();
        if (virtualLink.equals(""))
            log.warn("Virtual link is empty");
        vnfdConnectionPoint.setVirtual_link_reference(virtualLink);
        vnfdConnectionPoint.setFloatingIp(floatingIp);
        vnfdConnectionPointSet.add(vnfdConnectionPoint);
        vnfc.setConnection_point(vnfdConnectionPointSet);
        return vnfc;
    }

    public void setVnfrType(String vnfrType) {
        this.vnfrType = vnfrType;
    }

    public void setVirtualLink(String virtualLink) {
        this.virtualLink = virtualLink;
    }

    public void setFloatingIp(String ip) {
        this.floatingIp = ip;
    }

}

