#!/bin/bash

echo ncatclient_ip=${ncatclient_ip} >> i

# tests if all the clients sent their ip addresses to the servers by checking if the ip addresses are in the 'received' file on the server
grep ${ncatclient_ip} received -q
if [ $? -ne 0 ]; then
  exit 1;
fi