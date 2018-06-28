#!/bin/bash

PACKAGES=default-jdk docker.io maven

# Check for sudo/root
printf "Checking for root permissions..."
if [[ $(id -u) -ne 0 ]]; then
	>&2 echo "Error: Root permissions required"
	exit 1
else
	apt install -y $PACKAGES
fi