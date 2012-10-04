# Bash

../gradlew clean debug
if [ $? -eq 0 ]
 then
  adb uninstall org.ros.android.android_sensor_driver
  adb install bin/MainActivity-debug.apk
  adb shell am start -n org.ros.android.android_sensor_driver/org.ros.android.android_sensor_driver.MainActivity
  adb logcat -c
  adb logcat | grep CICCIO
fi
