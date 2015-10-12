<img src="http://i.imgur.com/eYoj81B.png" width="80" /> SSLUnpinning - Xposed Module
========

Android Xposed Module to bypass SSL certificate validation (Certificate Pinning).

Description
-----------

If you need intercept the traffic from one app who use certificate pinning, with a tool like Burp Proxy, the SSLUnpinning help you with this hard work! 
The SSLUnpinning through Xposed Framework, make severous hooks in SSL classes to bypass the certificate verifications for one specific app, then you can intercept all your traffic.

Usage
---------------

* install Xposed in your device (root access on Android 4.0.3 or later);
http://repo.xposed.info/module/de.robv.android.xposed.installer
* Download the APK available here https://github.com/ac-pm/SSLUnpinning_Xposed or clone the project and compile;
* Install SSLUnpinning_XposedMod.apk on a device with Xposed:

        adb install SSLUnpinning_XposedMod.apk
 
* SSLUnpinning will list the applications to choose from which will be unpinned;
* Ok! Now you can intercept all traffic from the chosen app.

Download
---------------
Get it from Xposed repo: http://repo.xposed.info/module/mobi.acpm.sslunpinning

### How to uninstall

        adb uninstall SSLUnpinning_XposedMod.apk
        
Screenshots

<img src="http://i.imgur.com/zL9skG3.png" width="200" />
<img src="http://i.imgur.com/saXV9Oc.png" width="200" />


License
-------

See ./LICENSE.

Author
-------

ACPM



