#!/bin/bash

#from http://wiki.ros.org/sig/Rosjava/Android%20Studio/Download

#If you're on a 64-bit machine, you may need the following libraries:
#from https://developer.android.com/sdk/installing/index.html?pkg=tools
# at Troubleshooting Ubuntu
sudo dpkg --add-architecture i386
sudo apt-get update
sudo apt-get install libncurses5:i386 libstdc++6:i386 zlib1g:i386

#from http://paolorotolo.github.io/android-studio/,
#add that repository with the following command:
sudo apt-add-repository ppa:paolorotolo/android-studio

#Update the APT with the command:
sudo apt-get update

#install the program with the command:
sudo apt-get install android-studio

#install java orcle jdk
install-java-jdk.sh

