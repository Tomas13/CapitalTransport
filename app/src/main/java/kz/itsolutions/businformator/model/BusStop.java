package kz.itsolutions.businformator.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

import kz.itsolutions.businformator.controllers.BusStopController;
import kz.itsolutions.businformator.db.DBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@DatabaseTable
public class BusStop implements Serializable {

    public static final String FIELD_ID = "_id";
    public static final String FIELD_NAME = "title";
    public static final String FIELD_SERVER_ID = "id";
    public static final String FIELD_DESCRIPTION = "desc";
    public static final String FIELD_LAT = "y";
    public static final String FIELD_LON = "x";

    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    private long id;
    @DatabaseField(columnName = FIELD_SERVER_ID, generatedId = false, unique = true)
    private int serverId;
    @DatabaseField(columnName = FIELD_NAME)
    private String name;
    @DatabaseField(columnName = FIELD_DESCRIPTION)
    private String description;
    @DatabaseField(columnName = FIELD_LAT)
    private double latitude;
    @DatabaseField(columnName = FIELD_LON)
    private double longitude;

    @ForeignCollectionField(eager = false)
    private Collection<RouteBusStop> routeBusStops = new ArrayList<>();

    private LatLng pointGoogle;

    private List<Route> routes;

    public BusStop() {
    }

    public BusStop(long id, int serverId, String name) {
        this.id = id;
        this.serverId = serverId;
        this.name = name;
    }

    public BusStop(long id, int serverId, String name, double lat, double lon, String desc) {
        this.id = id;
        this.serverId = serverId;
        this.name = name;
        this.pointGoogle = new LatLng(lat, lon);
        this.description = desc;
    }

    public long getId() {
        return id;
    }

    public int getServerId() {
        return serverId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LatLng getPointGoogle() {
        return new LatLng(latitude, longitude);
    }

    public GeoPoint getPointOsm() {
        return new GeoPoint(latitude, longitude);
    }

    @Override
    public String toString() {
        return name;
    }

    public static BusStop getByServerId(DBHelper dbHelper, long serverId) {
        final Dao<BusStop, Integer> dao = dbHelper.getBusStopDao();
        final PreparedQuery<BusStop> query;
        BusStop busStop = null;
        try {
            query = dao.queryBuilder().where().eq(FIELD_SERVER_ID, serverId).prepare();
            busStop = dao.queryForFirst(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return busStop;
    }

    public static List<BusStop> getNearestBusStops(DBHelper dbHelper, GeoPoint point) {
        if (point == null) {
            return null;
        }
        return getNearestBusStops(dbHelper, point.getLatitude(), point.getLongitude());
    }

    public static List<BusStop> getNearestBusStops(DBHelper dbHelper, double lat, double lon) {
        return getNearestBusStops(dbHelper, lat, lon, BusStopController.BUS_STOP_RADIUS);
    }

    public static List<BusStop> getNearestBusStops(DBHelper dbHelper, double lat, double lon, int radius) {
        List<BusStop> nearestBusStops = new ArrayList<BusStop>();
        if (lat == 0.0 && lon == 0.0) {
            return nearestBusStops;
        }
        double distance;
        List<BusStop> allBusStops = BusStop.getAll(dbHelper);
        for (BusStop busStop : allBusStops) {
            distance = BusStopController.calcDistance(lat, lon,
                    busStop.getPointGoogle().latitude, busStop.getPointGoogle().longitude);
            if (distance < radius) {
                nearestBusStops.add(busStop);
            }
        }
        return nearestBusStops;

    }

    public static List<BusStop> getAll(DBHelper dbHelper) {
        List<BusStop> result = new LinkedList<>();
        Dao<BusStop, Integer> dao = dbHelper.getBusStopDao();
        try {
            result = dao.queryBuilder().query();
        } catch (SQLException e) {
            Log.e("routes", "SQLException", e);
        }
        return result;
    }

    private PreparedQuery<Route> routePreparedQuery = null;

    public List<Route> getRoutes(DBHelper dbHelper) {
        if (routes != null && routes.size() != 0) {
            return routes;
        }
        try {
            if (routePreparedQuery == null) {
                routePreparedQuery = getBusStopRoutesQuery(dbHelper);
            }
            routePreparedQuery.setArgumentHolderValue(0, this);
            routes = dbHelper.getRouteDao().query(routePreparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return routes;
    }

    /**
     * Build our query for Post objects that match a User.
     */
    private PreparedQuery<Route> getBusStopRoutesQuery(DBHelper dbHelper) throws SQLException {
        // build our inner query for UserPost objects
        QueryBuilder<RouteBusStop, Integer> routeBusStopQb = dbHelper.getRouteBusStopDao().queryBuilder();
        // just select the post-id field
        routeBusStopQb.selectColumns(RouteBusStop.FIELD_ROUTE_ID);
        SelectArg userSelectArg = new SelectArg();
        // you could also just pass in user1 here
        routeBusStopQb.where().eq(RouteBusStop.FIELD_BUS_STOP_ID, userSelectArg);
        // build our outer query for Post objects
        QueryBuilder<Route, Integer> routeQb = dbHelper.getRouteDao().queryBuilder();
        // where the id matches in the post-id from the inner query
        routeQb.where().in(BusStop.FIELD_ID, routeBusStopQb);
        return routeQb.prepare();
    }

    public static List<BusStop> fromJsonArray(JSONArray values) throws JSONException {
        List<BusStop> result = new LinkedList<>();
        for (int i = 0; i < values.length(); ++i) {
            JSONObject object = values.getJSONObject(i);
            result.add(new BusStop(object));
        }
        return result;
    }

    public static BusStop fromJsonObject(JSONObject jsonObject) throws JSONException {
        return new BusStop(jsonObject);
    }

    private BusStop(JSONObject json) throws JSONException {
        this.serverId = json.getInt(FIELD_SERVER_ID);
        this.name = json.getString(FIELD_NAME).trim();
        this.description = json.getString(FIELD_DESCRIPTION).trim();
        this.latitude = json.getDouble(FIELD_LAT);
        this.longitude = json.getDouble(FIELD_LON);
    }

    public static boolean hasRecords(DBHelper dbHelper) {
        Dao<BusStop, Integer> dao = dbHelper.getBusStopDao();
        try {
            return dao.queryBuilder().queryForFirst() != null;
        } catch (SQLException e) {
            Log.e("routes", "SQLException", e);
        }
        return false;
    }
}
