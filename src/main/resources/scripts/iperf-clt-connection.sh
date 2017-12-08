#!/bin/bash

outgoing=`sudo netstat -npt | grep iperf | awk '{print $5}' | sed 's/:.*//'`

if [ $outgoing == ${server_ip} ]
then
  exit 0
else
  echo "This client connected to $outgoing but should have connected to ${server_ip}"
  exit 1
fi

