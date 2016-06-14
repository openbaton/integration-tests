#!/bin/bash

# check number of incoming ncat connections
count=`cat received | wc -l`
if [ $count -ne 3 ]
then
  echo "Peer3 expected to be contacted by three peers (peer2, peer4 and peer5) but it was contacted by $count peers with the following ips:"
  while read line ; do
    echo "$line"
  done < received
  exit 2
fi

# tests if peer2, peer4 and peer5 sent their ip addresses to peer3 by checking if the ip addresses are in the 'received' file on the peer
grep ${peer2_private_ip} received -q
if [ $? -ne 0 ]; then
  echo "Peer2 with ip ${peer2_private_ip} did not connect to peer3. The following peers connected to this peer:"
  while read line ; do
    echo "$line"
  done < received
  exit 1;
fi

grep ${peer4_private2_ip} received -q
if [ $? -ne 0 ]; then
  echo "Peer4 with ip ${peer4_private2_ip} did not connect to peer3. The following peers connected to this peer:"
  while read line ; do
    echo "$line"
  done < received
  exit 1;
fi

grep ${peer5_private2_ip} received -q
if [ $? -ne 0 ]; then
  echo "Peer5 with ip ${peer5_private2_ip} did not connect to peer3. The following peers connected to this peer:"
  while read line ; do
    echo "$line"
  done < received
  exit 1;
fi