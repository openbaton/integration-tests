#!/bin/bash

incoming=`sudo netstat -npt | grep iperf | awk '{print $5}' | grep -v 5001 | sed 's/:.*//'`

count=`echo "$incoming" | wc -l`
if [ $count -ne 1 ] 
then
  exit 1
fi

echo client_ip=${client_ip} incoming=$incoming >> i


if [ $incoming == ${client_ip} ]
then
  exit 0
else
  exit 1
fi

