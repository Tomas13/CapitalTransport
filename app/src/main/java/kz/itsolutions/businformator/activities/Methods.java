package kz.itsolutions.businformator.activities;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import java.util.Timer;

import kz.itsolutions.businformator.R;

import static kz.itsolutions.businformator.utils.Consts.LOG_TAG;
import static kz.itsolutions.businformator.utils.Consts.urlJsonObj;

/**
 * Created by root on 9/11/17.
 */

public class Methods {

    public static Fragment getCurrentFragment(AppCompatActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        int stackCount = fragmentManager.getBackStackEntryCount();
        if (fragmentManager.getFragments() != null)
            return fragmentManager.getFragments().get(stackCount > 0 ? stackCount - 1 : stackCount);
        else return null;
    }


    public  static void makePostRequest(String token) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(urlJsonObj);

        //Post Data
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        nameValuePair.add(new BasicNameValuePair("token", token));
        nameValuePair.add(new BasicNameValuePair("device", "0"));

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

    public static void  removeFragments(AppCompatActivity activity, Fragment... fragments){
        for (Fragment fr: fragments) {
            activity.getSupportFragmentManager().beginTransaction().remove(fr).commit();
        }
    }


    public static void showNewsFragment(MapGoogleActivity activity, ActionBar mActionBar, DrawerLayout mDrawerLayout,
                                        RelativeLayout mLeftDrawer, RelativeLayout mRightDrawer, Fragment newsFragment) {
        activity.getSupportFragmentManager().popBackStack();
        activity.resetTimersAndClearMap();

        mActionBar.setSubtitle("НОВОСТИ");

        activity.hideMapFragment();

        mDrawerLayout.closeDrawer(mLeftDrawer);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mRightDrawer);

        activity.getSupportFragmentManager().beginTransaction().replace(R.id.container, newsFragment).commit();
    }



    static void stopBusTimer(Timer busTimer){
        if (busTimer != null) {

            busTimer.cancel();
            busTimer.purge();     //29.07
            busTimer = null;
            Log.v(LOG_TAG, "stop BusTimer");
        }
    }



    static void stopBusesTimer(Timer busesTimer) {
        if (busesTimer != null) {
            busesTimer.cancel();
            busesTimer = null;
            Log.v(LOG_TAG, "stop busesTimer");
        }
    }


    static void showToast(Context context, Toast toast, String text) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }


    /*private void drawRouteBusStops(Route route, boolean needClearMap) {
        if (route == null)
            return;
        if (needClearMap)
            mMap.clear();
//        clearAllMapMarkers();
        hashMapMarkerBusStops.clear();
        if (!mSharedPreferences.getBoolean(KEY_SHOW_BUS_STOPS, true))
            return;
        drawBusStops(route);
        if (route.getLinkedRoute() != null) {
            drawBusStops(route.getLinkedRoute());
        }
    }*/

}
