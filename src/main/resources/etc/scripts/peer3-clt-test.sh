#!/bin/bash

# check if this peer connected to the correct peer(s)
grep Peer4 sent -q
if [ $? -ne 0 ]; then
  echo "Peer3 did not connect to peer4. Instead it connected to the following peers:"
  while read line ; do
    echo "$line"
  done < sent
  exit 1
fi