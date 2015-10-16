package kz.itsolutions.businformator.controllers;

import android.text.TextUtils;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kz.itsolutions.businformator.model.Bus;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.utils.Consts;
import kz.itsolutions.businformator.utils.HttpHelper;

public class BusController {

    public static List<Bus> getRouteBuses(Route route) throws HttpException, IOException, JSONException {
        List<Bus> buses = new ArrayList<>();
        HttpHelper httpHelper = new HttpHelper();
        try {
            JSONObject params = new JSONObject();
            params.put("key", RouteController.sGenerateApiKey());
            String response = httpHelper.getJson(Consts.API_SERVER_URL + "action=buses&number=" + route.getNumber(), params);
            try {
                JSONArray jsonArray;
                JSONObject jsonObject;
                if (!TextUtils.isEmpty(response)) {
                    jsonArray = new JSONArray(response);
                    if (jsonArray.length() <= 5) {
                        return getRouteBusesFromInfoBus(httpHelper, route);
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        buses.add(Bus.fromJson(jsonObject));
                    }
                }

            } catch (JSONException e) {
                return getRouteBusesFromInfoBus(httpHelper, route);
            }

        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buses;
    }

    private static List<Bus> getRouteBusesFromInfoBus(HttpHelper httpHelper, Route route) throws JSONException, IOException, HttpException {
        String response = httpHelper.getInfoBusJson(String.format(
                "http://infobus.kz/cities/1/routes/%s/busses", route.getBusReportRouteId()));
        JSONArray jsonArray = new JSONArray(response);
        List<Bus> buses = new ArrayList<>();
        JSONObject jsonObject;
        if (jsonArray.length() > buses.size()) {
            buses.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.getBoolean("offline")) {
                    buses.add(Bus.fromInfoBusJson(route, jsonObject));
                }
            }
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
}
