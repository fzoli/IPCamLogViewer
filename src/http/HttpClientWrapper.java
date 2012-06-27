package http;

import java.io.IOException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

class HttpClientWrapper {

    private static X509HostnameVerifier hv = new X509HostnameVerifier() {

        @Override
        public void verify(String string, SSLSocket ssls) throws IOException {
            ;
        }

        @Override
        public void verify(String string, X509Certificate xc) throws SSLException {
            ;
        }

        @Override
        public void verify(String string, String[] strings, String[] strings1) throws SSLException {
            ;
        }

        @Override
        public boolean verify(String string, SSLSession ssls) {
            return true;
        }
    };
    
    private static X509TrustManager tm = new X509TrustManager() {
        
        @Override
	public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            ;
        }
        
        @Override
	public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            ;
	}

        @Override
	public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
	}
        
    };

    public static void wrapHttpClient(HttpClient httpClient) {
        try {
            wrapHttpClient(httpClient, null);
        }
        catch(Exception ex) {
            ;
        }
    }

    public static void wrapHttpClient(HttpClient httpClient, URL cacertPath) throws Exception {
        wrapHttpClient(httpClient, cacertPath, null);
    }
    
    public static void wrapHttpClient(HttpClient httpClient, URL cacertPath, String cacertPassword) throws Exception {
        wrapHttpClient(httpClient, false, cacertPath, cacertPassword);
    }
    
    private static void wrapHttpsUrlConnection(SSLContext context, boolean verifyHostname) {
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        if (!verifyHostname) HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }
    
    public static void wrapHttpClient(HttpClient httpClient, boolean verifyHostname, URL cacertPath, String cacertPassword) throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLS");
        TrustManager[] trustmanagers;
        if (cacertPath == null) {
            trustmanagers = new TrustManager[]{tm};
        }
        else {
            trustmanagers = new TrustManager[] {new StrictReloadableX509TrustManager(cacertPath, cacertPassword)};
        }
        ctx.init(null, trustmanagers, null);
        wrapHttpsUrlConnection(ctx, verifyHostname);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx);
        if (!verifyHostname) {
            ssf.setHostnameVerifier(hv);
        }
        ClientConnectionManager ccm = httpClient.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", ssf, 443));
    }
    
}