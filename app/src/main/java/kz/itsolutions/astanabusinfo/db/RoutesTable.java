package kz.itsolutions.astanabusinfo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class RoutesTable extends ProjectDB {

    private final int MAX_HISTORY_COUNT = 10;

    public RoutesTable(Context c) {
        super(c);
    }

    public boolean hasRoutes() {
        Cursor cursor = mDb.rawQuery("SELECT * FROM ".concat(ROUTES), null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count != 0;
    }

    /*
        Метод возвращает все (А->B & B->A) маршруты
     */
    @Deprecated
    public Cursor getAllRoutes() {
        Cursor cursor = mDb.rawQuery(String
                .format("SELECT * FROM %s ORDER BY %s", ROUTES, ROUTE_NUMBER), null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    /*
        Метод возвращает только (А->B) маршруты
     */
    public Cursor getRoutes() {
        Cursor cursor = mDb.rawQuery(String
                .format("SELECT * FROM %s GROUP BY %s", ROUTES, ROUTE_NUMBER), null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getRoutesOrderByLastSeenDate() {
        Cursor cursor = mDb.rawQuery(String
                .format("SELECT * FROM %s WHERE %s != 0 ORDER BY %s DESC LIMIT %d", ROUTES, ROUTE_LAST_SEEN_DATE,
                        ROUTE_LAST_SEEN_DATE, MAX_HISTORY_COUNT * 2), null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getFavoriteRoutes() {
        Cursor cursor = mDb.rawQuery(String
                .format("SELECT * FROM %s WHERE %s == 1 ORDER BY %s", ROUTES, ROUTE_IS_FAVORITE, ROUTE_NUMBER), null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getRoutes(String value) {
        value = value.replace("\"", "\"\"");
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT * FROM %s WHERE %s = %s %s LIKE \"%%%s%%\" OR %s LIKE \"%%%s%%\"", ROUTES,
                        ROUTE_NUMBER, value, ROUTE_NAME_RU, value, ROUTE_NAME_KZ, value),
                null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getBusStopRoutes(int busStopId) {
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT * FROM %s, %s WHERE %s = %s AND %s = %s", ROUTES, ROUTE_BUS_STOPS,
                        ROUTE_BUS_STOP_BUS_STOP_ID, busStopId,
                        ROUTE_SERVER_ID, ROUTE_BUS_STOP_ROUTE_ID),
                null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public void insertRoutes(JSONArray jsonArray) {
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(mDb, ROUTES);
        int routeServerIdIndex = ih.getColumnIndex(ROUTE_SERVER_ID);
        int nameRu = ih.getColumnIndex(ROUTE_NAME_RU);
        int nameKz = ih.getColumnIndex(ROUTE_NAME_KZ);
        int descriptionRu = ih.getColumnIndex(ROUTE_DESCRIPTION_RU);
        int descriptionKz = ih.getColumnIndex(ROUTE_DESCRIPTION_KZ);
        int number = ih.getColumnIndex(ROUTE_NUMBER);
        int track = ih.getColumnIndex(ROUTE_TRACK);
        int isFavorite = ih.getColumnIndex(ROUTE_IS_FAVORITE);
        int pointFrom = ih.getColumnIndex(ROUTE_POINT_FROM);
        int pointTo = ih.getColumnIndex(ROUTE_POINT_TO);

        try {
            mDb.beginTransaction();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                ih.prepareForInsert();
                int routeServerId = json.getInt("id");
                ih.bind(routeServerIdIndex, routeServerId);
                ih.bind(pointFrom, json.getString("dFromRu").trim());
                ih.bind(pointTo, json.getString("dToRu").trim());
                ih.bind(nameRu, json.getString("dRu"));
                ih.bind(nameKz, json.getString("dKz"));
                ih.bind(number, json.getInt("n"));
                ih.bind(descriptionRu, json.getString("descrRu"));
                ih.bind(descriptionKz, json.getString("descrKz"));
                ih.bind(track, json.getJSONArray("track").toString());
                ih.bind(isFavorite, false);
                ih.execute();
            }
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            ih.close();
            mDb.endTransaction();
        }
        BusStopsTable busStopsTable = new BusStopsTable(mCtx);
        JSONObject json;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                json = jsonArray.getJSONObject(i);
                int routeServerId = json.getInt("id");
                busStopsTable.insertBusStops(json.getJSONArray("busstops"), routeServerId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /* public long putRoute(int id, String number, int streetId, int areaId) {
         ContentValues cv = new ContentValues();
         cv.put(ROUTE_SERVER_ID, id);
         cv.put(ROUTE_NUMBER, number.trim());
         return mDb.insert(ROUTES, null, cv);
     }

     public long updateRoute(int id, String number, int streetId, int areaId) {
         ContentValues cv = new ContentValues();
         cv.put(ROUTE_NUMBER, number.trim());
         return mDb.update(ROUTES, cv, "Id=" + id, null);
     }
 */

    public long updateIsFavorite(int serverId, boolean isFavorite) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_IS_FAVORITE, isFavorite ? 1 : 0);
        return mDb.update(ROUTES, cv, ROUTE_SERVER_ID + "=" + serverId, null);
    }

    public long updateIsFavoriteByNumber(int number, boolean isFavorite) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_IS_FAVORITE, isFavorite ? 1 : 0);
        return mDb.update(ROUTES, cv, ROUTE_NUMBER + "=" + number, null);
    }

    public long updateLastSeen(int serverId) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_LAST_SEEN_DATE, new Date().getTime());
        return mDb.update(ROUTES, cv, ROUTE_SERVER_ID + "=" + serverId, null);
    }

    public long updateLastSeenByNumber(int number) {
        ContentValues cv = new ContentValues();
        cv.put(ROUTE_LAST_SEEN_DATE, new Date().getTime());
        return mDb.update(ROUTES, cv, ROUTE_NUMBER + "=" + number, null);
    }

    public Cursor getRoute(int serverId) {
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT * FROM %s WHERE %s = %d", ROUTES, ROUTE_SERVER_ID, serverId),
                null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getRouteByNumber(int number) {
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT * FROM %s WHERE %s = %d", ROUTES, ROUTE_NUMBER, number),
                null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getRouteTrack(int serverId) {
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT %s FROM %s WHERE %s = %d", ROUTE_TRACK, ROUTES, ROUTE_SERVER_ID, serverId),
                null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getRouteTrackByNumber(int routeNumber, boolean isOrderAsc) {
        String orderBy =  "ORDER BY id " + (isOrderAsc ? "ASC" : "DESC");
        Cursor cursor = mDb.rawQuery(
                String.format("SELECT %s FROM %s WHERE %s = %d %s", ROUTE_TRACK, ROUTES, ROUTE_NUMBER, routeNumber,
                        orderBy), null);
        if (cursor != null && cursor.getCount() != 0)
            cursor.moveToFirst();
        return cursor;
    }

    public void deleteData() {
        mDb.delete(ROUTES, null, null);
    }
}
