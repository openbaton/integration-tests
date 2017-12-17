  <img src="https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/openBaton.png" width="250"/>
  
  Copyright © 2015-2017 [Open Baton](http://openbaton.org). 
  Licensed under [Apache v2 License](http://www.apache.org/licenses/LICENSE-2.0).

# Integration Tests

This project provides integration tests for Open Baton. 
Eleven different tests are provided by default. Extending this set of tests provided is also possible just replicating one of the ones already existing, and providing different descriptors. 

1. scenario-dummy-iperf
2. scenario-many-dependencies
3. scenario-real-iperf
4. scenario-complex-ncat
5. scenario-scaling
6. error-in-configure
7. error-in-instantiate
8. error-in-start
9. error-in-terminate
10. wrong-lifecycle-event
11. user-project-test
12. stress-test

**scenario-dummy-iperf** uses the [Dummy VNFM][vnfm-dummy] to simulate a VNFM and therefore tests the communication between NFVO and VNFM. 
It does not actually deploy a network service. The fake network service is a simple iperf scenario with one server and one client. 

**scenario-many-dependencies** also uses the Dummy VNFM but its fake network service is a little more complex in the sense that it has many VNFD with many dependencies between them. 

The test **scenario-real-iperf** actually deploys a network service on openstack. 
It consists of two VNFD and deploys one iperf server and two iperf clients. The clients contact the server. 

The test **scenario-complex-ncat** deploys a more complex network service on openstack. 
Five virtual machines will be running and acting as peers. 
The following picture shows the architecture in which the peers connect to each other using ncat and send their ip address. 
A peer at the beginning of an arrow acts as an ncat client and connects to the peer at the end of the arrow. 
The receiving peer stores the ip address of the sender so that it is possible to verify which peer connected to which. 
The two colours represent the two different networks on which the peers are running and connecting. 
Blue is the network *private* and black *private2*.

![Complex scenario][complex-ncat]

The test **scenario-scaling** tests the scaling function of Open Baton. 
It starts by deploying two VMs one acting as a ncat server and one as a ncat client which sends his ip address to the server so that it is possible to check if the client actually connected to the server. 
Then it executes some scaling functions like scale out and scale in and checks if new instances of the server and the client are deployed. Cases like scale in on just one instance and scale out on the maximum number of instances are included. 
It also examines if the client instances are provided with the ip addresses of the new server instances, so that they are able to connect to them. 
To see detailed information about which scaling functions are executed exactly please refer to the *scenario-scaling.ini* file in the project.

The tests **error-in-configure**, **error-in-instantiate**, **error-in-start**, **error-in-terminate** each deploy a network service from a NSD which contains a failing script in the particular lifecycle event and tests if the NFVO handles it correctly. 

The test **wrong-lifecycle-event** tries to onboard a NSD to the NFVO which contains an undefined lifecycle event. The test will pass if the onboarding is not successful. 

The **user-project-test** checks if the NFVO handles user and project management correctly. It adds and deletes users, projects and a vim instance from different 
user perspectives. This test can be executed without a VNFManager. 

The **stress-test** checks if the NFVO can handle a large number of NSR deployments from the same or different NSDs at the same time. This test uses the Dummy-VNFM. For this test you should set the property *nfvo.vmanager.executor.maxpoolsize* to a large number (e.g. 200) in the /etc/openbaton/openbaton.properties file.

In most of the tests a vim instance and a network service descriptor are stored on the orchestrator and the network service launched. 
If that is successful, the network service is stopped and the network service record, network service descriptor and the vim instance are removed. 
In the cases of the **scenario-real-iperf**, **scenario-complex-iperf** and **scenario-scaling** test also the service itself is tested, i.e. if iperf is running and the clients can connect to the server. 
Therefore the integration tests will execute some scripts for testing on the virtual machines. 

## Technical Requirements

Depending on the test cases selected you will probably need at least: 

1. A running NFVO. Follow the documentation [here][openbaton-doc] for several installation mechanisms.
2. A running VNFM: either the Dummy or the Generic VNFM (in case of execution of all tesitng scenarios you will need both)
3. A running VIM driver: either the OpenStack or the test one (in case of execution of all tesitng scenarios you will need both)
4. OpenStack as VIM in case of scenarios instantiating resources on a VIM openstack-based.

## How to install and configure the Integration Tests

Assuming that you have properly fulfilled the aforementioned technical requirements needed for your specific use cases, you will need to install and configure the integration-test tool. First of all, clone this repository in any path on your host:

```bash
git clone https://github.com/openbaton/integration-tests.git
```

The second step is to configure the properties file properly. You can find an example of the integration-tests.properties file in  *integration-tests/src/main/resources*. 
Open it and set the property values according to your needs. 

| Field          			| Value       																|
| -------------   		    | -------------:																|
| nfvo-ip  					| The ip of the machine on which the NFVO you want to use is running |
| nfvo-port					| The port on which the NFVO is running |
| nfvo-usr					| The username required for logging in the NFVO |
| nfvo-pwd                  | The password required for logging in the NFVO |
| nfvo-project-name         | The name of the project that the integration tests shall use |
| nfvo-ssl-enabled			| Set this to *true* if the NFVO uses SSL |
| local-ip					| The ip of the machine on which the integration test is running |
| rest-waiter-port          | Port where the http server will be listening. 0 or null for random |
| clear-after-test          | If set to *true*, the NFVO will be cleared of all the remaining NSRs, NSD, VNFPackages and Vim-Instances left from previous test |
| ssh-private-key-file-path | Private key file path used for scp and ssh commands. If null none will be used |
| integration-test-scenarios | Here you can specify a folder in which you can put integration test scenarios. The *.ini* files in this folder overwrite the ones in the projects resource folder |
| nsd-path                  | Path to directory with custom network service descriptors |
| vim-path                  | Path to directory with custom vim json files |
| scripts-path              | Path to directory with custom script files |


In case you plan to use scenarios which are instantiating VMs on OpenStack, you need a keypair.
Import a key pair in the OpenStack dashboard, give it a name and assign the public key of the host, on which the integration tests will run, to it. 

The next step is to create a vim file. 
Here is an example where you just have to change some fields. 
```json
{
  "name":"vim-instance",
  "authUrl":"http://your-openstack-url",
  "tenant":"the tenant you use",
  "username":"openstack username",
  "password":"openstack password",
  "keyPair":"the name of the imported key pair",
  "securityGroups": [
    "default"
  ],
  "type":"openstack",
  "location":{
    "name":"your location",
    "latitude":"the latitude",
    "longitude":"the longitude"
  }
}
```

Name the vim file *real-vim.json* and add it to the folder *integration-tests/src/main/resources/etc/json_file/vim_instances/* in the project.
In the folder *integration-tests/src/main/resources/etc/json_file/network_service_descriptors* of the project you will find a file named NetworkServiceDescriptor-iperf-real.json and one named NetworkServiceDescriptor-complex-iperf.json. 
Make sure that the official Ubuntu cloud image [ubuntu-14.04-server-cloudimg-amd64-disk1][ubuntu-image] is available on the glance image repository in your OpenStack instance. 

The scenario *error-in-terminate.ini* needs some special configuration in the NFVO if you want to run it. Change **nfvo.delete.vnfr.wait** to **true** in */etc/openbaton/openbaton.properties* and restart the NFVO if it was already running.

Then use a terminal to navigate into the project's root directory and execute the command *./integration-tests.sh compile*.

## How to use the Integration Tests

### Start the Integration Test

Before starting the integration tests be sure that the NFVO, Generic VNFM and Dummy VNFM you want to use are already running. 
Then start the test by executing *./integration-tests.sh start*.
It is possible to specify the test scenarios you want to run so that not every test in the */src/main/resources/integration-test-scenarios* folder is executed. 
Therefore use additional command line arguments while starting the integration tests. Every scenario occuring as an argument will be executed. For example *./integration-tests.sh start scenario-real-iperf.ini scenario-scaling.ini* will just execute the tests described in the files *scenario-real-iperf.ini* and *scenario-scaling.ini* located in the folder */src/main/resources/integration-test-scenarios*.
If you do not pass any command line arguments, every available scenario will be executed. To see which scenarios are available execute *./integration-tests.sh list*.

### Test results

While the tests are running they will produce output to the console. This output will be logged in the file integration-test.log which is in the project's root directory. 
If a test finished it will either tell you that it passed successfully or not. 
If it did not pass correctly you will find the reason in the log file. 

## How to extend the Integration Tests

You can write your own integration test scenarios. 
Please refer to [the documentation][integration-test-write] to learn more.

## Issue tracker

Issues and bug reports should be posted to the GitHub Issue Tracker of this project

## What is Open Baton?

Open Baton is an open source project providing a comprehensive implementation of the ETSI Management and Orchestration (MANO) specification and the TOSCA Standard.

Open Baton provides multiple mechanisms for interoperating with different VNFM vendor solutions. It has a modular architecture which can be easily extended for supporting additional use cases. 

It integrates with OpenStack as standard de-facto VIM implementation, and provides a driver mechanism for supporting additional VIM types. It supports Network Service management either using the provided Generic VNFM and Juju VNFM, or integrating additional specific VNFMs. It provides several mechanisms (REST or PUB/SUB) for interoperating with external VNFMs. 

It can be combined with additional components (Monitoring, Fault Management, Autoscaling, and Network Slicing Engine) for building a unique MANO comprehensive solution.

## Source Code and documentation

The Source Code of the other Open Baton projects can be found [here][openbaton-github] and the documentation can be found [here][openbaton-doc]

## News and Website

Check the [Open Baton Website][openbaton]

Follow us on Twitter @[openbaton][openbaton-twitter]

## Licensing and distribution
Copyright © [2015-2017] Open Baton project

Licensed under the Apache License, Version 2.0 (the "License");

you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Support
The Open Baton project provides community support through the Open Baton Public Mailing List and through StackOverflow using the tags openbaton.

## Supported by
  <img src="https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/fokus.png" width="250"/><img src="https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/tu.png" width="150"/>

[fokus-logo]: https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/fokus.png
[openbaton]: http://openbaton.org
[openbaton-doc]: http://openbaton.org/documentation
[openbaton-github]: http://github.org/openbaton
[openbaton-logo]: https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/openBaton.png
[openbaton-mail]: mailto:users@openbaton.org
[openbaton-twitter]: https://twitter.com/openbaton
[tub-logo]: https://raw.githubusercontent.com/openbaton/openbaton.github.io/master/images/tu.png
[complex-ncat]: complex-ncat.png
[integration-test-write]: http://openbaton.github.io/documentation/integration-test-write
[vnfm-dummy]: https://github.com/openbaton/dummy-vnfm-amqp
[ubuntu-image]:https://uec-images.ubuntu.com/releases/14.04/release/ubuntu-14.04-server-cloudimg-amd64-disk1.img
