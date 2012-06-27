package http;

import java.io.*;
import java.util.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import resources.ResourceUtils;

public class HttpExecutor {

    private int status;
    private String url, encode;
    private final HttpClient HTTP_CLIENT;
    private UsernamePasswordCredentials creds = null;
    
    public HttpExecutor(String url) {
        this(url, true, false, null, null);
    }
    
    public HttpExecutor(String url, String encode) {
        this(url, encode, true, false, null, null);
    }
    
    public HttpExecutor(String url, String usr, String passwd) {
        this(url, true, true, usr, passwd);
    }
    
    public HttpExecutor(String url, String encode, String usr, String passwd) {
        this(url, encode, true, true, usr, passwd);
    }
    
    public HttpExecutor(String url, boolean validCert) {
        this(url, validCert, false, null, null);
    }
    
    public HttpExecutor(String url, String encode, boolean validCert) {
        this(url, encode, validCert, false, null, null);
    }
    
    public HttpExecutor(String url, boolean validCert, String usr, String passwd) {
        this(url, validCert, true, usr, passwd);
    }
    
    public HttpExecutor(String url, String encode, boolean validCert, String usr, String passwd) {
        this(url, encode, validCert, true, usr, passwd);
    }
    
    private HttpExecutor(String url, boolean validCert, boolean withCreds, String usr, String passwd) {
        this(url, "utf-8", validCert, withCreds, usr, passwd);
    }
    
    private HttpExecutor(String url, String encode, boolean validCert, boolean withCreds, String usr, String passwd) {
        this.url = url;
        this.encode = encode;
        HTTP_CLIENT = getThreadSafeClient();
        HTTP_CLIENT.getParams().setParameter("http.connection.timeout", 5000);
        if (!validCert) {
            try {
                HttpClientWrapper.wrapHttpClient(HTTP_CLIENT, false, ResourceUtils.class.getResource("cacerts"), "ParasztAkiEztOlvassa"); // viszont, aki ezt is olvassa, az nem paraszt
            }
            catch(Exception ex) {
                System.err.println(ex.getMessage());
                System.err.println("HTTPS cert check disabled.");
                HttpClientWrapper.wrapHttpClient(HTTP_CLIENT);
            }
        }
        if (withCreds) creds = new UsernamePasswordCredentials(usr, passwd);
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setEncode(String encode) {
        this.encode = encode;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setUsrAndPswd(String usr, String passwd) {
        creds = new UsernamePasswordCredentials(usr, passwd);
    }
    
    public InputStream getResponseStream(Map<String, String> map) {
        return execute(map);
    }
    
    public InputStream getResponseStream() {
        return execute(new HashMap<String, String>());
    }
    
    public String getResponse() {
        return getResponse(new HashMap<String, String>());
    }
    
    public String getResponse(Map<String, String> map) {
        InputStream stream = execute(map);
        DataInputStream dis = new DataInputStream(stream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte data;
        try {
            while ((data = dis.readByte()) != -1) {
                baos.write(data);
            }
        } catch(Exception ex) {}
        byte[] response = baos.toByteArray();
        try {
            return new String(response, encode);
        }
        catch(UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    private DefaultHttpClient getThreadSafeClient() {
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager();
        cm.setDefaultMaxPerRoute(15);
        cm.setMaxTotal(15);
        return new DefaultHttpClient(cm);
    }
    
    private UrlEncodedFormEntity createFormEntity(Map<String, String> map) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        Set<String> keys = map.keySet();
        for (String k : keys) {
            nameValuePairs.add(new BasicNameValuePair(k, map.get(k)));
        }
        try {
            return new UrlEncodedFormEntity(nameValuePairs, encode);
        }
        catch (UnsupportedEncodingException ex) {
            //ex.printStackTrace();
            return null;
        }
    }
    
    private InputStream getResponseStream(HttpResponse httpResponse) {
        status = httpResponse.getStatusLine().getStatusCode();
        HttpEntity httpEntity = httpResponse.getEntity();
        try {
            return httpEntity.getContent();
        }
        catch(IOException ex) {
            //ex.printStackTrace();
            return null;
        }
    }
    
    private InputStream execute(Map<String, String> map) {
        UrlEncodedFormEntity entity = createFormEntity(map);
        HttpPost post = new HttpPost(url);
        post.setEntity(entity);
        try {        
            if (creds != null) post.addHeader(new BasicScheme().authenticate(creds, post));
            HttpResponse response = HTTP_CLIENT.execute(post);
            return getResponseStream(response);
        } catch (AuthenticationException ex) {
            //ex.printStackTrace();
        }
        catch(IOException ex) {
            //ex.printStackTrace();
        }
        return null;
    }
    
}