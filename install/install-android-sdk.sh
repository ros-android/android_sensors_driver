#!/bin/bash

#from https://developer.android.com/sdk/installing/index.html?pkg=tools
#install sdk 
SDK_NAME="Sdk"
APP_DIR=~/Android
cd ~/Downloads
wget -O android-sdk.tgz 'https://dl.google.com/android/android-sdk_r24.0.2-linux.tgz'
rm -rf ${APP_DIR}/$SDK_NAME
mkdir -p ${APP_DIR}
tar -xvzf android-sdk.tgz -C ${APP_DIR}
cd ${APP_DIR}
mv android-sdk-linux/ $SDK_NAME/
rm -f ~/Downloads/android-sdk.tgz

#Add android-studio and sdk to your PATH
echo "export PATH=\$PATH:${APP_DIR}/$SDK_NAME/tools:${APP_DIR}/$SDK_NAME/platform-tools:/opt/android-studio/bin">> ~/.bashrc
echo "export ANDROID_HOME=${APP_DIR}/$SDK_NAME">> ~/.bashrc

export PATH=$PATH:${APP_DIR}/$SDK_NAME/tools:${APP_DIR}/$SDK_NAME/platform-tools:/opt/android-studio/bin
export ANDROID_HOME=${APP_DIR}/$SDK_NAME

#from http://stackoverflow.com/questions/17963508/how-to-install-android-sdk-build-tools-on-the-command-line
#add package to android sdk

#from http://stackoverflow.com/questions/6775904/grepping-using-the-alternative-operator
#from http://stackoverflow.com/questions/16015590/bash-extract-number-from-string
#from http://stackoverflow.com/questions/4594319/shell-replace-cr-lf-by-comma
#from http://stackoverflow.com/questions/369758/how-to-trim-whitespace-from-bash-variable
NUMBERS=`android list sdk --all | grep -E "Android SDK Tools|Android SDK Platform-tools|Android SDK Build-tools, revision 21.1.2|Android SDK Build-tools, revision 19.1|SDK Platform Android 5.0.1, API 21|SDK Platform Android 4.0.3, API 15|SDK Platform Android 2.3.3, API 10|Android Support Repository|Android Support Library|Google Repository" | cut -d'-' -f1| sed -e 'H;${x;s/\n/,/g;s/^,//;p;};d' | tr -d ' '`
echo yes | android update sdk -u -a -t $NUMBERS

#   android sdk number. 
#   to see full list, type: 
#     android list sdk --all

#from http://www.cyberciti.biz/faq/bash-comment-out-multiple-line-code/
: ' 
   1- Android SDK Tools, revision 24.0.2
   2- Android SDK Platform-tools, revision 21
   3- Android SDK Build-tools, revision 21.1.2
  10- Android SDK Build-tools, revision 19.1
  20- SDK Platform Android 5.0.1, API 21, revision 2
  26- SDK Platform Android 4.0.3, API 15, revision 5
  31- SDK Platform Android 2.3.3, API 10, revision 2
 112- Android Support Repository, revision 11
 113- Android Support Library, revision 21.0.3
 119- Google Repository, revision 15
'
