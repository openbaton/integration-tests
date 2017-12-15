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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import org.ini4j.Profile;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;
import org.openbaton.sdk.api.rest.VNFPackageAgent;

/**
 * Created by tbr on 30.11.15.
 *
 * <p>Class used to upload a VNFPackage to the NFVO. Class used to upload a VNFPackage to the NFVO.
 */
public class PackageUpload extends Tester<VNFPackage> {
  private static final String EXTERNAL_PATH_NAME = "/etc/openbaton/integration-tests/vnf-packages/";
  private String packageName = "";

  public PackageUpload(Properties p) throws FileNotFoundException {
    super(p, VNFPackage.class);
  }

  @Override
  protected VNFPackage prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws Exception {
    log.info("Upload VNFPackage " + packageName);
    this.setAbstractRestAgent(requestor.getVNFPackageAgent());

    File f = new File(EXTERNAL_PATH_NAME + packageName);
    if (!f.exists()) {
      log.error("No package: " + f.getName() + " found!");
      throw new Exception("No package: " + f.getName() + " found!");
    }

    VNFPackageAgent packageAgent = requestor.getVNFPackageAgent();
    try {
      packageAgent.create(EXTERNAL_PATH_NAME + packageName);
    } catch (SDKException e) {
      log.error("Could not store VNFPackage " + packageName);
      throw e;
    }

    log.debug("--- Successfully stored VNFPackage " + packageName);
    return param;
  }

  @Override
  public void configureSubTask(Profile.Section currentSection) {
    this.setPackageName(currentSection.get("package-name"));
  }

  public void setPackageName(String name) {
    this.packageName = name;
  }
}
