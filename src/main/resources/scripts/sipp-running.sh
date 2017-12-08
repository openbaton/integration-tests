#!/bin/bash

sipp_count=$(ps -aux | grep -v grep | grep -v "sipp-running" | grep sipp | wc -l)
if [ $sipp_count -lt 1 ]
then
  exit 1
else
  exit 0
fi

