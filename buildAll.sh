# Bash

../gradlew clean debug
if [ $? -eq 0 ]
 then
  adb uninstall org.ros.android.android_sensors_driver
  adb install bin/MainActivity-debug.apk
  adb shell am start -n org.ros.android.android_sensors_driver/org.ros.android.android_sensors_driver.MainActivity
  adb logcat -c
  adb logcat | grep CICCIO
fi
