#!/usr/bin/env bash


IMAGE_NAME='pwcracker-server'
IS_IMAGE_EXIST=$(docker images | grep ${IMAGE_NAME})
if [ "$IS_IMAGE_EXIST" ]; then
    echo "delete old image and rebuild the new one"
    docker rmi -f ${IMAGE_NAME}
fi
sbt assembly
docker build -t ${IMAGE_NAME} .