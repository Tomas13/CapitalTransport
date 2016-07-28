package kz.itsolutions.businformator.push;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jean on 7/4/2016.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private String urlJsonObj = "http://crm.astanalrt.com/notifications/savedevice";

    private String oldtoken;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
//        String refreshedToken = FirebaseInstanceId.getInstance().getToken();


        SharedPreferences sp = getSharedPreferences("push", Activity.MODE_PRIVATE);
        if (sp.contains("fcm_token")){
            oldtoken = sp.getString("fcm_token", "");
            makePostRequestOnNewThread();
        }

    }

    private void makePostRequestOnNewThread() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                makePostRequest(FirebaseInstanceId.getInstance().getToken());
            }
        });
        t.start();
    }


    private void makePostRequest(String token) {

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(urlJsonObj);

        //Post Data
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        nameValuePair.add(new BasicNameValuePair("token", token));
        nameValuePair.add(new BasicNameValuePair("oldtoken", oldtoken));

        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // log exception
            e.printStackTrace();
        }

        //making POST request.
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
//            Log.d("Http Post Response:", response.toString());
        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }

    }
}