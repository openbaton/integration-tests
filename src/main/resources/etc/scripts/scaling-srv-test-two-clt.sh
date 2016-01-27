#!/bin/bash

count=`sudo netstat -ntp | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | wc -l`
if [ $count -ne 2] 
then
  exit 2
fi

incoming1=`sudo netstat -npt | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | head -n1`
incoming2=`sudo netstat -npt | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//' | tail -n1`

echo client_ip=${client_ip} incoming1=$incoming1 incoming2=$incoming2 >> i

# if incoming1 and 4 are equal then the server seems to be contacted by the same cliet which is not correct
if [ $incoming1 == $incoming2 ]
then
  exit 3
fi


if [ $incoming1 == ${client_ip} -o $incoming2 == ${client_ip} ]
then
  exit 0
else
  exit 1
fi

