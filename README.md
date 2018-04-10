
How to test using emulator
==========================

Clone git repository.

> git clone https://github.com/vnesek/installtest.git

Build installtest APK.

./gradlew clean assemble

Start AVD with writable system partition.

> sdk/tools/emulator -writable-system -avd Android_TV_1080p_API_25 -no-snapshot-load -qemu

Install installtest as priviledged APK.

> adb root
> adb remount
> adb push ./app/build/outputs/apk/debug/app-debug.apk /system/priv-app/InstallTest/app-debug.apk
> adb reboot

Launch installtest in debugger or using adb.

> adb shell am start -n net.instantcom.installtest/.InstallActivity
> adb logcat | grep Install

APK will launch with two buttons. You can try installation of sample mybigapk version 1 and upgraded
version 2. APK will be downloaded from internet (~100MB) on first install. Fresh installation and
upgrade should pass ok, downgrade should fail.

Click on MyBigApk 1 button. Check if mybigapk version 1 is installed (see bellow).

Output:

```
04-10 12:15:27.529  3801  3801 D InstallActivity: android.content.pm.extra.STATUS=0
04-10 12:15:27.529  3801  3801 D InstallActivity: android.content.pm.extra.PACKAGE_NAME=net.instantcom.fms.android.mybigapk
04-10 12:15:27.529  3801  3801 D InstallActivity: android.content.pm.extra.SESSION_ID=126286709
04-10 12:15:27.529  3801  3801 D InstallActivity: android.content.pm.extra.LEGACY_STATUS=1
04-10 12:15:27.529  3801  3801 D InstallActivity: android.content.pm.extra.STATUS_MESSAGE=INSTALL_SUCCEEDED
04-10 12:15:27.529  3801  3801 D InstallActivity: session=126286709
```

Click on MyBigApk 2 button. Check if mybigapk version 2 is installed.

Try to downgrade. Click on MyBigApk 1 button:

```
04-10 12:16:32.039  3801  3801 D InstallActivity: android.content.pm.extra.STATUS=4
04-10 12:16:32.040  3801  3801 D InstallActivity: android.content.pm.extra.PACKAGE_NAME=net.instantcom.fms.android.mybigapk
04-10 12:16:32.040  3801  3801 D InstallActivity: android.content.pm.extra.SESSION_ID=1134976704
04-10 12:16:32.040  3801  3801 D InstallActivity: android.content.pm.extra.LEGACY_STATUS=-25
04-10 12:16:32.040  3801  3801 D InstallActivity: android.content.pm.extra.STATUS_MESSAGE=INSTALL_FAILED_VERSION_DOWNGRADE
04-10 12:16:32.040  3801  3801 D InstallActivity: session=1134976704
```

Check if mybigapk is installed
==============================

> adb shell ls -al /data/app

```
drwxr-xr-x  4 system system 4096 2018-04-10 12:04 net.instantcom.fms.android.mybigapk-1
```

Uninstall it.

> adb uninstall net.instantcom.fms.android.mybigapk