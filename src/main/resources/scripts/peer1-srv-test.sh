#!/bin/bash

# check number of incoming ncat connections
count=`cat received | wc -l`
if [ $count -ne 1 ]
then
  echo "Peer1 expected to be contacted by one peer (peer5) but it was contacted by $count peers with the following ips:"
  while read line ; do
    echo "$line"
  done < received
  exit 2
fi

# tests if peer5 sent its ip address to peer1 by checking if the ip address is in the 'received' file on the peer
grep ${peer5_private_ip} received -q
if [ $? -ne 0 ]; then
  echo "Peer5 with ip ${peer5_private_ip} did not connect to peer1. The following peers connected to this peer:"
  while read line ; do
    echo "$line"
  done < received
  exit 1;
fi