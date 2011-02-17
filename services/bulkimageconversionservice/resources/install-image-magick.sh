#!/bin/bash

sudo apt-get update
sudo su -c "export DEBIAN_FRONTEND='noninteractive'; apt-get -y install libc6-i386"
sudo apt-get -y install imagemagick

