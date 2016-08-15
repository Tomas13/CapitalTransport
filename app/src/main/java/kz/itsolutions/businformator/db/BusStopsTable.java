package kz.itsolutions.businformator.db;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class BusStopsTable extends ProjectDB {

    public BusStopsTable(Context c) {
        super(c);
    }

    public boolean hasBusStops() {
        Cursor cursor = mDb.rawQuery("SELECT * FROM ".concat(BUS_STOPS), null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count != 0;
    }

    public Cursor getRouteBusStops(long routeId) {
        Cursor cursor = mDb.rawQuery(String
                .format("SELECT * FROM %s,%s WHERE RouteBusStops.RouteServerId == %s AND BusStops.ServerId == RouteBusStops.BusStopServerId",
                        BUS_STOPS, ROUTE_BUS_STOPS, routeId), null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getBusStops() {
        Cursor cursor = mDb.rawQuery(String
                .format("SELECT * FROM %s", BUS_STOPS), null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getBusStops(String value) {
        value = value.replace("\"", "\"\"");
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT DISTINCT * FROM %s WHERE %s = %s %s LIKE \"%%%s%%\" OR %s LIKE \"%%%s%%\"", BUS_STOPS,
                        ROUTE_NUMBER, value, ROUTE_NAME_RU, value, ROUTE_NAME_KZ, value),
                null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public void insertBusStops(JSONArray jsonArray, int routeId) {
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(mDb, BUS_STOPS);
        int busStopServerIdIndex = ih.getColumnIndex(BUS_STOP_SERVER_ID);
        int name = ih.getColumnIndex(BUS_STOP_NAME);
        int longitude = ih.getColumnIndex(BUS_STOP_LONGITUDE);
        int latitude = ih.getColumnIndex(BUS_STOP_LATITUDE);
        try {
            mDb.beginTransaction();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                int busStopServerId = json.getInt("id");
                if (hasBusStop(busStopServerId))
                    continue;
                ih.prepareForInsert();
                ih.bind(busStopServerIdIndex, busStopServerId);
                ih.bind(name, json.getString("title"));
                ih.bind(latitude, json.getDouble("x"));
                ih.bind(longitude, json.getDouble("y"));
                ih.execute();
            }
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            ih.close();
            mDb.endTransaction();
        }
        RouteBusStopsTable routeBusStopsTable = new RouteBusStopsTable(mCtx);
        routeBusStopsTable.insertRouteBusStops(jsonArray, routeId);
    }

    public void insertBusStops(JSONArray jsonArray) {
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(mDb, BUS_STOPS);
        int busStopServerIdIndex = ih.getColumnIndex(BUS_STOP_SERVER_ID);
        int name = ih.getColumnIndex(BUS_STOP_NAME);
        int longitude = ih.getColumnIndex(BUS_STOP_LONGITUDE);
        int latitude = ih.getColumnIndex(BUS_STOP_LATITUDE);
        int desc = ih.getColumnIndex(BUS_STOP_DESCRIPTION);
        try {
            mDb.beginTransaction();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                int busStopServerId = json.getInt("id");
                if (hasBusStop(busStopServerId))
                    continue;
                ih.prepareForInsert();
                ih.bind(busStopServerIdIndex, busStopServerId);
                ih.bind(name, json.getString("title"));
                ih.bind(latitude, json.getDouble("x"));
                ih.bind(longitude, json.getDouble("y"));
                ih.bind(desc, json.getString("desc"));
                ih.execute();
            }
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            ih.close();
            mDb.endTransaction();
        }
    }

    public boolean hasBusStop(int serverId) {
        Cursor c = getBusStop(serverId);
        boolean result = c.getCount() > 0;
        c.close();
        return result;
    }

    public Cursor getBusStop(int serverId) {
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT * FROM %s WHERE %s = %d", BUS_STOPS, BUS_STOP_SERVER_ID, serverId),
                null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public void deleteData() {
        mDb.delete(BUS_STOPS, null, null);
    }
}
