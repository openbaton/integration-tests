#!/bin/bash

incoming=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//'`

# check number of incoming iperf connections
count=`echo "$incoming" | wc -l`
if [ $count -ne 2 ]
then
  exit 2
fi

# get the ip addresses of the hosts which connect to peer2
incoming1=`echo "$incoming" | head -n1`
incoming2=`echo "$incoming" | tail -n1`

if [ $incoming1 == ${peer5_private_ip} ] && [ $incoming2 == ${peer1_private_ip} ]
then
  exit 0
fi

if [ $incoming1 == ${peer1_private_ip} ] && [ $incoming2 == ${peer5_private_ip} ]
then
  exit 0
fi

exit 1

