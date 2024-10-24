#!/bin/bash

# brew install k6

k6 run script.js    # 10
k6 run --vus 2 --duration 10s script.js # 2000
k6 run --vus 20 --duration 10s script.js # 2000
k6 run --vus 50 --duration 10s script.js # 2000

## limit
k6 run --vus 100 --duration 10s script.js 

curl http://localhost:8092/value
curl http://localhost:8092/reset



## see: https://k6.io/docs/misc/fine-tuning-os/#viewing-limits-configuration
#ulimit -n 65535
## unlock on OS X
#sudo sysctl -w kern.maxfiles=20480
sysctl kern.maxfiles
sysctl kern.maxfilesperproc
csrutil disable

## Soft limit
sudo launchctl limit maxfiles 65536 200000