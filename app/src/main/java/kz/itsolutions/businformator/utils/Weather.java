package kz.itsolutions.businformator.utils;

import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by arna on 10.12.14.
 */
public class Weather {

    public interface WeatherInterface {
        public void setTemperature(int value);
    }

    private static final String URL = "http://api.openweathermap.org/data/2.5/weather?id=1526273&units=metric&APPID=a083d974ea00633edeb77382aad4b415";

    public static void fetchData(final WeatherInterface weatherInterface) {
        AstanaBusRestClient.getWithFullUrl(URL, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                if (TextUtils.isEmpty(response))
                    return;
                try {
                    JSONObject object = new JSONObject(response);
                    JSONObject main = object.getJSONObject("main");
                    if (weatherInterface != null) {
                        weatherInterface.setTemperature(main.getInt("temp"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }


}
