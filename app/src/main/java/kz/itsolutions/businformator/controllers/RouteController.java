package kz.itsolutions.businformator.controllers;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import kz.itsolutions.businformator.db.RoutesTable;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.model.RouteStatistic;
import kz.itsolutions.businformator.utils.AssetFile;
import kz.itsolutions.businformator.utils.Consts;
import kz.itsolutions.businformator.utils.HttpHelper;
import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

public class RouteController {

    Context mContext;
    RoutesTable rDb;
    private static final String LOG_TAG = "astana_bus_db";

    public enum State {
        Updated, AlreadyExist, NoData
    }

    public RouteController(Context context) {
        this.mContext = context;
        rDb = new RoutesTable(context);
    }

    public ArrayList<Route> getRoutes() {
        rDb.openRead();
        ArrayList<Route> routes = new ArrayList<>();
        Cursor cursor = rDb.getAllRoutes();
        while (!cursor.isAfterLast()) {
            routes.add(fromCursor(cursor));
        }
        cursor.close();
        rDb.close();
        return routes;
    }

    private Route fromCursor(Cursor cursor) {
        Route route = new Route(cursor.getLong(0), cursor.getInt(1), cursor.getString(10), cursor.getString(11), cursor
                .getString(2), cursor.getString(3), cursor.getString(4),
                cursor.getString(5), cursor.getInt(6), null, null, cursor.getInt(8) > 0);
        cursor.moveToNext();
        if (route.getNumber() == cursor.getInt(6)) {
            route.linkedRoute = new Route(cursor.getLong(0), cursor.getInt(1), cursor.getString(10), cursor.getString(11), cursor
                    .getString(2), cursor.getString(3), cursor.getString(4),
                    cursor.getString(5), cursor.getInt(6), null, null, cursor.getInt(8) > 0);
            cursor.moveToNext();
        }
        return route;
    }

    public ArrayList<Route> getRoutes(String value) {
        rDb.openRead();
        ArrayList<Route> alFlats = new ArrayList<>();
        Cursor cursor = rDb.getRoutes(value);
        while (!cursor.isAfterLast()) {
            alFlats.add(new Route(cursor.getLong(0), cursor.getInt(1), cursor.getString(10), cursor.getString(11), cursor
                    .getString(2), cursor.getString(3), cursor.getString(4),
                    cursor.getString(5), cursor.getInt(6), null, null, cursor.getInt(9) > 0));
            cursor.moveToNext();
        }
        cursor.close();
        rDb.close();
        return alFlats;
    }

    public ArrayList<Route> getRoutes(int busStopId) {
        rDb.openRead();
        ArrayList<Route> routes = new ArrayList<>();
        Cursor cursor = rDb.getBusStopRoutes(busStopId);
        while (!cursor.isAfterLast()) {
            routes.add(new Route(cursor.getLong(0), cursor.getInt(1), cursor.getString(10), cursor.getString(11), cursor
                    .getString(2), cursor.getString(3), cursor.getString(4),
                    cursor.getString(5), cursor.getInt(6), null, null, cursor.getInt(9) > 0));
            cursor.moveToNext();
        }
        cursor.close();
        rDb.close();
        return routes;
    }

    public Route getRoute(int serverId) {
        rDb.openRead();
        Route route = null;
        Cursor cursor = rDb.getRoute(serverId);
        if (!cursor.isAfterLast()) {
            route = new Route(cursor.getLong(0), cursor.getInt(1), cursor.getString(10), cursor.getString(11), cursor
                    .getString(2), cursor.getString(3), cursor.getString(4),
                    cursor.getString(5), cursor.getInt(6), null, null, cursor.getInt(9) > 0);
        }
        cursor.close();
        rDb.close();
        return route;
    }

    public State loadRoutesFromServer() throws HttpException, IOException, JSONException {
        HttpHelper httpHelper = new HttpHelper();
        String response;
        try {
            rDb.open();
            if (rDb.hasRoutes()) {
                rDb.close();
                return State.AlreadyExist;
            }
            JSONObject params = new JSONObject();
            params.put("key", sGenerateApiKey());
            Log.i(LOG_TAG, "start download routes");
            response = httpHelper.getJson(Consts.API_SERVER_URL + "action=routes&d=0&t=1", params);
            Log.i(LOG_TAG, "end download routes");
            insertRoutesToDb(response);
        } finally {
            rDb.close();
        }
        return State.Updated;
    }

    public State loadRoutesFromAssets() throws JSONException {
        rDb.openRead();
        if (rDb.hasRoutes()) {
            rDb.close();
            return State.AlreadyExist;
        }
        rDb.close();
        String data = AssetFile.readFromFile("routes.json", mContext);
        if (!TextUtils.isEmpty(data)) {
            Log.v(LOG_TAG, "start insert routes");
            rDb.open();
            insertRoutesToDb(data);
            rDb.close();
            Log.v(LOG_TAG, "end insert routes");
            return State.Updated;
        }
        return State.NoData;
    }

    private void insertRoutesToDb(String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        rDb.insertRoutes(jsonArray);
    }

    public Route updateIsFavorite(Route route) {
        rDb.open();
        rDb.updateIsFavoriteByNumber(route.getNumber(), route.isFavorite());
        rDb.close();
        return route;
    }

    public void updateLastSeen(Route route) {
        rDb.open();
        rDb.updateLastSeenByNumber(route.getNumber());
        rDb.close();
    }

    //Возвращает шифрованный ключ
    public static String sGenerateApiKey() {
        return String.valueOf(NowTimeGMT() * NowTimeGMT() / 2) + "180780" + nextSessionId();
    }

    //Возвращает начало текущего дня в формате unix
    public static int NowTimeGMT() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        TimeZone _timeZoneGMT = TimeZone.getTimeZone("GMT");
        cal.setTimeZone(_timeZoneGMT);
        return (int) (cal.getTimeInMillis() / 1000L);
    }

    public static String nextSessionId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    //Статистика по маршрутам
    public HashMap<Integer, RouteStatistic> loadRoutesStatisticsFromServer() throws HttpException, IOException, JSONException {
        HashMap<Integer, RouteStatistic> routeStatsMap = new HashMap<>();
        HttpHelper httpHelper = new HttpHelper();
        JSONObject params = new JSONObject();
        params.put("key", sGenerateApiKey());
        String response = httpHelper.getJson(Consts.API_SERVER_URL + "action=routes_stats", params);
        JSONArray jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            int routeId = jsonObject.getInt("route_id");
            int busesCount = jsonObject.getInt("active_bus");
            int avgSpeed = jsonObject.getInt("avgspeed");
            routeStatsMap.put(routeId, new RouteStatistic(avgSpeed, busesCount));
        }
        return routeStatsMap;
    }
}
