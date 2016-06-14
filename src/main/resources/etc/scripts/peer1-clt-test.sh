#!/bin/bash

# check if this peer connected to the correct peer(s)
grep Peer2 sent -q
if [ $? -ne 0 ]; then
  echo "Peer1 did not connect to peer2. Instead it connected to the following peers:"
  while read line ; do
    echo "$line"
  done < sent
  exit 1
fi