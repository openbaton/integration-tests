package org.openbaton.integration.test.utils;

import java.util.*;

/**
 * Created by tbr on 02.12.15.
 */
public class VNFCRepresentation {

    private String hostname;
    private Map<String, String> configuration = new HashMap<>();
    private Map<String, List<String>> netIps = new HashMap<>();
    private Map<String, List<String>> netFips = new HashMap<>();

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void addConfiguration(String k, String v) {
        configuration.put(k, v);
    }

    public void setConfiguration(Map<String, String> c) {
        this.configuration=c;
    }

    public String getConfiguration(String key) {
        return configuration.get(key);
    }

    public Set<String> getAllConfigurationKeys() {
        return configuration.keySet();
    }

    public Map<String,String> getConfiguration() {
        return configuration;
    }

    public void addNetIp(String net, String ip) {
        if (!netIps.containsKey(net)) {
            List<String> ipList = new LinkedList<>();
            ipList.add(ip);
            netIps.put(net, ipList);
        }
        if (!netIps.get(net).contains(ip))
            netIps.get(net).add(ip);
    }

    public List<String> getIpsByNet(String net) {
        return netIps.get(net);
    }

    public List<String> getAllIps() {
        List<String> ips = new LinkedList<>();
        for (String net : netIps.keySet()) {
            ips.addAll(getIpsByNet(net));
        }
        return ips;
    }

    public void addNetFip(String net, String fip) {
        if (!netFips.containsKey(net)) {
            List<String> fipList = new LinkedList<>();
            fipList.add(fip);
            netFips.put(net, fipList);
        }
        if (!netFips.get(net).contains(fip))
            netFips.get(net).add(fip);
    }

    public List<String> getFipsByNet(String net) {
        return netFips.get(net);
    }

    public List<String> getAllFips() {
        List<String> fips = new LinkedList<>();
        for (String net : netFips.keySet()) {
            fips.addAll(getFipsByNet(net));
        }
        return fips;
    }

    public Set<String> getAllNetNames() {
        Set<String> netNames = netIps.keySet();
        for (String net : netFips.keySet()) {
            if (!netNames.contains(net))
                netNames.add(net);
        }
        return netNames;
    }
}
