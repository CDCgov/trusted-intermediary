#!/bin/bash

docker build -t ig-local-dev -f Dockerfile-local-dev .
docker run -it --rm --mount type=bind,source="$(pwd)",target=/trusted-intermediary ig-local-dev

if [[ "$OSTYPE" == "darwin"* ]]; then
  open ./output/index.html
fi
