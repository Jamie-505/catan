#!/usr/bin/env bash

set -e # exit on failure

export app_name="catanBackend"

ln -sf `pwd`/server ${app_name} # set a link to this build
mv ${app_name} ~
cd ~/${app_name}
./gradlew bootRepackage

sudo systemctl restart catanBackend.service
#gradle bootrun