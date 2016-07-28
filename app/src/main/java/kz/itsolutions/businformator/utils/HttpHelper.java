package kz.itsolutions.businformator.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;


public class HttpHelper {

    public String getJson(String url, JSONObject jsonObj) throws HttpException, IOException {

        StringBuilder builder = new StringBuilder();

        try {
            HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

            DefaultHttpClient client = new DefaultHttpClient();

            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("http", new PlainSocketFactory(), 80));
            registry.register(new Scheme("https", socketFactory, 443));
            SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
            DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());

            // Set verifier
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

            HttpPost httpPost = new HttpPost(url);

            StringEntity ent = new StringEntity(jsonObj.toString(), HTTP.UTF_8);
            ent.setContentType("application/json");

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();

            try {
                nameValuePairs.add(new BasicNameValuePair("key", jsonObj.getString("key")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httpPost);

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                //Log.e("MyTag",
                //      "Failed to get url("+url+") as JSON");
            }
        } catch (ClientProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
        return builder.toString();
    }

    public String getInfoBusJson(String url) throws HttpException, IOException {

        StringBuilder builder = new StringBuilder();

        try {
            HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

            DefaultHttpClient client = new DefaultHttpClient();

            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme("http", new PlainSocketFactory(), 80));
            registry.register(new Scheme("https", socketFactory, 443));
            SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
            DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());

            // Set verifier
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

            HttpGet httpGet = new HttpGet(url);

            HttpResponse response = httpClient.execute(httpGet);

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
        } catch (ClientProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }

        return builder.toString();
    }


}
