# How to use the Integration Tests

## Overview

This project provides integration tests. 
Three tests are run.

1. scenario-dummy-iperf
2. scenario-many-dependencies
3. scenario-real-iperf
4. scenario-complex-iperf

scenario-dummy-iperf uses the Dummy VNFM to simulate a VNFM and therefore tests the communication between NFVO and VNFM. 
It does not actually deploy a network service. The fake network service is a simple iperf scenario with one server and one client. 

scenario-many-dependencies also uses the Dummy VNFM but its fake network service is a little bit more complex in the sense that it has many VNFD with many dependencies between them. 

The test scenario-real-iperf actually deploys a network service on openstack. 
It consists of two VNFD and deploys one iperf server and two iperf clients. The clients contact the server. 

The test scenario-complex-iperf deploys a more complex network service on openstack. 
Five virtual machines will be running and acting as peers. 
The following picture shows the architecture in which the peers connect to each other using iperf. 
A peer at the beginning of an arrow acts as an iperf client and connects to the peer at the end of the arrow. 
The two colours represent the two different networks on which the peers are running and connecting. 
Blue is the network *private* and black *private2*.

![Complex scenario][complex-iperf]

In every test a vim instance and a network service descriptor are stored on the orchestrator and the network service launched. 
If that is successful, the network service is stoped and the network service record, network service descriptor and the vim instance are removed. 
In the cases of the scenario-real-iperf and scenario-complex-iperf test also the service itself is tested, i.e. if iperf is running and the clients can connect to the server. 

## Requirements

1. A running NFVO
2. A running Generic VNFM
3. A running Dummy VNFM

## Installation and configuration

Clone the project to your machine. 
In *integration-tests/src/main/resources* is a file named integration-test.properties. 
Open it and set the property values according to your needs. 

| Field          				| Value       																|
| -------------   				| -------------:																|
| nfvo-ip  					| The ip of the machine on which the NFVO you want to use is running |
| nfvo-port					| The port on which the NFVO is running |
| nfvo-usr					| The username if a login is required for the NFVO |
| nfvo-pwd                                      | The password if a login is required for the NFVO |
| local-ip					| The ip of the machine on which the integration test is running |

After that you will also need a keypair for openstack. Create one and download the private key as a .pem file. 
Rename it to integration-test.pem and provide it with the needed permissions by executing *chmod 400 integration-test.pem*.
Create the directory */etc/openbaton/integration-test* on your machine and move the pem file into it. 
The next step is to create a vim file. 
Here is an example where you just have to change some fields. 
```json
{
  "name":"vim-instance",
  "authUrl":"http://your-openstack-url",
  "tenant":"the tenant you use",
  "username":"openstack username",
  "password":"openstack password",
  "keyPair":"in here the one you created",
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
We used the image ubuntu-14.04-server-cloudimg-amd64-disk1 for the virtual machines in openstack. 
If you want to use another image, change every occurence of the above mentioned image in those two files to the name of the one you want to use and of course make sure it is available on openstack. 
If you want to use another image, change every occurence of the above mentioned image to the name of the one you want to use and of course make sure it is available on openstack. 
If you are using another image, you also have to configure the .ini files of the scenario-complex-iperf and scenario-real-iperf. 
That is you have to look for the tasks of *GenericServiceTester* and change the value of vm-scripts-path to the path where you want to have the test scripts stored on your virtual machine and the value of user-name according to the user name used on the virtual machines deployed by the image. These were the steps to use another image. 

Then use a shell to navigate into the project's root directory. 
Execute the command *./gradlew clean build*.
After that you will find the folder *build/libs/* in the project. Inside of this folder is the project's executable jar file. 

## Start the integration test

Before starting the integration test be sure that the NFVO, Generic VNFM and Dummy VNFM you want to use are already running. 
Then start the test by navigating into the folder *integration-tests/build/libs* and execute the command *java -jar integration-tests-0.15-SNAPSHOT.jar*.

## Test results

While the tests are running they will produce output to the console. This output will be logged in the file integration-test.log which is in the project's root directory. 
If a test finished it will either tell you that it passed successfully or not. 
If it did not pass correctly you will find the reason in the log file. 

## Write your own integration tests
Please refer to *http://openbaton.github.io/documentation/integration-test-write/*.

## Development

* Want to contribute? Great!

## News and Website

* Information about OpenBaton can be found on our website. Follow us on Twitter @openbaton.

<!---
References
-->

[complex-iperf]:complex-iperf.png
