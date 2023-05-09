#!/usr/bin/env bash

# Install python 3.9
sudo apt update
sudo apt install software-properties-common
sudo add-apt-repository ppa:deadsnakes/ppa
sudo apt install python3.9

# Install package manager (poetry)
curl -sSL https://install.python-poetry.org | python3 -

# Clone repo
git clone https://github.com/basiliskus/devops-deployment-metrics.git
cd devops-deployment-metrics

# Install project dependencies
poetry install

# Create toml configuration file with custom values
sed -e '14,18d' -e 's/my_github_owner/CDCgov/' -e 's/my_github_repository/trusted-intermediary/' -e 's/12345678/42635194/' sample-config.toml > config.toml

# Run devops-deployment-metrics
poetry run devops-deployment-metrics -c config.toml
