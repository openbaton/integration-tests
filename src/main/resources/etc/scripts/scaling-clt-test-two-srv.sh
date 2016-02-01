#!/bin/bash

outgoing=`sudo netstat -npt | grep iperf | awk '{print $5}' | sed 's/:.*//'`

count=`echo "$outgoing"  wc -l`
if [ $count -ne 2 ] 
then
  exit 2
fi

outgoing1=`echo "$outgoing" | head -n1`
outgoing2=`echo "$outgoing" | tail -n1`

echo server_ip=${server_ip} outgoing1=$outgoing1 outgoing2=$outgoing2 >> i

# if outgoing1 and 2 are equal then the client seems to contact the same server which is not correct
if [ $outgoing1 == $outgoing2 ]
then
  exit 3
fi


if [ $outgoing1 == ${server_ip} -o $outgoing2 == ${server_ip} ]
then
  exit 0
else
  exit 1
fi

