/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
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
import org.openbaton.sdk.api.rest.VirtualNetworkFunctionDescriptorAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by tbr on 30.11.15.
 */

/**
 * With the help of this class it is possible to upload a NSD based on a previously uploaded
 * VNFPackage. If you want to do this task without the integration test, you would normally have to
 * put the VNFD IDs into the VNFD part of the NSD's json file. But because the integration tests
 * cannot know them in advance, you do not spcify the ID field in the NSD's json, but the type of
 * the VNFR. Then this class will use a VNFD which is uploaded to Openbaton and has the same type.
 */
public class NetworkServiceDescriptorCreateFromPackage extends NetworkServiceDescriptorCreate {
  private static Logger log = LoggerFactory.getLogger(NetworkServiceDescriptor.class);

  public NetworkServiceDescriptorCreateFromPackage(Properties p) throws FileNotFoundException {
    super(p);
  }

  @Override
  protected NetworkServiceDescriptor prepareObject() throws FileNotFoundException {
    NetworkServiceDescriptor nsd = super.prepareObject();
    Set<VirtualNetworkFunctionDescriptor> vnfds = nsd.getVnfd();
    for (VirtualNetworkFunctionDescriptor vnfd : vnfds) {
      vnfd.setId(getVnfdIdByType(vnfd.getType()));
      vnfd.setType(null);
    }
    return nsd;
  }

  private String getVnfdIdByType(String type) throws FileNotFoundException {
    VirtualNetworkFunctionDescriptorAgent vnfdAgent =
        requestor.getVirtualNetworkFunctionDescriptorAgent();
    List<VirtualNetworkFunctionDescriptor> obtained = null;
    try {
      obtained = vnfdAgent.findAll();
    } catch (SDKException e) {
      log.error("Error trying to get all VNFDs.");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    String id = "";
    for (VirtualNetworkFunctionDescriptor vnfd : obtained) {
      if (vnfd.getType().equals(type)) {
        id = vnfd.getId();
        return id;
      }
    }
    if (id.equals(""))
      log.warn(
          "Did not find a VNFD of type "
              + type
              + ". Hence the NSD that you want to create from package will contain a VNFD with an empty ID and probably cause problems.");
    return id;
  }
}
