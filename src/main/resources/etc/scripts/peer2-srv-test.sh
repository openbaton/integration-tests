#!/bin/bash

# check number of incoming iperf connections
count=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | wc -l`
if [ $count -ne 2 ]
then
  exit 2
fi

# get the ip addresses of the hosts which connect to peer2
incoming1=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | head -n1`
incoming2=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | tail -n1`

if [ $incoming1 == ${peer5_private_ip} ] && [ $incoming2 == ${peer1_private_ip} ]
then
  exit 0
fi

if [ $incoming1 == ${peer1_private_ip} ] && [ $incoming2 == ${peer5_private_ip} ]
then
  exit 0
fi

exit 1

