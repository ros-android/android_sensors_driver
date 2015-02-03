#!/bin/bash

PROJECT_DIR=~/ANPL

# clone android_core , android_extras
cd ${PROJECT_DIR}/rosjava/src
git clone -b indigo https://github.com/talregev/android_core.git
git clone -b indigo https://github.com/rosjava/android_extras.git
