#!/bin/bash

iperf_count=`exec ps -aux | grep -v grep | grep iperf | wc -l`
if [ $iperf_count -lt 1 ]
then
  exit 1
else
  exit 0
fi

