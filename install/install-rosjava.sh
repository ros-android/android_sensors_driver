#!/bin/bash

#from http://wiki.ros.org/rosjava/Tutorials/indigo/Installation

PROJECT_DIR=~/ANPL

#3.1 Preparation
sudo apt-get install ros-indigo-catkin ros-indigo-ros python-wstool

#3.2 Sources

rm -rf ${PROJECT_DIR}/rosjava
mkdir -p ${PROJECT_DIR}/rosjava
wstool init -j4 ${PROJECT_DIR}/rosjava/src https://raw.githubusercontent.com/yujinrobot/yujin_tools/master/rosinstalls/indigo/rosjava.rosinstall
source /opt/ros/indigo/setup.bash
cd ${PROJECT_DIR}/rosjava
# Make sure we've got all rosdeps and msg packages.
rosdep update
rosdep install --from-paths src -i -y
catkin_make

echo "source ${PROJECT_DIR}/rosjava/devel/setup.bash">> ~/.bashrc
source ${PROJECT_DIR}/rosjava/devel/setup.bash

#install java orcle jdk
install-java-jdk.sh
