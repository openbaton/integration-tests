#!/bin/bash

echo ncatclient_ip=${ncatclient_ip} >> i

# tests if all the clients sent their ip addresses to the servers by checking if the ip addresses are in the 'received' file on the server
grep ${ncatclient_ip} received -q
if [ $? -ne 0 ]; then
  echo "The ncat client with ip ${ncatclient_ip} did not connect to this ncat server. The following clients connected to this server:"
  while read line ; do
    echo "$line"
  done < received
  exit 1;
fi