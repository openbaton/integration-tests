#!/bin/bash

# check number of incoming ncat connections
count=`cat received | wc -l`
if [ $count -ne 1 ]
then
  echo "Peer4 expected to be contacted by one peer (peer3) but it was contacted by $count peers with the following ips:"
  while read line ; do
    echo "$line"
  done < received
  exit 2
fi

# tests if peer3 sent its ip address to peer4 by checking if the ip address is in the 'received' file on the peer
grep ${peer3_private2_ip} received -q
if [ $? -ne 0 ]; then
  echo "Peer3 with ip ${peer3_private2_ip} did not connect to peer4. The following peers connected to this peer:"
  while read line ; do
    echo "$line"
  done < received
  exit 1;
fi