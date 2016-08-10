package kz.itsolutions.businformatordraft.model;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@DatabaseTable
public class Bus {

    public static final String FIELD_ID = "_id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SERVER_ID = "server_id";
    public static final String FIELD_ROUTE_NUMBER = "route_number";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_SPEED = "speed";
    public static final String FIELD_Z = "z";
    public static final String FIELD_LAT = "lat";
    public static final String FIELD_LON = "lon";

    private long id;

    @DatabaseField(columnName = FIELD_NAME, generatedId = true)
    private String name;

    @DatabaseField(columnName = FIELD_SERVER_ID, generatedId = true)
    private long serverId;

    @DatabaseField(columnName = FIELD_ROUTE_NUMBER, generatedId = true)
    private int routeNumber;

    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    private int routeId;

    @DatabaseField(columnName = FIELD_TIME, generatedId = true)
    private long time;

    @DatabaseField(columnName = FIELD_SPEED, generatedId = true)
    private double speed;

    @DatabaseField(columnName = FIELD_ID, generatedId = true)
    private LatLng pointGoogle;

    @DatabaseField(columnName = FIELD_Z, generatedId = true)
    private int z;

    private double longitude;

    private double latitude;


    public Bus() {

    }

    public Bus(long id, String name, long serverId, long time, double speed, int z) {
        this.id = id;
        this.name = name;
        this.serverId = serverId;
        this.time = time;
        this.speed = speed;
        this.z = z;
    }

    public Bus(long id, int routeNumber, int routeId, String name, long serverId, long time, double speed, double lat, double lon, int z) {
        this.id = id;
        this.routeNumber = routeNumber;
        this.name = name;
        this.serverId = serverId;
        this.time = time;
        this.speed = speed;
        this.pointGoogle = new LatLng(lat, lon);
        this.z = z;
        this.routeId = routeId;
    }

    public static Bus fromJson(JSONObject jsonObject) throws JSONException {
        int routeId = jsonObject.getInt("route_id");
        int routeNumber = jsonObject.getInt("route_n");
        int serverBusId = jsonObject.getInt("bus_id");
        String busName = jsonObject.getString("bus_name");
        double x = jsonObject.getDouble("x");
        double y = jsonObject.getDouble("y");
        int z = jsonObject.getInt("z");
        long time = jsonObject.getLong("t");
        double speed = jsonObject.getDouble("s");
        return new Bus(0, routeNumber, routeId, busName, serverBusId, time, speed, y, x, z);
    }




    public static Bus fromInfoBusJson(Route route, JSONObject jsonObject) throws JSONException {
        long serverBusId = jsonObject.getLong("imei");
        String imei = String.valueOf(serverBusId);
        String busName = imei.length() >= 3 ? imei.substring(imei.length() - 3) : "777";//jsonObject.getString("name");
        double lon = jsonObject.getDouble("lon");
        double lat = jsonObject.getDouble("lat");
        int direction = jsonObject.getInt("direction");
        double speed = jsonObject.getDouble("speed");
        return new Bus(0, route.getNumber(), route.getServerId(), busName, serverBusId, 0, speed, lat, lon, direction);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof Bus))
            return false;
        Bus bus = (Bus) object;
        return (this.serverId == bus.getServerId());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        if (TextUtils.isEmpty(name) || name.equals("null"))
            return String.valueOf(serverId);
        return name;
    }

    public long getServerId() {
        return serverId;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public int getRouteId() {
        return routeId;
    }

    public long getTime() {
        return time;
    }

    public String getTime(String format) {
        if (time == 0) {
            return "";
        }
        return epoch2DateString(time, format);
    }

    public double getSpeed() {
        return speed;
    }

    public LatLng getPointGoogle() {
        return pointGoogle;
    }

    public GeoPoint getPointOsm() {
        return new GeoPoint(pointGoogle.latitude, pointGoogle.longitude);
    }

    public int getZ() {
        return z;
    }

    private static String epoch2DateString(long epochSeconds, String formatString) {
        Date date = new Date(epochSeconds * 1000);
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        format.setTimeZone(TimeZone.getTimeZone("GMT+6"));
        return format.format(date);
    }

    public static int getDirectionIcon(Context ctx, int z, int routeLineColor) {
        String drawableName = null;
        if ((z >= 0 && z <= 22.5) || (z >= 337.5 && z <= 360)) {
            drawableName = "bus_n";
        } else if (z >= 22.5 && z <= 67.5) {
            drawableName = "bus_ne";
        } else if (z >= 67.5 && z <= 112.5) {
            drawableName = "bus_e";
        } else if (z >= 112.5 && z <= 157.5) {
            drawableName = "bus_se";
        } else if (z >= 157.5 && z <= 202.5) {
            drawableName = "bus_s";
        } else if (z >= 202.5 && z <= 247.5) {
            drawableName = "bus_sw";
        } else if (z >= 247.5 && z <= 292.5) {
            drawableName = "bus_w";
        } else if (z >= 292.5 && z <= 337.5) {
            drawableName = "bus_nw";
        }
        switch (routeLineColor) {
            case Color.RED:
                drawableName = drawableName + "_p";
                break;
            case Color.MAGENTA:
                drawableName = drawableName + "_o";
                break;
        }
        return ctx.getResources().getIdentifier(drawableName, "drawable", ctx.getPackageName());
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setRouteNumber(int routeNumber) {
        this.routeNumber = routeNumber;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }
}
