package http;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

class ReloadableX509TrustManager implements X509TrustManager {

  private final URL trustStorePath;
  private final char[] password;
  private X509TrustManager trustManager;
  private List<X509Certificate> tempCertList = new ArrayList<X509Certificate>();

  public ReloadableX509TrustManager(URL tspath) throws Exception {
      this(tspath, null);
  }
  
  public ReloadableX509TrustManager(URL tspath, String password) throws Exception {
    this.trustStorePath = tspath;
    this.password = password.toCharArray();
    reloadTrustManager();
  }

  public X509TrustManager getTrustManager() {
    return trustManager;
  }

  private void setTrustManager(X509TrustManager trustManager) {
    this.trustManager = trustManager;
  }
  
  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        getTrustManager().checkClientTrusted(chain, authType);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    try {
            getTrustManager().checkServerTrusted(chain, authType);
    }
    catch (CertificateException cx) {
      addServerCertAndReload(chain[0]);
            getTrustManager().checkServerTrusted(chain, authType);
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    X509Certificate[] issuers = getTrustManager().getAcceptedIssuers();
    return issuers;
  }

  private void reloadTrustManager() throws Exception {
    // load keystore from specified cert store (or default)
    KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
    InputStream in = trustStorePath.openStream();
    try {
      ts.load(in, password);
    }
    finally {
      in.close();
    }
    // add all temporary certs to KeyStore (ts)
    for (X509Certificate cert : tempCertList) {
      ts.setCertificateEntry(Double.toString(Math.random()), cert);
    }
    // initialize a new TMF with the ts we just loaded
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ts);
    // acquire X509 trust manager from factory
    TrustManager tms[] = tmf.getTrustManagers();
    for (int i = 0; i < tms.length; i++) {
      if (tms[i] instanceof X509TrustManager) {
                setTrustManager((X509TrustManager)tms[i]);
        return;
      }
    }
    throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
  }

  private void addServerCertAndReload(X509Certificate cert) {
    try {
      tempCertList.add(cert);
      reloadTrustManager();
    }
    catch (Exception ex) {
        ;
    }
  }

}

//http://jcalcote.wordpress.com/2010/06/22/managing-a-dynamic-java-trust-store/
//now you don't have to use: java -Djavax.net.ssl.trustStore=mycacert -jar HttpTest.jar