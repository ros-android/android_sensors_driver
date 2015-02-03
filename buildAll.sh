#!/bin/bash

PROJECT_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
KEY=${PROJECT_PATH}/keys/key.jks

if [ ! -f ${KEY} ]; then 
    echo "Please enter password for your new key,"
    echo "and other detail you will ask."
    echo "Remember your password, you will ask again,"
    echo "when it install the APK"
	./createKey.sh key
fi

./build.sh key
