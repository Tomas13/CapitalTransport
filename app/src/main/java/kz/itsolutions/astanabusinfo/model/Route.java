package kz.itsolutions.astanabusinfo.model;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;

import kz.itsolutions.astanabusinfo.db.DBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.Marker;
import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@DatabaseTable
public class Route implements Comparable<Route>, Serializable {

    public static final String KEY_ROUTE_NUMBER = "key_route_number";

    public static final String FIELD_ID = "_id";
    public static final String FIELD_SERVER_ID = "id";
    public static final String FIELD_NAME_RU = "dRu";
    public static final String FIELD_NAME_KZ = "dKz";
    public static final String FIELD_DESCRIPTION_RU = "descrRu";
    public static final String FIELD_DESCRIPTION_KZ = "descrKz";
    public static final String FIELD_ROUTE_NUMBER = "n";
    public static final String FIELD_IS_FAVORITE = "is_favorite";
    public static final String FIELD_FROM_RU = "dFromRu";
    public static final String FIELD_TO_RU = "dToRu";
    public static final String FIELD_LAST_SEEN_DATE = "last_seen_date";
    public static final String FIELD_TRACK = "track";
    public static final String FIELD_BUS_REPORT_ROUTE_ID = "busreportRouteId";
    public static final String FIELD_IS_INFO_BUS = "field_is_info_bus";

    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    private long id;

    @DatabaseField(columnName = FIELD_FROM_RU)
    private String pointFrom;

    @DatabaseField(columnName = FIELD_TO_RU)
    private String pointTo;

    @DatabaseField(columnName = FIELD_NAME_RU)
    private String nameRu;

    @DatabaseField(columnName = FIELD_NAME_KZ)
    private String nameKz;

    @DatabaseField(columnName = FIELD_SERVER_ID, generatedId = false, unique = true)
    private int serverId;

    @DatabaseField(columnName = FIELD_BUS_REPORT_ROUTE_ID, generatedId = false, unique = false)
    private int busReportRouteId;

    @DatabaseField(columnName = FIELD_ROUTE_NUMBER)
    private int number;

    @DatabaseField(columnName = FIELD_DESCRIPTION_RU)
    private String descriptionRu;

    @DatabaseField(columnName = FIELD_DESCRIPTION_KZ)
    private String descriptionKz;

    @DatabaseField(columnName = FIELD_TRACK)
    private String track;

    private List<LatLng> trackPoints;
    private List<GeoPoint> trackPointsOsm;

    public Route linkedRoute;

    @DatabaseField(columnName = FIELD_IS_FAVORITE, defaultValue = "false")
    private boolean isFavorite;

    @DatabaseField(columnName = FIELD_IS_INFO_BUS, defaultValue = "false")
    private boolean isInfoBus;

    @DatabaseField(columnName = FIELD_LAST_SEEN_DATE, defaultValue = "0")
    private long lastSeenDate;

    private List<BusStop> busStops;

    @Deprecated
    private String group;
    private ArrayList<LatLng> routePointsGoogleCourse1;
    private ArrayList<LatLng> routePointsGoogleCourse2;
    private Forecast forecast;

    private PreparedQuery<BusStop> routePreparedQuery = null;

    public void setBusStops(ArrayList<BusStop> busStops) {
        this.busStops = busStops;
    }

    public Route() {
    }

    public Route(long id, int serverId, String pointFrom, String pointTo, String nameRu, String nameKz,
                 String descriptionRu, String descriptionKz, int number, ArrayList<BusStop> busStops, String group,
                 boolean isFavorite) {
        this.id = id;
        this.pointFrom = pointFrom.trim();
        this.pointTo = pointTo.trim();
        this.nameRu = nameRu;
        this.nameKz = nameKz;
        this.serverId = serverId;
        this.number = number;
        this.descriptionRu = descriptionRu;
        this.descriptionKz = descriptionKz;
        this.group = group;
        this.isFavorite = isFavorite;

        routePointsGoogleCourse1 = new ArrayList<LatLng>();
        routePointsGoogleCourse2 = new ArrayList<LatLng>();

        this.busStops = busStops;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof Route))
            return false;
        Route route = (Route) object;
        return (this.serverId == route.getServerId());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%s %s - %s", number, pointFrom, pointTo);
    }

    public String getPointFrom() {
        return pointFrom;
    }

    public String getPointTo() {
        return pointTo;
    }

    public String getNameRu() {
        return nameRu;
    }

    public String getNameKz() {
        return nameKz;
    }

    public int getServerId() {
        return serverId;
    }

    public int getNumber() {
        return number;
    }

    public int getBusReportRouteId() {
        return busReportRouteId;
    }

    public void setBusReportRouteId(int busReportRouteId) {
        this.busReportRouteId = busReportRouteId;
    }

    public String getDescriptionRu() {
        return descriptionRu;
    }

    public String getDescriptionKz() {
        return descriptionKz;
    }

    public ArrayList<LatLng> getRoutePointsForGoogleCourse1() {
        return routePointsGoogleCourse1;
    }

    public ArrayList<LatLng> getRoutePointsForGoogleCourse2() {
        return routePointsGoogleCourse2;
    }

    public void setGooglePointsCourse1(ArrayList<LatLng> points) {
        routePointsGoogleCourse1.addAll(points);
    }

    public void setGooglePointsCourse2(ArrayList<LatLng> points) {
        routePointsGoogleCourse2.addAll(points);
    }
