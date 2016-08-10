package kz.itsolutions.businformatordraft.db;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class RouteBusStopsTable extends ProjectDB {

    public RouteBusStopsTable(Context c) {
        super(c);
    }

    public boolean hasRouteBusStops() {
        Cursor cursor = mDb.rawQuery("SELECT * FROM ".concat(ROUTE_BUS_STOPS), null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count != 0;
    }

    public Cursor getRouteBusStops() {
        Cursor cursor = mDb.rawQuery(String
                .format("SELECT * FROM %s ORDER BY %s", ROUTES, ROUTE_NUMBER), null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getRouteBusStops(String value) {
        value = value.replace("\"", "\"\"");
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT * FROM %s WHERE %s = %s %s LIKE \"%%%s%%\" OR %s LIKE \"%%%s%%\"", ROUTES,
                        ROUTE_NUMBER, value, ROUTE_NAME_RU, value, ROUTE_NAME_KZ, value),
                null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public void insertRouteBusStops(int routeId, int busStopId) {
        if (hasRouteBusStop(routeId, busStopId)) {
            Log.v(LOG_TAG, "duplicate routeBusStop");
            return;
        }
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(mDb, ROUTE_BUS_STOPS);
        int routeIdIndex = ih.getColumnIndex(ROUTE_BUS_STOP_ROUTE_ID);
        int busStopIdIndex = ih.getColumnIndex(ROUTE_BUS_STOP_BUS_STOP_ID);
        try {
            mDb.beginTransaction();
            ih.prepareForInsert();
            ih.bind(routeIdIndex, routeId);
            ih.bind(busStopIdIndex, busStopId);
            ih.execute();
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            ih.close();
            mDb.endTransaction();
        }
    }

    public void insertRouteBusStops(JSONArray jsonArray, int routeId) {
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(mDb, ROUTE_BUS_STOPS);
        int routeIdIndex = ih.getColumnIndex(ROUTE_BUS_STOP_ROUTE_ID);
        int busStopIdIndex = ih.getColumnIndex(ROUTE_BUS_STOP_BUS_STOP_ID);
        try {
            mDb.beginTransaction();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                int busStopId = json.getInt("id");
                if (hasRouteBusStop(routeId, busStopId)) {
                    Log.v(LOG_TAG, "duplicate routeBusStop");
                    return;
                }
                ih.prepareForInsert();
                ih.bind(routeIdIndex, routeId);
                ih.bind(busStopIdIndex, busStopId);
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

    public void insertBusStopRoutes(int busStopId, JSONArray jsonArray) {
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(mDb, ROUTE_BUS_STOPS);
        int routeIdIndex = ih.getColumnIndex(ROUTE_BUS_STOP_ROUTE_ID);
        int busStopIdIndex = ih.getColumnIndex(ROUTE_BUS_STOP_BUS_STOP_ID);
        try {
            mDb.beginTransaction();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                int routeId = json.getInt("id");
                if (hasRouteBusStop(routeId, busStopId)) {
                    Log.v(LOG_TAG, "duplicate routeBusStop");
                    return;
                }
                ih.prepareForInsert();
                ih.bind(routeIdIndex, busStopId);
                ih.bind(busStopIdIndex, busStopId);
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

    /* public long putRoute(int id, String number, int streetId, int areaId) {
         ContentValues cv = new ContentValues();
         cv.put(ROUTE_BUS_STOP_ROUTE_ID, id);
         cv.put(ROUTE_BUS_STOP_BUS_STOP_ID, number.trim());
         return mDb.insert(ROUTES, null, cv);
     }

    public long updateRoute(int id, String number, int streetId, int areaId) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_NUMBER, number.trim());
        return mDb.update(ROUTES, cv, "Id=" + id, null);
    }*/

    public boolean hasRouteBusStop(int routeId, int busStopId) {
        Cursor c = getRouteBusStop(routeId, busStopId);

        boolean result = c.getCount() > 0;
        c.close();
        return result;
    }

    public Cursor getRouteBusStop(int routeId, int busStopId) {
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT * FROM %s WHERE %s = %d AND %s = %d", ROUTE_BUS_STOPS, ROUTE_BUS_STOP_ROUTE_ID,
                        routeId, ROUTE_BUS_STOP_BUS_STOP_ID, busStopId),
                null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public void deleteData() {
        mDb.delete(ROUTES, null, null);
    }
}
