package kz.itsolutions.businformator.utils;

import com.google.android.gms.maps.model.LatLng;
import org.osmdroid.util.GeoPoint;

public class Consts {
    static String serverUrl = "sapi.i-t.kz";
    static String packageName = "com.itsolutions.businformator";
    static String packageVersion = "20";
    static String city = "astana";

    public static final String API_SERVER_URL = "http://" + serverUrl + "/astana/?n=" + packageName + "&v=" +
            packageVersion + "&city=" + city + "&";



    // кол-во маршрутов, которые можно выбрать в режиме MultiSelect
    public static short MAX_ROUTES_ON_MAP = 3;

    // интервал запроса списка автобусов для маршрута, мс
    public static short BUS_TIMER_INTERVAL = 4000;
    public static String BUS_POSITIONS_URL = "http://astrabus.otgroup.kz/api/";
    public static String BUS_POSITIONS_URL_NEW = "http://astrabus.otgroup.kz/api/buscoordinates";


    // интервал запроса статистика по маршрутам, мс
    public static short ROUTES_STATISTIC_TIMER_INTERVAL = 10000;

    // координаты города Астана
    public static final LatLng DEFAULT_CITY_LOCATION = new LatLng(51.1666667, 71.43333329);
    public static final GeoPoint DEFAULT_CITY_LOCATION_OSM = new GeoPoint(51.157600, 71.435165);
    public static final GeoPoint DEFAULT_CAMERA_POSITION = new GeoPoint(51.297118, 71.198858);

    public static final boolean IS_FREE = true;

    //KEYS
    public static final String KEY_ROUTES_ID = "routeIds";



}
