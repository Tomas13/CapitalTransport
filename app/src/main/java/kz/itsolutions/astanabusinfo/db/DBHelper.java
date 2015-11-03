package kz.itsolutions.astanabusinfo.db;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import kz.itsolutions.astanabusinfo.R;
import kz.itsolutions.astanabusinfo.activities.SplashActivity;
import kz.itsolutions.astanabusinfo.model.BusStop;
import kz.itsolutions.astanabusinfo.model.Route;
import kz.itsolutions.astanabusinfo.model.RouteBusStop;
import kz.itsolutions.astanabusinfo.utils.AssetFile;
import kz.itsolutions.astanabusinfo.utils.Consts;
import kz.itsolutions.astanabusinfo.utils.HttpHelper;

public class DBHelper extends OrmLiteSqliteOpenHelper {

    private static final String THIS_FILE = "DBHelper";

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "astanabus.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 22;

    //Hashed daos
    private HashMap<Class<?>, Dao<?, ?>> daos = new HashMap<Class<?>, Dao<?, ?>>();

    private Context context;
    private SplashActivity.LoadDataAsyncTask mAsyncTask;

    private static DBHelper instance = null;
    private static int instanceCount = 0;
    private static Object locker = new Object();

    public static void init(Context context) {
        if (instance == null) {
            synchronized (locker) {
                if (instance == null) {
                    instance = OpenHelperManager.getHelper(context, DBHelper.class);
                }
            }
        }
        ++instanceCount;
    }

    public static void release() {
        --instanceCount;
        if (instanceCount == 0) {
            synchronized (locker) {
                if (instanceCount == 0) {
                    instance.close();
                    OpenHelperManager.releaseHelper();
                    instance = null;
                }
            }
        }
    }

    public static DBHelper getHelper() {
        if (instance == null) {
            throw new IllegalStateException("DBHelper should be inited with DBHelper.init(context) before using");
        }
        return instance;
    }

