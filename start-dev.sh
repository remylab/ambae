#!/usr/bin/sh

docker build . --tag ambae --no-cache
docker-compose --file ./dev/docker-compose.yml up