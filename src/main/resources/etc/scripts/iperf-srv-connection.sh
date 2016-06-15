#!/bin/bash


incoming=`sudo netstat -npt | grep iperf | wc -l`

if [ $incoming -eq 0 ] 
then
  echo "Expected an incoming iperf connection but no client connected to this server"
  exit 1
else
  exit 0
fi
