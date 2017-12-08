#!/bin/bash

# check number of incoming ncat connections
count=`cat received | wc -l`
if [ $count -ne 2 ]
then
  echo "Peer2 expected to be contacted by two peers (peer1 and peer5) but it was contacted by $count peers with the following ips:"
  while read line ; do
    echo "$line"
  done < received
  exit 2
fi

# tests if peer1 and peer5 sent their ip addresses to peer2 by checking if the ip addresses are in the 'received' file on the peer
grep ${peer1_private_ip} received -q
if [ $? -ne 0 ]; then
  echo "Peer1 with ip ${peer1_private_ip} did not connect to peer2. The following peers connected to this peer:"
  while read line ; do
    echo "$line"
  done < received
  exit 1;
fi

grep ${peer5_private_ip} received -q
if [ $? -ne 0 ]; then
  echo "Peer5 with ip ${peer5_private_ip} did not connect to peer2. The following peers connected to this peer:"
  while read line ; do
    echo "$line"
  done < received
  exit 1;
fi