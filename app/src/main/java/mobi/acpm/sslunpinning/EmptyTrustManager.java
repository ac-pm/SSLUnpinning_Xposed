package mobi.acpm.sslunpinning;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by acpm on 04/10/15.
 */
public class EmptyTrustManager implements X509TrustManager {

    private static TrustManager[] emptyTM = null;

    public static TrustManager[] getInstance(){
        if (emptyTM == null) {
            emptyTM = new TrustManager[1];
            emptyTM[0] = new EmptyTrustManager();
        }
        return emptyTM;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
