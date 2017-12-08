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

import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.VNFPackageAgent;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

/**
 * Created by tbr on 01.12.15.
 *
 * Class used to delete a VNFPackage.
 */
public class PackageDelete extends Tester<VirtualNetworkFunctionDescriptor> {
  private String packageName = "";

  public PackageDelete(Properties properties) throws FileNotFoundException {
    super(properties, VirtualNetworkFunctionDescriptor.class);
    this.setAbstractRestAgent(requestor.getVNFPackageAgent());
  }

  @Override
  protected VirtualNetworkFunctionDescriptor prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws Exception {
    log.info("Delete VNFPackage " + packageName);
    VNFPackageAgent agent = requestor.getVNFPackageAgent();

    try {
      List<VNFPackage> packages = agent.findAll();
      for (VNFPackage p : packages) {
        if (p.getName().equals(packageName)) agent.delete(p.getId());
      }
    } catch (SDKException e) {
      log.error("Error while deleting the VNFPackage " + packageName);
      throw e;
    }
    log.debug("--- Successfully deleted the package " + packageName);
    return param;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
}
