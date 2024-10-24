#!/bin/bash

# Check if all required arguments are provided
if [ "$#" -ne 4 ]; then
    echo "Usage: $0 <datetime>,<data-point1>,<data-point2>,<message>"
    exit 1
fi

# Parse arguments
datetime=$(echo $1 | cut -d',' -f1)
data_point1=$(echo $1 | cut -d',' -f2)
data_point2=$(echo $1 | cut -d',' -f3)
message=$2

# Put log to CloudWatch Logs
log_group_name="/lab/log-group"
log_stream_name="<log-stream-name>"

aws logs put-log-events \
    --log-group-name $log_group_name \
    --log-stream-name $log_stream_name \
    --log-events "[{\"timestamp\": $(date -d "$datetime" +%s)000, \"message\": \"$message, data_point1=$data_point1, data_point2=$data_point2\"}]"
