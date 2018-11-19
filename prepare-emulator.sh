#!/bin/bash
# AutoArkaive emulator preparation script
# Start the emulator and run this script, then shut it down and you should be ready to go.
# Make sure no other Android devices or emulators are attached to the system when the script is run.

echo Installing APKs...
adb install -r APKs/AutoArkaive.apk
adb install -r APKs/Arkaive.apk

echo Granting accessibility and mock location permissions to AutoArkaive...
# from here: https://stackoverflow.com/questions/46899547/granting-accessibility-service-permission-for-debug-purposes
adb shell settings put secure enabled_accessibility_services "%accessibility:com.autoarkaive.autoarkaive/com.autoarkaive.autoarkaive.AAService";
adb shell appops set com.autoarkaive.autoarkaive 58 allow

echo Granting location permissions to Arkaive...
adb shell pm grant com.arkaive.arkaive android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.autoarkaive.autoarkaive android.permission.ACCESS_FINE_LOCATION

echo 'Done! Now, shutdown the emulator and start the server!'