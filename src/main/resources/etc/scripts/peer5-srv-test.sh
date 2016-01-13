#!/bin/bash

# check number of incoming iperf connections
count=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | wc -l`
if [ $count -ne 1 ]
then
  exit 2
fi

# get the ip address of the host which connects to peer5
incoming=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//'`

if [ $incoming == ${peer4_private2_ip} ]
then
  exit 0
fi

exit 1
