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
package org.openbaton.integration.test.utils;

import java.util.*;

/**
 * Created by tbr on 02.12.15.
 *
 * <p>Used by the GenericServiceTester class.
 */
public class VNFCRepresentation {

  public String toString() {
    return "VNFC " + vnfrName + " fips " + getAllFips() + " ips " + getAllIps();
  }

  private String hostname;
  private String vnfrName;
  private Map<String, String> configuration = new HashMap<>();
  private Map<String, List<String>> netIps = new HashMap<>();
  private Map<String, List<String>> netFips = new HashMap<>();

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getVnfrNname() {
    return vnfrName;
  }

  public void setVnfrName(String name) {
    this.vnfrName = name;
  }

  public void addConfiguration(String k, String v) {
    configuration.put(k, v);
  }

  public void setConfiguration(Map<String, String> c) {
    this.configuration = c;
  }

  public String getConfiguration(String key) {
    return configuration.get(key);
  }

  public Set<String> getAllConfigurationKeys() {
    return configuration.keySet();
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public void addNetIp(String net, String ip) {
    if (!netIps.containsKey(net)) {
      List<String> ipList = new LinkedList<>();
      ipList.add(ip);
      netIps.put(net, ipList);
    }
    if (!netIps.get(net).contains(ip)) netIps.get(net).add(ip);
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
    if (!netFips.get(net).contains(fip)) netFips.get(net).add(fip);
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
    Set<String> netNames = new HashSet<>();
    netNames.addAll(netIps.keySet());
    for (String net : netFips.keySet()) {
      if (!netNames.contains(net)) netNames.add(net);
    }
    return netNames;
  }
}
