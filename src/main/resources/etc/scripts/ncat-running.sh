#!/bin/bash

ncat_count=$(ps -aux | grep -v grep | grep -v "ncat-running" | grep ncat | wc -l)
if [ $ncat_count -lt 1 ]
then
  exit 1
else
  exit 0
fi