/*
    public ArrayList<BusStop> getBusStops() {
        return busStops;
    }*/

    public String getGroup() {
        return group;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public long getLastSeenDate() {
        return lastSeenDate;
    }

    public void setLastSeenDate(long milliseconds) {
        this.lastSeenDate = milliseconds;
    }

    public Forecast getForecast() {
        return forecast;
    }

    public void setForecast(Forecast forecast) {
        this.forecast = forecast;
    }

    private List<LatLng> getInfoBusTrack() {
        List<LatLng> trackPoints = new ArrayList<LatLng>();
        String[] tracks = track.split(",");
        for (int i = 0; i < tracks.length; i++) {
            String[] lonLat = tracks[i].trim().split(" ");
            double lon = Double.parseDouble(lonLat[0].trim());
            double lat = Double.parseDouble(lonLat[1].trim());
            trackPoints.add(new LatLng(lat, lon));
        }
        return trackPoints;
    }

    private List<GeoPoint> getInfoBusTrackOsm() {
        List<GeoPoint> trackPoints = new ArrayList<GeoPoint>();
        String[] tracks = track.split(",");
        for (int i = 0; i < tracks.length; i++) {
            String[] lonLat = tracks[i].trim().split(" ");
            double lon = Double.parseDouble(lonLat[0].trim());
            double lat = Double.parseDouble(lonLat[1].trim());
            trackPoints.add(new GeoPoint(lat, lon));
        }
        return trackPoints;
    }

    public List<LatLng> getTrackPoints() {
        if (trackPoints != null) {
            return trackPoints;
        }
        if (isInfoBus) {
            return getInfoBusTrack();
        }
        trackPoints = new ArrayList<LatLng>();
        if (!TextUtils.isEmpty(track)) {
            try {
                JSONArray trackArray = new JSONArray(track);
                JSONObject object;
                for (int i = 0; i < trackArray.length(); i++) {
                    object = trackArray.getJSONObject(i);
                    double lon = object.getDouble("lon");
                    double lat = object.getDouble("lat");
                    trackPoints.add(new LatLng(lon, lat));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return trackPoints;
    }

    public List<GeoPoint> getTrackPointsOsm() {
        if (trackPoints != null) {
            return trackPointsOsm;
        }
        if (isInfoBus) {
            return getInfoBusTrackOsm();
        }
        trackPointsOsm = new ArrayList<GeoPoint>();
        if (!TextUtils.isEmpty(track)) {
            try {
                JSONArray trackArray = new JSONArray(track);
                JSONObject object;
                for (int i = 0; i < trackArray.length(); i++) {
                    object = trackArray.getJSONObject(i);
                    double lon = object.getDouble("lon");
                    double lat = object.getDouble("lat");
                    trackPointsOsm.add(new GeoPoint(lon, lat));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return trackPointsOsm;
    }

    public Route getLinkedRoute() {
        return getLinkedRoute(DBHelper.getHelper(), this.number, this.serverId);
    }

    public List<LatLng> getTrackPointsForInverseCourse() {
        Route route = this.getLinkedRoute();
        return route != null ? route.getTrackPoints() : new ArrayList<LatLng>();
    }

    public List<GeoPoint> getTrackPointsForInverseCourseOsm() {
        Route route = this.getLinkedRoute();
        return route != null ? route.getTrackPointsOsm() : new ArrayList<GeoPoint>();
    }

    @Override
    public int compareTo(Route another) {
        return this.number - another.number;
    }

    public static boolean hasRecords(DBHelper dbHelper) {
        Dao<Route, Integer> dao = dbHelper.getRouteDao();
        try {
            return dao.queryBuilder().queryForFirst() != null;
        } catch (SQLException e) {
            Log.e("routes", "SQLException", e);
        }
        return false;
    }

    public static Route getByServerId(DBHelper dbHelper, long serverId) {
        final Dao<Route, Integer> dao = dbHelper.getRouteDao();
        final PreparedQuery<Route> query;
        Route route = null;
        try {
            query = dao.queryBuilder().where().eq(FIELD_SERVER_ID, serverId).prepare();
            route = dao.queryForFirst(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return route;
    }

    public static Route getByNumber(DBHelper dbHelper, int number) {
        final Dao<Route, Integer> dao = dbHelper.getRouteDao();
        final PreparedQuery<Route> query;
        Route route = null;
        try {
            query = dao.queryBuilder().where().eq(FIELD_ROUTE_NUMBER, number).prepare();
            route = dao.queryForFirst(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return route;
    }

    public static List<Route> getAllGroupByNumber(DBHelper dbHelper) {
        List<Route> result = new LinkedList<>();
        Dao<Route, Integer> dao = dbHelper.getRouteDao();
        try {
            result = dao.queryBuilder().groupBy(FIELD_ROUTE_NUMBER).query();
            for (Iterator<Route> it = result.iterator(); it.hasNext();) {
                Route currentRoute = it.next();
                if (currentRoute.getNumber() == 104 ||
                        currentRoute.getNumber() == 109 ||
                        currentRoute.getNumber() == 112 ||
                        currentRoute.getNumber() == 126 ||
                        currentRoute.getNumber() == 129 ||
                        currentRoute.getNumber() == 130 ||
                        currentRoute.getNumber() == 53) {
                    it.remove();
                }

                if (currentRoute.getNumber() == 54)
                    currentRoute.setNumber(105);
            }
        } catch (SQLException e) {
            Log.e("routes", "SQLException", e);
        }
        return result;
    }


    public void setNumber(int number) {
        this.number = number;
    }

    public static List<Route> getFavorites(DBHelper dbHelper) {
        List<Route> result = new LinkedList<>();
        Dao<Route, Integer> dao = dbHelper.getRouteDao();
        try {
            result = dao.queryBuilder().where().eq(FIELD_IS_FAVORITE, true).query();
        } catch (SQLException e) {
            Log.e("routes", "SQLException", e);
        }
        return result;
    }

    public static List<Route> getHistoryRoutes(DBHelper dbHelper) {
        List<Route> result = new LinkedList<>();
        Dao<Route, Integer> dao = dbHelper.getRouteDao();
        try {
            result = dao.queryBuilder().groupBy(FIELD_ROUTE_NUMBER).orderBy(FIELD_LAST_SEEN_DATE, false).limit(10L).where().ne(FIELD_LAST_SEEN_DATE, 0).query();
        } catch (SQLException e) {
            Log.e("routes", "SQLException", e);
        }
        return result;
    }

    private static Route getLinkedRoute(DBHelper dbHelper, int number, int serverId) {
        final Dao<Route, Integer> dao = dbHelper.getRouteDao();
        final PreparedQuery<Route> query;
        Route Route = null;
        try {
            query = dao.queryBuilder().where()
                    .ne(FIELD_SERVER_ID, serverId)
                    .and()
                    .eq(Route.FIELD_ROUTE_NUMBER, number)
                    .prepare();
            Route = dao.queryForFirst(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Route;
    }

    public List<BusStop> getBusStops() {
        if (busStops != null && busStops.size() != 0) {
            return busStops;
        }
        DBHelper dbHelper = DBHelper.getHelper();
        try {
            if (routePreparedQuery == null) {
                routePreparedQuery = getRouteBusStopsQuery(dbHelper);
            }
            routePreparedQuery.setArgumentHolderValue(0, this);
            busStops = dbHelper.getBusStopDao().query(routePreparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return busStops;
    }

    /**
     * Build our query for Post objects that match a User.
     */
    private PreparedQuery<BusStop> getRouteBusStopsQuery(DBHelper dbHelper) throws SQLException {
        // build our inner query for UserPost objects
        QueryBuilder<RouteBusStop, Integer> routeBusStopQb = dbHelper.getRouteBusStopDao().queryBuilder();
        // just select the post-id field
        routeBusStopQb.selectColumns(RouteBusStop.FIELD_BUS_STOP_ID);
        SelectArg userSelectArg = new SelectArg();
        // you could also just pass in user1 here
        routeBusStopQb.where().eq(RouteBusStop.FIELD_ROUTE_ID, userSelectArg);
        // build our outer query for Post objects
        QueryBuilder<BusStop, Integer> busStopQb = dbHelper.getBusStopDao().queryBuilder();
        // where the id matches in the post-id from the inner query
        busStopQb.where().in(BusStop.FIELD_ID, routeBusStopQb);
        return busStopQb.prepare();
    }

    public static List<Route> findRoutesForPoints(DBHelper dbHelper, Marker from, Marker to) {
        return findRoutesForPoints(dbHelper, new GeoPoint(from.latitude, from.longitude), new GeoPoint(to.latitude, to.longitude));
    }

    public static List<Route> findRoutesForPoints(DBHelper dbHelper, LatLng from, LatLng to) {
        return findRoutesForPoints(dbHelper, new GeoPoint(from.latitude, from.longitude), new GeoPoint(to.latitude, to.longitude));
    }

    public static List<Route> findRoutesForPoints(DBHelper dbHelper, GeoPoint from, GeoPoint to) {
        List<BusStop> fromPointNearestBusStops = BusStop.getNearestBusStops(DBHelper.getHelper(), from.getLatitude(), from.getLongitude(), 500);
        List<BusStop> toPointNearestBusStops = BusStop.getNearestBusStops(DBHelper.getHelper(), to.getLatitude(), to.getLongitude(), 500);
        List<Integer> fromPointRoutes = new ArrayList<>();
        List<Integer> toPointRoutes = new ArrayList<>();
        for (BusStop busStop : fromPointNearestBusStops) {
            for (Route r : busStop.getRoutes(dbHelper)) {
                if (fromPointRoutes.contains(r.getNumber())) continue;
                fromPointRoutes.add(r.getNumber());
            }
        }
        for (BusStop busStop : toPointNearestBusStops) {
            for (Route r : busStop.getRoutes(dbHelper)) {
                if (toPointRoutes.contains(r.getNumber())) continue;
                toPointRoutes.add(r.getNumber());
            }
        }
        List<Route> results = new ArrayList<>();
        for (Integer number : fromPointRoutes) {
            if (!toPointRoutes.contains(number)) continue;
            results.add(Route.getByNumber(dbHelper, number));
        }
        Collections.sort(results);
        return results;
    }

    public static List<Route> fromJsonArray(JSONArray values) throws JSONException {
        List<Route> result = new LinkedList<Route>();
        for (int i = 0; i < values.length(); ++i) {
            JSONObject category = values.getJSONObject(i);
            result.add(new Route(category));
        }
        return result;
    }

    public static Route fromJsonObject(JSONObject jsonObject) throws JSONException {
        return new Route(jsonObject);
    }

    private Route(JSONObject json) throws JSONException {
        this.serverId = json.getInt(FIELD_SERVER_ID);
        this.nameRu = json.getString(FIELD_NAME_RU).trim();
        this.nameKz = json.getString(FIELD_NAME_KZ).trim();
        this.pointFrom = json.getString(FIELD_FROM_RU);
        this.pointTo = json.optString(FIELD_TO_RU);
        this.descriptionKz = json.getString(FIELD_DESCRIPTION_KZ);
        this.descriptionRu = json.getString(FIELD_DESCRIPTION_RU);
        this.number = json.getInt(FIELD_ROUTE_NUMBER);
        this.track = json.getString(FIELD_TRACK);
    }

    public static Route fromInfoBusJsonObject(JSONObject jsonObject) throws JSONException {
        return new Route(
                jsonObject.getInt("routeNumber"),
                jsonObject.getString("routeName"),
                jsonObject.getInt("busreportRouteId"),
                jsonObject.getString("location")
        );
    }

    // for InfoBus routes
    private Route(int routeNumber, String routeName, int busReportRouteId, String trackInfoBus) throws JSONException {
        this.number = routeNumber;
        String[] fromTo = routeName.split("-");
        this.serverId = busReportRouteId;
        this.busReportRouteId = busReportRouteId;
        if (fromTo.length > 1) {
            this.pointFrom = fromTo[0].trim();
            this.pointTo = fromTo[1].trim();
        } else {
            this.pointFrom = routeName;
            this.pointTo = routeName;
        }
        this.track = trackInfoBus;
        this.isInfoBus = true;
    }
}
