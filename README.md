*Attention:* I'm working in a new suite of tools that include SSLUnpinning feature and many, many others! Look here -> https://github.com/ac-pm/Inspeckage


<img src="http://i.imgur.com/eYoj81B.png" width="80" /> SSLUnpinning - Xposed Module
========

Android Xposed Module to bypass SSL certificate validation (Certificate Pinning).

Description
-----------

If you need to intercept the traffic from an app which uses certificate pinning, with a tool like Burp Proxy, the SSLUnpinning will help you with this hard work!
The SSLUnpinning through Xposed Framework, makes several hooks in SSL classes to bypass the certificate verifications for one specific app, then you can intercept all your traffic.

API
-----------
Java Secure Socket Extension (JSSE)
- javax.net.ssl.*

APACHE
- org.apache.http.conn.ssl.*

OKHTTP
- okhttp3.*

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

        adb uninstall mobi.acpm.sslunpinning
        
Screenshots

<img src="http://i.imgur.com/zL9skG3.png" width="200" />
<img src="http://i.imgur.com/saXV9Oc.png" width="200" />


License
-------

See ./LICENSE.

Author
-------

ACPM



