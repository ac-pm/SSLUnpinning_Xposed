package mobi.acpm.sslunpinning;

import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.ssl.SSLSocketFactory;

import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

public class Module implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static final String PREFS = "UnpinningPrefs";
    public static XSharedPreferences sPrefs;
    public static final String MY_PACKAGE_NAME = Module.class.getPackage().getName();

    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        sPrefs = new XSharedPreferences(MY_PACKAGE_NAME, PREFS);
        sPrefs.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {

        sPrefs.reload();

        if (!loadPackageParam.packageName.equals(sPrefs.getString("package", "")))
            return;

        if (loadPackageParam.packageName.equals("mobi.acpm.sslunpinning"))
            return;
        // --- Java Secure Socket Extension (JSSE) ---

        //TrustManagerFactory.getTrustManagers >> EmptyTrustManager
        try {
            findAndHookMethod("javax.net.ssl.TrustManagerFactory", loadPackageParam.classLoader, "getTrustManagers", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    TrustManager[] tms = EmptyTrustManager.getInstance();
                    param.setResult(tms);
                }
            });
        } catch (Error e) {
            XposedBridge.log("Unpinning_error: " + e.getMessage());
        }
        //SSLContext.init >> (null,EmptyTrustManager,null)
        try {
            findAndHookMethod("javax.net.ssl.SSLContext", loadPackageParam.classLoader, "init", KeyManager[].class, TrustManager[].class, SecureRandom.class, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = null;
                    param.args[1] = EmptyTrustManager.getInstance();
                    param.args[2] = null;
                }
            });
        } catch (Error e) {
            XposedBridge.log("Unpinning_error: " + e.getMessage());
        }
        //HttpsURLConnection.setSSLSocketFactory >> new SSLSocketFactory
        try {
            findAndHookMethod("javax.net.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setSSLSocketFactory", javax.net.ssl.SSLSocketFactory.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = newInstance(javax.net.ssl.SSLSocketFactory.class);
                }
            });
        } catch (Error e) {
            XposedBridge.log("Unpinning_error: " + e.getMessage());
        }
        // --- APACHE ---

        //HttpsURLConnection.setDefaultHostnameVerifier >> SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
        try {
            findAndHookMethod("org.apache.http.conn.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setDefaultHostnameVerifier",
                    HostnameVerifier.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                        }
                    });
        } catch (Error e) {
            XposedBridge.log("Unpinning_error: " + e.getMessage());
        }
        //HttpsURLConnection.setHostnameVerifier >> SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
        try {
            findAndHookMethod("org.apache.http.conn.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setHostnameVerifier", HostnameVerifier.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.args[0] = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                        }
                    });
        } catch (Error e) {
            XposedBridge.log("Unpinning_error: " + e.getMessage());
        }

        //SSLSocketFactory.getSocketFactory >> new SSLSocketFactory
        try {
            findAndHookMethod("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader, "getSocketFactory", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult((SSLSocketFactory) newInstance(SSLSocketFactory.class));
                }
            });
        } catch (Error e) {
            XposedBridge.log("Unpinning_error: " + e.getMessage());
        }

        //SSLSocketFactory(...) >> SSLSocketFactory(...){ new EmptyTrustManager()}
        try {
            Class<?> sslSocketFactory = findClass("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader);
            findAndHookConstructor(sslSocketFactory, String.class, KeyStore.class, String.class, KeyStore.class,
                    SecureRandom.class, HostNameResolver.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            String algorithm = (String) param.args[0];
                            KeyStore keystore = (KeyStore) param.args[1];
                            String keystorePassword = (String) param.args[2];
                            SecureRandom random = (SecureRandom) param.args[4];

                            KeyManager[] keymanagers = null;
                            TrustManager[] trustmanagers;

                            if (keystore != null) {
                                keymanagers = (KeyManager[]) callStaticMethod(SSLSocketFactory.class, "createKeyManagers", keystore, keystorePassword);
                            }

                            trustmanagers = new TrustManager[]{new EmptyTrustManager()};

                            setObjectField(param.thisObject, "sslcontext", SSLContext.getInstance(algorithm));
                            callMethod(getObjectField(param.thisObject, "sslcontext"), "init", keymanagers, trustmanagers, random);
                            setObjectField(param.thisObject, "socketfactory", callMethod(getObjectField(param.thisObject, "sslcontext"), "getSocketFactory"));
                        }

                    });

        } catch (Error e) {
            XposedBridge.log("Unpinning_error: " + e.getMessage());
        }
        //SSLSocketFactory.isSecure >> true
        try {
            findAndHookMethod("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader, "isSecure", Socket.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
        } catch (Error e) {
            XposedBridge.log("Unpinning_error: " + e.getMessage());
        }

        ///OKHTTP
        try {
            findAndHookMethod("okhttp3.CertificatePinner", loadPackageParam.classLoader, "findMatchingPins", String.class, new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.args[0] = "";
                }
            });
        } catch (Error e) {
            XposedBridge.log("Unpinning_error: " + e.getMessage());
        }
    }
}
