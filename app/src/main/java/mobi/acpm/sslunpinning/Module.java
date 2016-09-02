package mobi.acpm.sslunpinning;

import org.apache.http.conn.scheme.HostNameResolver;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.scheme.Scheme;

import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
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

public class Module implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {

        String packageName = ConfigUtil.readFromFile();

        if (!loadPackageParam.packageName.equals(packageName))
            return;

        XposedBridge.log("Xposed SSLUnpinning: " + packageName);

        // --- Java Secure Socket Extension (JSSE) ---

        //TrustManagerFactory.getTrustManagers >> EmptyTrustManager
        findAndHookMethod("javax.net.ssl.TrustManagerFactory", loadPackageParam.classLoader, "getTrustManagers", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                TrustManager[] tms = EmptyTrustManager.getInstance();
                param.setResult(tms);
            }
        });

        //SSLContext.init >> (null,EmptyTrustManager,null)
        findAndHookMethod("javax.net.ssl.SSLContext", loadPackageParam.classLoader, "init", KeyManager[].class, TrustManager[].class, SecureRandom.class, new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = null;
                param.args[1] = EmptyTrustManager.getInstance();
                param.args[2] = null;
            }
        });

        //HttpsURLConnection.setSSLSocketFactory >> new SSLSocketFactory
        findAndHookMethod("javax.net.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setSSLSocketFactory", javax.net.ssl.SSLSocketFactory.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = newInstance(javax.net.ssl.SSLSocketFactory.class);
            }
        });

        // --- APACHE ---

        //SchemeRegistry.register >> new Scheme
        findAndHookMethod("org.apache.http.conn.scheme.SchemeRegistry", loadPackageParam.classLoader, "register", org.apache.http.conn.scheme.Scheme.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Scheme scheme = (Scheme)param.args[0];
                if (scheme.getName() =="https") {
                    param.args[0] = new Scheme("https", SSLSocketFactory.getSocketFactory(), 443);
                }
            }
        });

        //HttpsURLConnection.setDefaultHostnameVerifier >> SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
        findAndHookMethod("org.apache.http.conn.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setDefaultHostnameVerifier",
                HostnameVerifier.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                    }
                });

        //HttpsURLConnection.setHostnameVerifier >> SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
        findAndHookMethod("org.apache.http.conn.ssl.HttpsURLConnection", loadPackageParam.classLoader, "setHostnameVerifier", HostnameVerifier.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                    }
                });

        //SSLSocketFactory.getSocketFactory >> new SSLSocketFactory
        findAndHookMethod("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader, "getSocketFactory", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult((SSLSocketFactory) newInstance(SSLSocketFactory.class));
            }
        });

        /**
         * org.apache.http.conn.ssl.SSLSocketFactory(...){
         195        if (algorithm == null) {
         196            algorithm = TLS;
         197        }
         198        KeyManager[] keymanagers = null;
         199        if (keystore != null) {
         200            keymanagers = createKeyManagers(keystore, keystorePassword);
         201        }
         202        TrustManager[] trustmanagers = null;
         203        if (truststore != null) {
         204            trustmanagers = createTrustManagers(truststore);
         205        }
         206        this.sslcontext = SSLContext.getInstance(algorithm);
         207        this.sslcontext.init(keymanagers, trustmanagers, random);
         208        this.socketfactory = this.sslcontext.getSocketFactory();
         209        this.nameResolver = nameResolver;
                    }
         **/
        //SSLSocketFactory(...) >> SSLSocketFactory(...){ new EmptyTrustManager()}
        Class<?> sslSocketFactory = findClass("org.apache.http.conn.ssl.SSLSocketFactory",loadPackageParam.classLoader);
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



        //SSLSocketFactory.isSecure >> true
        findAndHookMethod("org.apache.http.conn.ssl.SSLSocketFactory", loadPackageParam.classLoader, "isSecure", Socket.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });
    }
}
