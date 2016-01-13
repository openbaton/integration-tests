#!/bin/bash

# check number of incoming iperf connections
count=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | wc -l`
if [ $count -ne 3 ]
then
  exit 2
fi

# get the ip addresses of the hosts which connect to peer3
incoming1=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | head -n1`
incoming2=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | head -n2 | tail -n1`
incoming3=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | tail -n1`


if [ $incoming1 == ${peer2_private_ip} ] && [ $incoming2 == ${peer4_private2_ip} ] && [ $incoming3 == ${peer5_private2_ip} ] 
then
  exit 0
fi

if [ $incoming1 == ${peer2_private_ip} ] && [ $incoming2 == ${peer5_private2_ip} ] && [ $incoming3 == ${peer4_private2_ip} ] 
then
  exit 0
fi

if [ $incoming1 == ${peer4_private2_ip} ] && [ $incoming2 == ${peer2_private_ip} ] && [ $incoming3 == ${peer5_private2_ip} ] 
then
  exit 0
fi

if [ $incoming1 == ${peer4_private2_ip} ] && [ $incoming2 == ${peer5_private2_ip} ] && [ $incoming3 == ${peer2_private_ip} ] 
then
  exit 0
fi

if [ $incoming1 == ${peer5_private2_ip} ] && [ $incoming2 == ${peer2_private_ip} ] && [ $incoming3 == ${peer4_private2_ip} ] 
then
  exit 0
fi

if [ $incoming1 == ${peer5_private2_ip} ] && [ $incoming2 == ${peer4_private2_ip} ] && [ $incoming3 == ${peer2_private_ip} ] 
then
  exit 0
fi

exit 1
