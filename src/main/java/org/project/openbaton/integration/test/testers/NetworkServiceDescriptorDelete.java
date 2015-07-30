package org.project.openbaton.integration.test.testers;

import org.project.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.project.openbaton.integration.test.utils.Tester;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by mob on 29.07.15.
 */
public class NetworkServiceDescriptorDelete extends Tester<NetworkServiceDescriptor> {
    /**
     * @param properties : IntegrationTest properties containing:
     *                   nfvo-usr
     *                   nfvo-pwd
     *                   nfvo-ip
     *                   nfvo-port
     * @param aClass     : example VimInstance.class
     * @param filePath   : example "/etc/json_file/vim_instances/vim-instance.json"
     * @param basePath
     */
    private static Map<String,Integer> mapCounter=new HashMap<>();
    private static int NSRCreated;

    public NetworkServiceDescriptorDelete(Properties p) {
        super(p, NetworkServiceDescriptor.class,"","/ns-descriptors");
    }

    @Override
    protected NetworkServiceDescriptor prepareObject() {
        return null;
    }

    public void setNSRCreated (int num){
        NSRCreated=num;
    }
    public void nSRDeleted(String id){
        if(mapCounter.get(id)==null)
            mapCounter.put(id,1);
        else{
            Integer previousCounter=mapCounter.get(id);
            mapCounter.put(id,++previousCounter);
        }
    }
    @Override
    protected Object doWork() throws Exception {
        String nsdId = (String) param;
        nSRDeleted(nsdId);
        log.debug("--Deleting NSD: " + nsdId + " counter:" + mapCounter.get(nsdId) + " and NSRCreated:" + NSRCreated);
        if(mapCounter.get(nsdId)==NSRCreated)
            delete(nsdId);
        return null;
    }

    @Override
    protected void handleException(Exception e) {

    }
}
