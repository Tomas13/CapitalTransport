package kz.itsolutions.businformator.controllers;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import kz.itsolutions.businformator.db.BusStopsTable;
import kz.itsolutions.businformator.db.DBHelper;
import kz.itsolutions.businformator.model.BusStop;
import kz.itsolutions.businformator.model.Forecast;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.utils.AssetFile;
import kz.itsolutions.businformator.utils.Consts;
import kz.itsolutions.businformator.utils.HttpHelper;
import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static kz.itsolutions.businformator.controllers.RouteController.sGenerateApiKey;

public class BusStopController {
    // поиск остановок в радиусе, расстояние в метрах
    public static final int BUS_STOP_RADIUS = 1000;

    private static final String LOG_TAG = BusStopController.class.getSimpleName();

    Context mContext;
    BusStopsTable rDb;
    RouteController mRouteController;

    public BusStopController(Context context) {
        this.mContext = context;
        rDb = new BusStopsTable(context);
        mRouteController = new RouteController(mContext);
    }

    public ArrayList<BusStop> getRouteBusStops(int routeServerId) {
        rDb.openRead();
        ArrayList<BusStop> busStops = new ArrayList<>();
        Cursor cursor = rDb.getRouteBusStops(routeServerId);
        while (!cursor.isAfterLast()) {
            busStops.add(new BusStop(cursor.getLong(0), cursor.getInt(1), cursor
                    .getString(2), cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5)));
            cursor.moveToNext();
        }
        cursor.close();
        rDb.close();
        return busStops;
    }

    public ArrayList<BusStop> getBusStops() {
        rDb.openRead();
        ArrayList<BusStop> busStops = new ArrayList<>();
        Cursor cursor = rDb.getBusStops();
        while (!cursor.isAfterLast()) {
            busStops.add(new BusStop(cursor.getLong(0), cursor.getInt(1), cursor
                    .getString(2), cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5)));
            cursor.moveToNext();
        }
        cursor.close();
        rDb.close();
        return busStops;
    }

    public BusStop getBusStop(int busStopServerId) {
        rDb.openRead();
        BusStop busStop = null;
        Cursor cursor = rDb.getBusStop(busStopServerId);
        if (!cursor.isAfterLast()) {
            busStop = new BusStop(cursor.getLong(0), cursor.getInt(1), cursor
                    .getString(2), cursor.getDouble(3), cursor.getDouble(4), cursor.getString(5));
        }
        cursor.close();
        rDb.close();
        return busStop;
    }

    public ArrayList<BusStop> getNearestBusStops(double lat, double lon) {
        return getNearestBusStops(lat, lon, BUS_STOP_RADIUS);
    }

    public ArrayList<BusStop> getNearestBusStops(double lat, double lon, int radius) {
        if (lat == 0.0 && lon == 0.0) {
            return null;
        }
        double distance;
        ArrayList<BusStop> nearestBusStops = new ArrayList<>();
        ArrayList<BusStop> allBusStops = getBusStops();
        for (BusStop busStop : allBusStops) {
            distance = calcDistance(lat, lon,
                    busStop.getPointGoogle().latitude, busStop.getPointGoogle().longitude);
            if (distance < radius) {
                nearestBusStops.add(busStop);
            }
        }
        return nearestBusStops;
    }

    public static double calcDistance(double lat1, double lon1, double lat2, double lon2) {
        // haversine great circle distance approximation, returns meters
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60; // 60 nautical miles per degree of seperation
        dist = dist * 1852; // 1852 meters per nautical mile
        return dist;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public void loadBusStopsFromServer() throws HttpException, IOException, JSONException {
        HttpHelper httpHelper = new HttpHelper();
        String response;
        try {
            rDb.open();
            if (rDb.hasBusStops()) {
                rDb.close();
                return;
            }
            JSONObject params = new JSONObject();
            params.put("key", sGenerateApiKey());
            Log.i(LOG_TAG, "start download bus stops");
            response = httpHelper.getJson(Consts.API_SERVER_URL + "action=zones", params);
            Log.i(LOG_TAG, "end download bus stops");
            try {
                insertBusStopsToDb(response);
            } catch (JSONException e) {
                throw e;
            }
        } catch (HttpException e) {
            throw e;
        } catch (UnknownHostException e) {
            // Нет интернета
            throw e;
        } finally {
            rDb.close();
        }
    }

    public void loadBusStopsFromAssets() throws JSONException {
        rDb.open();
        if (rDb.hasBusStops()) {
            rDb.close();
            return;
        }
        String data = AssetFile.readFromFile("bus_stops.json", mContext);
        if (!TextUtils.isEmpty(data)) {
            insertBusStopsToDb(data);
        }
        rDb.close();
    }

    private void insertBusStopsToDb(String data) throws JSONException {
        try {
            JSONArray jsonArray = new JSONArray(data);
            Log.v(LOG_TAG, "start insert BusStops");
            rDb.insertBusStops(jsonArray);
            Log.v(LOG_TAG, "end insert BusStops");
        } catch (JSONException e) {
            throw e;
        }
    }

    //Возвращает прогноз по остановке
    public ArrayList<Route> getForecastForBusStop(BusStop busStop) throws HttpException, IOException, JSONException {
        ArrayList<Route> forecasts = new ArrayList<>();
        HttpHelper httpHelper = new HttpHelper();
        try {
            String url = Consts.API_SERVER_URL + "action=forecast&busstopid=" + busStop.getServerId();
            String response;
            JSONObject params = new JSONObject();
            params.put("key", sGenerateApiKey());
            try {
                response = httpHelper.getJson(url, params);
            } catch (IOException e) {
                throw e;
            }
            try {
                JSONArray jsonarray = new JSONArray(response);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonObject = jsonarray.getJSONObject(i);
                    Route route = Route.getByServerId(DBHelper.getHelper(), jsonObject.getInt(Route.FIELD_SERVER_ID));
                    Forecast forecast = new Forecast(route, jsonObject.getInt("min_bus_id"),
                            jsonObject.getInt("min_distance"), jsonObject.getInt("min_f_minutes"),
                            jsonObject.getInt("next_bus_id"), jsonObject.getInt("next_min_distance"),
                            jsonObject.getInt("next_min_f_minutes"));
                    //if (route == null) continue;
                    route.setForecast(forecast);
                    forecasts.add(route);
                }
                //отсортируем по номеру маршрута
                Comparator<Route> forecastComparator = new Comparator<Route>() {
                    @Override
                    public int compare(Route forecast, Route forecast2) {
                        return forecast.getNumber() - forecast2.getNumber();
                    }
                };
                Collections.sort(forecasts, forecastComparator);
            } catch (JSONException e) {
                throw e;
            }
        } catch (HttpException e) {
            throw e;
        }
        return forecasts;
    }
}
