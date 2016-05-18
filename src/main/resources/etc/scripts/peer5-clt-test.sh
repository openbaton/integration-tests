#!/bin/bash

outgoing=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep 5001 | sed 's/:.*//'`

# check number of outgoing iperf connections
count=`echo "$outgoing" | wc -l`
if [ $count -ne 3 ]
then
  exit 2
fi

# get the ip addresses of the hosts to which peer5 connects to
outgoing1=`echo "$outgoing" | head -n1`
outgoing2=`echo "$outgoing" | head -n2 | tail -n1`
outgoing3=`echo "$outgoing" | tail -n1`

if [ $outgoing1 == ${peer1_private_ip} ] && [ $outgoing2 == ${peer2_private_ip} ] && [ $outgoing3 == ${peer3_private2_ip} ]
then
  exit 0
fi

if [ $outgoing1 == ${peer1_private_ip} ] && [ $outgoing2 == ${peer3_private2_ip} ] && [ $outgoing3 == ${peer2_private_ip} ] 
then
  exit 0
fi

if [ $outgoing1 == ${peer2_private_ip} ] && [ $outgoing2 == ${peer1_private_ip} ] && [ $outgoing3 == ${peer3_private2_ip} ] 
then
  exit 0
fi

if [ $outgoing1 == ${peer2_private_ip} ] && [ $outgoing2 == ${peer3_private2_ip} ] && [ $outgoing3 == ${peer1_private_ip} ] 
then
  exit 0
fi

if [ $outgoing1 == ${peer3_private2_ip} ] && [ $outgoing2 == ${peer1_private_ip} ] && [ $outgoing3 == ${peer2_private_ip} ] 
then
  exit 0
fi

if [ $outgoing1 == ${peer3_private2_ip} ] && [ $outgoing2 == ${peer2_private_ip} ] && [ $outgoing3 == ${peer1_private_ip} ] 
then
  exit 0
fi

exit 1
