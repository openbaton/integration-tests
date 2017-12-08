#!/bin/bash

# Kill the running sipp server
sudo pkill screen
echo "SIPp server killed"

timeout=300
seconds=0
until [ $(ps aux | grep -v grep | grep -v "kill-sipp-server-and-" | grep sipp | wc -l) -gt 1 ]
do
	if [ $seconds -ge $timeout ]
		then
			exit 1
		fi
	echo "waiting for healing (since $seconds seconds).. timeout is $timeout seconds"
	((seconds+=5))
	sleep 5
done

echo "SIPp server healed"
