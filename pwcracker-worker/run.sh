#!/usr/bin/env bash

if [ "$#" -ne 2 ]; then
    echo "need to specify redis and rabbit endpoint for example ./run.sh <redis_endpoint> <rabbit_endpoint>"
else
    docker run -d \
            --name=worker \
            -e REDIS_ENDPOINT=$1 \
            -e RABBIT_ENDPOINT=$2 \
            pwcracker-worker
fi