#!/bin/bash

# from http://vardhan-justlikethat.blogspot.co.il/2012/05/android-solution-install-parse-failed.html

PROJECT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR=`basename $PROJECT_PATH`
KEY=${PROJECT_PATH}/keys/${1}.jks
ALIAS="org.ros.android.$PROJECT_DIR"

if [ -z "$1" ]; then
    echo "Please enter your key name."
    echo "example: ./buildAll.sh tal will create keys/tal.jks" 
    exit
fi

keytool -genkey -v -keystore ${KEY} -alias ${ALIAS} -keyalg RSA -keysize 2048 -validity 20000
