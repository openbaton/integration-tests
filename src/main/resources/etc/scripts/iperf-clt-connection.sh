#!/bin/bash

outgoing=`sudo netstat -npt | grep iperf | awk '{print $5}' | sed 's/:.*//'`

if [ $outgoing == ${server_ip} ]
then
  exit 0
else
  exit 1
fi

