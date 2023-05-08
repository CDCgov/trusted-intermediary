#!/usr/bin/env bash

# Install python 3.9
sudo apt update
sudo apt install software-properties-common
sudo add-apt-repository ppa:deadsnakes/ppa
sudo apt install python3.9

# Install package manager (poetry)
curl -sSL https://install.python-poetry.org | python3 -

# Install project dependencies
poetry install
