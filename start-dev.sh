#!/usr/bin/sh

docker build . --tag ambae --no-cache
docker-compose up