    public DBHelper(Context context) throws NameNotFoundException {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        createTables(connectionSource);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            dropTables(connectionSource);
            createTables(connectionSource);
        }
    }

    private void createTables(ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Route.class);
            TableUtils.createTable(connectionSource, BusStop.class);
            TableUtils.createTable(connectionSource, RouteBusStop.class);
        } catch (SQLException e) {
            Log.e(THIS_FILE, "SQLException", e);
        }
    }

    private void dropTables(ConnectionSource connectionSource) {
        try {
            TableUtils.dropTable(connectionSource, Route.class, true);
            TableUtils.dropTable(connectionSource, BusStop.class, true);
            TableUtils.dropTable(connectionSource, RouteBusStop.class, true);
        } catch (SQLException e) {
            Log.e(THIS_FILE, "SQLException", e);
        }
    }

    public Dao<Route, Integer> getRouteDao() {
        return getCachedDao(Route.class);
    }

    public Dao<BusStop, Integer> getBusStopDao() {
        return getCachedDao(BusStop.class);
    }

    public Dao<RouteBusStop, Integer> getRouteBusStopDao() {
        return getCachedDao(RouteBusStop.class);
    }

    private <D extends Dao<T, ?>, T> D getCachedDao(Class<T> clazz) {
        if (!daos.containsKey(clazz)) {
            D dao = null;
            try {
                dao = getDao(clazz);
                daos.put(clazz, dao);
            } catch (SQLException e) {
                Log.e(THIS_FILE, "SQLException", e);
            }
            return dao;
        } else {
            @SuppressWarnings("unchecked")
            D dao = (D) daos.get(clazz);
            return dao;
        }
    }

    public boolean recreateTables() {
        dropTables(connectionSource);
        createTables(connectionSource);
        return true;
    }

    public void updateRoutesFromAssets() {
        try {
            String data = AssetFile.readFromFile("ib_routes.json", context);
            if (!TextUtils.isEmpty(data)) {
                updateRoutes(new JSONArray(data));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateRoutesFromInternetDaniyar() {
        HttpHelper httpHelper = new HttpHelper();
        JSONObject params = new JSONObject();

        try {
            String response = httpHelper.getInfoBusJson(Consts.BUS_POSITIONS_URL);

           // Log.d("DANIYAR", "result: " +  response);

            updateRoutesDaniyar(new JSONObject(response));

           // Log.d("DANIYAR", "DATABASE IS CALLED");
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateRoutesDaniyar(final JSONObject jsonObject) throws JSONException {
        DBHelper dbHelper = DBHelper.getHelper();
        Dao<Route, Integer> dao = dbHelper.getRouteDao();

        String segmentsString = AssetFile.readFromFile("segments.json", context);
        JSONArray segmentsJson = new JSONArray(segmentsString);

        int myMonkeyId = 0;
        try {
            for (Iterator<String> iter = jsonObject.keys(); iter.hasNext();) {
                String key = iter.next();

                JSONObject currentJsonObject = jsonObject.getJSONObject(key);
                int routeNumber = currentJsonObject.getInt("route");

                Route route = Route.getByNumber(dbHelper, routeNumber);

                if (route == null) {
                    String name = "Автобус №" + routeNumber;
                    int routeId = myMonkeyId;
                    String location = "";

                    for (int i = 0; i < segmentsJson.length(); i++) {
                        JSONObject currentRouteElement = segmentsJson.getJSONObject(i);

                        String field2 = currentRouteElement.getString("FIELD2");
                        if (field2.contains("B"))
                            continue;

                        if (currentRouteElement.getInt("FIELD2") == routeNumber) {
                            location += currentRouteElement.getString("FIELD4") + " " + currentRouteElement.getString("FIELD3") + ", ";
                        }
                    }

                    if (location.length() > 2) {
                        location = location.substring(0, location.length() - 2);
                    }

                    if (location.length() == 0) {
                        String secondarySegments = AssetFile.readFromFile("ib_routes.json", context);
                        JSONArray secondaryJsonArray = new JSONArray(secondarySegments);

                        for (int i = 0; i < secondaryJsonArray.length(); i++) {
                            JSONObject currJsonObject = secondaryJsonArray.getJSONObject(i);

                            if (Integer.parseInt(currJsonObject.getString("routeNumber")) == routeNumber) {
                                location = currJsonObject.getString("location");
                                break;
                            }
                        }
                    }

                    route = new Route(routeNumber, name, routeId, location);
                    Log.d("DANIYAR", routeNumber + " " + location);
                    createRecord(route, Route.class);

                    location = "";
                }

                myMonkeyId++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean updateRoutes(final JSONArray objects) {
        try {
            DBHelper dbHelper = DBHelper.getHelper();
            Dao<Route, Integer> dao = dbHelper.getRouteDao();
            for (int i = 0; i < objects.length(); i++) {
                JSONObject jsonObject = objects.getJSONObject(i);
                int routeNumber = jsonObject.getInt("routeNumber");
                Route route = Route.getByNumber(dbHelper, routeNumber);
                if (route != null) {
                    route.setBusReportRouteId(jsonObject.getInt(Route.FIELD_BUS_REPORT_ROUTE_ID));
                    dao.update(route);
                    route = route.getLinkedRoute();
                    if (route != null) {
                        route.setBusReportRouteId(jsonObject.getInt(Route.FIELD_BUS_REPORT_ROUTE_ID));
                        dao.update(route);
                    }
                } else {
                    createRecord(Route.fromInfoBusJsonObject(jsonObject), Route.class);
                }
            }
        } catch (SQLException e) {
            Log.e(THIS_FILE, "Failed to populateFromAssets DB", e);
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void populateFromAssets(SplashActivity.LoadDataAsyncTask asyncTask) {
        mAsyncTask = asyncTask;
        try {
            if (mAsyncTask != null) {
                mAsyncTask.publishProgress(R.string.data_preparation, -1);
            }
            String data = AssetFile.readFromFile("bus_stops.json", context);
            if (!TextUtils.isEmpty(data)) {
                populateBusStops(new JSONArray(data));
            }
            if (mAsyncTask != null) {
                mAsyncTask.publishProgress(R.string.data_preparation, -1);
            }
            data = AssetFile.readFromFile("routes.json", context);
            if (!TextUtils.isEmpty(data)) {
                populateRoutes(new JSONArray(data));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean populateRoutes(final JSONArray objects) {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    DBHelper dbHelper = DBHelper.getHelper();
                    int length = objects.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject jsonObject = objects.getJSONObject(i);
                        createRecord(Route.fromJsonObject(jsonObject), Route.class);
                        JSONArray busStopsArray = jsonObject.getJSONArray("busstops");
                        int routeId = jsonObject.getInt(Route.FIELD_SERVER_ID);
                        for (int j = 0; j < busStopsArray.length(); j++) {
                            createRecord(new RouteBusStop(dbHelper, routeId, busStopsArray.getJSONObject(j).getInt("id"), j),
                                    RouteBusStop.class);
                        }
                        if (mAsyncTask != null) {
                            mAsyncTask.publishProgress(R.string.routes, Math.round((i * 100) / length));
                        }
                    }
                   /* List<Route> routeList = Route.fromJsonArray(objects);
                    for (Route route : routeList) {
                        createRecord(route, Route.class);
                        createRecord(new RouteBusStop(), RouteBusStop.class);
                    }*/
                    return null;
                }
            });
        } catch (SQLException e) {
            Log.e(THIS_FILE, "Failed to populateFromAssets DB", e);
            return false;
        }
        return true;
    }

    private boolean populateBusStops(final JSONArray objects) {
        try {
            TransactionManager.callInTransaction(getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    List<BusStop> busStops =  BusStop.fromJsonArray(objects);
                    createCollection(busStops, BusStop.class, busStops.size());
                    return null;
                }
            });
        } catch (SQLException e) {
            Log.e(THIS_FILE, "Failed to populateFromAssets DB", e);
            return false;
        }
        return true;
    }

    private <T> void createCollection(Iterable<T> collection, Class<T> clazz, int collectionSize) throws SQLException {
        Log.i(DATABASE_NAME, "Create Collection for " + clazz.getName());
        Dao<T, Integer> dao = getCachedDao(clazz);
        int i = 0;
        try {
            for (T item : collection) {
                ++i;
                dao.create(item);
                if (mAsyncTask != null) {
                    mAsyncTask.publishProgress(R.string.bus_stops, Math.round((i * 100) / collectionSize));
                }
            }
        } catch (SQLException e) {
            Log.e(THIS_FILE, "On Create: SQLException on number: " + i, e);
            throw e;
        }
        Log.i(THIS_FILE, "Created collection with size:" + i);
    }

    public <T> void createRecord(T item, Class<T> clazz) throws SQLException {
        Log.i(DATABASE_NAME, "Create Collection for " + clazz.getName());
        Dao<T, Integer> dao = getCachedDao(clazz);
        try {
            dao.create(item);
        } catch (SQLException e) {
            throw e;
        }
    }
}
