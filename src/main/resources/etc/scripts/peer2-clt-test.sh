#!/bin/bash

# check number of outgoing iperf connections
count=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep 5001 | sed 's/:.*//' | wc -l`
if [ $count -ne 1 ]
then
  exit 2
fi

# get the ip address of the host to which peer2 connects to
outgoing=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep 5001 | sed 's/:.*//'`

if [ $outgoing == ${peer3_private_ip} ]
then
  exit 0
fi

exit 1
