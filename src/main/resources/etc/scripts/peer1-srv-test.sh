#!/bin/bash

# get the ip address of the host which connects to peer1
incoming=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//'`

# check number of incoming iperf connections
count=`echo "$incoming" | wc -l`
if [ $count -ne 1 ]
then
  exit 2
fi

if [ $incoming == ${peer5_private_ip} ] 
then
  exit 0
else
  exit 1
fi
