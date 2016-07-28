package kz.itsolutions.businformator.controllers;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import kz.itsolutions.businformator.model.Bus;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.utils.Consts;
import kz.itsolutions.businformator.utils.HttpHelper;

public class BusController {



    //method when one route is chosen
    public static List<Bus> getRouteBusesDaniyar(Route route) throws HttpException, IOException, JSONException{
        ArrayList<Bus> buses = new ArrayList<>();
        HttpHelper httpHelper = new HttpHelper();

        try {
            String response = httpHelper.getInfoBusJson(Consts.BUS_POSITIONS_URL_NEW + "/" + route.getNumber());


            Log.d("astanaBusController", "response is " + response);

            JSONObject jsonObject = new JSONObject(response);

            long myMonkeyId = 0;

            for (Iterator<String> iter = jsonObject.keys(); iter.hasNext();) {
                String key = iter.next();

                JSONObject busJson = jsonObject.getJSONObject(key);

                String latitudeStr = busJson.getString("latitude");
                double latitude = Double.parseDouble(latitudeStr);

                String longitudeStr = busJson.getString("longitude");
                double longitude = Double.parseDouble(longitudeStr);

                String angleStr = busJson.getString("angle");
                int angle = Integer.valueOf(angleStr);

                String timeStr = busJson.getString("time");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date time = format.parse(timeStr);
                //Log.d("TIME TIME", time.toString());
                Bus bus = new Bus(0, route.getNumber(), (int) route.getId(), "", /*Long.parseLong(key)*/ myMonkeyId, time.getTime(), 0.0, latitude, longitude, angle);

                myMonkeyId++;

                buses.add(bus);
            }


        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return buses;
    }



    @Deprecated
    public static ArrayList<Bus> getBusInfoByRouteNumber(int number) throws HttpException, IOException, JSONException {
        ArrayList<Bus> buses = new ArrayList<>();
        HttpHelper _httpHelper = new HttpHelper();
        try {
            JSONObject params = new JSONObject();
            params.put("key", RouteController.sGenerateApiKey());

            String response = _httpHelper.getJson(Consts.API_SERVER_URL + "action=buses&number=" + number, params);

            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    buses.add(Bus.fromJson(jsonObject));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buses;
    }

    public static ArrayList<Bus> getBusInfoForRoutes(List<Route> routes) throws HttpException, IOException, JSONException {
        if (routes == null || routes.size() == 0)
            return null;
        ArrayList<Bus> buses = new ArrayList<>();
        HttpHelper _httpHelper = new HttpHelper();
        try {
            JSONObject params = new JSONObject();
            params.put("key", RouteController.sGenerateApiKey());
            StringBuilder sb = new StringBuilder();
            for (Route route : routes) {
                sb.append("&numbers[]=").append(route.getNumber());
            }
            String response = _httpHelper.getJson(Consts.API_SERVER_URL + "action=buses" + sb.toString(), params);

            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    buses.add(Bus.fromJson(jsonObject));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buses;
    }



    //getting a info on list of buses, when we choose a several routes
    public static List<Bus> getSeveralRouteBuses(List<Route> route) throws HttpException, IOException, JSONException{
        ArrayList<Bus> buses = new ArrayList<>();
        HttpHelper httpHelper = new HttpHelper();

        try {
            String response = httpHelper.getInfoBusJson(Consts.BUS_POSITIONS_URL_NEW);
            JSONObject jsonObject = new JSONObject(response);

            long myMonkeyId = 0;

            for (Iterator<String> iter = jsonObject.keys(); iter.hasNext();) {
                String key = iter.next();

                JSONObject busJson = jsonObject.getJSONObject(key);

                String routeNumberStr = busJson.getString("route");

                int routeNumber = -1;

                try {
                    routeNumberStr = routeNumberStr.replace("\"","");
                    routeNumber = Integer.parseInt(routeNumberStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    continue;
                }

                for(Route route2 : route){
                    if (route2 != null && routeNumber == route2.getNumber()) {
                        String latitudeStr = busJson.getString("latitude");
                        double latitude = Double.parseDouble(latitudeStr);

                        String longitudeStr = busJson.getString("longitude");
                        double longitude = Double.parseDouble(longitudeStr);

                        String angleStr = busJson.getString("angle");
                        int angle = Integer.valueOf(angleStr);

                        String timeStr = busJson.getString("time");
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Date time = format.parse(timeStr);
                        //Log.d("TIME TIME", time.toString());
                        Bus bus = null;

                        bus = new Bus(0, routeNumber, (int) route2.getId(), "", /*Long.parseLong(key)*/ myMonkeyId, time.getTime(), 0.0, latitude, longitude, angle);

                        myMonkeyId++;

                        buses.add(bus);
                    }

                }

            }

        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return buses;
    }
}
