package org.openbaton.integration.test.testers;

import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.integration.test.utils.Tester;
import org.openbaton.sdk.api.exception.SDKException;

import java.util.Properties;
import java.util.Set;

/**
 * Created by tbr on 31.05.16.
 *
 * This Tester expects a NSD object from it's preceding task and tries to delete the VNFDs of this
 * NSD, filtered by vnfd name and type.
 */
public class VirtualNetworkFunctionDescriptorDelete
    extends Tester<VirtualNetworkFunctionDescriptor> {

  private String vnfdType = "";
  private String vnfdName = "";

  public VirtualNetworkFunctionDescriptorDelete(Properties p) {
    super(p, VirtualNetworkFunctionDescriptor.class, "", "/vnf-descriptors");
  }

  @Override
  protected VirtualNetworkFunctionDescriptor prepareObject() {
    return null;
  }

  @Override
  protected Object doWork() throws Exception {

    NetworkServiceDescriptor nsd = (NetworkServiceDescriptor) param;
    Set<VirtualNetworkFunctionDescriptor> vnfds = nsd.getVnfd();

    for (VirtualNetworkFunctionDescriptor vnfd : vnfds) {
      if (!"".equals(vnfdType)) {
        if (!vnfd.getType().equals(vnfdType)) continue;
      }

      if (!"".equals(vnfdName)) {
        if (!vnfd.getName().equals(vnfdName)) continue;
      }

      log.info(
          "Delete VNFD of type "
              + vnfdType
              + " with name "
              + vnfd.getName()
              + " and id "
              + vnfd.getId());
      deleteVnfd(vnfd.getId());
    }

    return null;
  }

  private void deleteVnfd(String id) throws Exception {
    try {
      delete(id);
    } catch (SDKException sdkEx) {
      log.error(
          "Exception during deletion of VirtualNetworkFunctionDescriptor with id: " + id, sdkEx);
      throw sdkEx;
    }
    log.debug(" --- VirtualNetworkFunctionDescriptorDelete has deleted the VNFD with id: " + id);
  }

  public void setVnfdType(String vnfdType) {
    this.vnfdType = vnfdType;
  }

  public void setVnfdName(String vnfdName) {
    this.vnfdName = vnfdName;
  }
}
