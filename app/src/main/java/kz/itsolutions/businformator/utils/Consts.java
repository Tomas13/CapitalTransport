package kz.itsolutions.businformator.utils;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.GeoPoint;

public class Consts {
    static String serverUrl = "sapi.i-t.kz";
    static String packageName = "com.itsolutions.businformatorDraft";
    static String packageVersion = "20";
    static String city = "astana";

    public static final String API_SERVER_URL = "http://" + serverUrl + "/astana/?n=" + packageName + "&v=" +
            packageVersion + "&city=" + city + "&";

    public static final String urlJsonObj = "http://crm.astanalrt.com/notifications/savedevice";


    public final static String LOG_TAG = "astana_bus";
    public final static String MAIN_PREFS = "main_prefs";
    public static final String KEY_SELECTED_BUS_STOP_ID = "selected_bus_stop_id";
    public static final String KEY_SHOW_WELCOME_MESSAGE = "show_welcome_message";
    public static final String KEY_SHOW_BUS_STOPS = "key_show_bus_stops";
    public static final String KEY_STARTS_COUNT = "key_starts_count";
    public static final String KEY_NOT_SHOW_VOTE_DIALOG = "key_not_show_rating_dialog";
    public static final String KEY_IS_SHOWN_SEARCH_MENU = "key_is_shown_search_menu";
    public static final String KEY_LAST_OPENED_TAB = "key_last_opened_tab";
    public static final String KEY_FIND_ROUTES_FROM_LAT = "key_find_routes_from_lat";
    public static final String KEY_FIND_ROUTES_FROM_LON = "key_find_routes_from_lon";
    public static final String KEY_FIND_ROUTES_TO_LAT = "key_find_routes_to_lat";
    public static final String KEY_FIND_ROUTES_TO_LON = "key_find_routes_to_lon";
    public static final String KEY_SELECTED_ROUTE_SERVER_ID = "key_selected_route_server_id";

    public static final int REQUEST_FORECAST_CODE = 11;


    public static final int REQUEST_LOCATION = 1;


    // кол-во маршрутов, которые можно выбрать в режиме MultiSelect
    public static short MAX_ROUTES_ON_MAP = 5;

    // интервал запроса списка автобусов для маршрута, мс
    public static short BUS_TIMER_INTERVAL = 4000;
    public static String BUS_POSITIONS_URL = "http://astrabus.otgroup.kz/api/";
    public static String BUS_POSITIONS_URL_NEW = "http://astrabus.otgroup.kz/api/buscoordinates";
    public static String BUS_EDGES = "http://astrabus.otgroup.kz/api/edges";


    // интервал запроса статистика по маршрутам, мс
    public static short ROUTES_STATISTIC_TIMER_INTERVAL = 10000;

    // координаты города Астана
    public static final LatLng DEFAULT_CITY_LOCATION = new LatLng(51.1666667, 71.43333329);
    public static final GeoPoint DEFAULT_CITY_LOCATION_OSM = new GeoPoint(51.157600, 71.435165);
    public static final GeoPoint DEFAULT_CAMERA_POSITION = new GeoPoint(51.297118, 71.198858);

    public static final boolean IS_FREE = true;

    //KEYS
    public static final String KEY_ROUTES_ID = "routeIds";


    public final static int
            BLUE = 0xFC6C4A,
            GREEN = 0x5CBD1B,
            ORANGE = 0x0387EE,
            PINK = 0x5B4AFC,
            PURPLE = 0xE24163;


}
