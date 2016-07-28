package kz.itsolutions.businformator.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpException;
import org.json.JSONException;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.Polyline;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.BusStopsAdapter;
import kz.itsolutions.businformator.adapters.MyPagerAdapter;
import kz.itsolutions.businformator.adapters.RoutesAdapter;
import kz.itsolutions.businformator.controllers.BusController;
import kz.itsolutions.businformator.controllers.BusStopController;
import kz.itsolutions.businformator.controllers.RouteController;
import kz.itsolutions.businformator.db.DBHelper;
import kz.itsolutions.businformator.model.Bus;
import kz.itsolutions.businformator.model.BusStop;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.model.RouteStatistic;
import kz.itsolutions.businformator.utils.Consts;
import kz.itsolutions.businformator.utils.Weather;
import kz.itsolutions.businformator.widgets.MyProvider;
import kz.itsolutions.businformator.widgets.SingleWidget;
import kz.itsolutions.businformator.widgets.infoWindows.BusInfoWindow;
import kz.itsolutions.businformator.widgets.infoWindows.BusStopInfoWindow;
import kz.itsolutions.businformator.widgets.tabPageIndicator.TabPageIndicator;

public class MapOsmActivity extends Activity implements View.OnClickListener, Weather.WeatherInterface,
        View.OnLongClickListener, Marker.OnMarkerClickListener, Marker.OnMarkerDragListener {

    private String LOG_TAG = "astana_bus";
    public static final int FORECAST_CODE = 11;

    /* Views */
    private DrawerLayout mDrawerLayout;
    ActionBar mActionBar;
    View mRightDrawer, mLeftDrawer, busStopInfoWindowView, busInfoWindowView;
    ListView listViewRoutes, listViewFavoriteRoutes, listViewHistoryRoutes, listViewFoundRoutes;
    EditText etSearchRoute;
    TextView tvBusStopTitle, tvBusStopDescription, tvBusTitle, tvBusDescription, tvInternetStatus;
    ActionBarDrawerToggle mDrawerToggle;
    ImageButton btnShowNearestBusStops;
    LinearLayout llWelcomeMessage;
    AutoCompleteTextView etPointFrom, etPointTo;
    ImageButton btnPointToClear, btnPointToOnMap, btnPointFromClear, btnPointFromOnMap;

    DBHelper mDbHelper;
    SharedPreferences mSharedPreferences;
    boolean mIsRouteStatisticEnabled = false;

    MapView mMapView;
    MapController mController;
    Marker mCustomLocationMarker, mMarkerPointFrom, mMarkerPointTo;

    /* Overlays */
    MyLocationNewOverlay mMyLocationOverlay;
    FolderOverlay busStopsMarkersOverlay, busesMarkersOverlay;
    FolderOverlay routesPathOverlay;
    ItemizedIconOverlay manualMarkersOverlay;
    MapEventsOverlay mapEventsOverlay;
    CompassOverlay compassOverlay;

    /* Markers */
    Marker lastSelectedMarker;

    private ResourceProxy mResourceProxy;

    private CharSequence mTitle;
    LoadDataAsyncTask mLoadDataAsyncTask;

    RoutesAdapter mAdapter, mHistoryAdapter, mFavoriteAdapter, mFoundRoutesAdapter;
    RouteController mRouteController;
    BusStopController mBusStopController;
    List<Route> mRoutes, mSelectedRoutes;
    List<Bus> mBuses;
    Route mSelectedRoute;
    BusStop mSelectedBusStop, mBusStopInfoWindowShowed, mAlarmBusStop;
    HashMap<Integer, RouteStatistic> statisticHashMap;
    HashMap<Marker, BusStop> hashMapMarkerBusStops;
    ArrayList<Marker> markers;
    HashMap<Long, Marker> hashMapBusMarkers;

    BusStopsAdapter mBusStopsPointToAdapter, mBusStopsPointFromAdapter;

    MyPagerAdapter mPagerAdapter;
    ViewPager mViewPager;

    RoutesDrawerItemClickListener listener;
    boolean isMarkerClicked, isSessionFromWidget, needFinishWhenOnPause, isFindRoutesMode, isCustomLocationMode,
            isAlarmMode, isShowBusStopsMode, showBusStopsForRoute;
    AlertDialog.Builder notHaveGmsDialog, voteAppDialog;

    public enum PointType {FROM, TO}

    private PointType currentPointType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        DBHelper.init(getApplicationContext());
        mDbHelper = DBHelper.getHelper();
        SplashActivity.updateCurrentInstallationMap(this);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.map_osm_activity);
        hashMapMarkerBusStops = new HashMap<Marker, BusStop>();
//        EasyTracker.getInstance().setContext(this);
//        mTracker = EasyTracker.getTracker();
        boolean needShowWelcomeMessage = getIntent().getBooleanExtra(MapGoogleActivity.KEY_SHOW_WELCOME_MESSAGE, false);
        llWelcomeMessage = (LinearLayout) findViewById(R.id.ll_welcome_message);
        btnShowNearestBusStops = (ImageButton) findViewById(R.id.btn_show_nearest_bus_stops_for_custom_location);
        btnShowNearestBusStops.setOnClickListener(this);
        btnShowNearestBusStops.setOnLongClickListener(this);
        tvInternetStatus = (TextView) findViewById(R.id.tv_internet_status);
        busStopInfoWindowView = getLayoutInflater().inflate(R.layout.bus_stop_window_osm, null);
        tvBusStopTitle = (TextView) busStopInfoWindowView.findViewById(R.id.tv_bus_stop_title);
        tvBusStopDescription = (TextView) busStopInfoWindowView.findViewById(R.id.tv_bus_stop_description);
        busInfoWindowView = getLayoutInflater().inflate(R.layout.bus_window_osm, null);
        tvBusTitle = (TextView) busInfoWindowView.findViewById(R.id.tv_bus_title);
        tvBusDescription = (TextView) busInfoWindowView.findViewById(R.id.tv_bus_description);
        //
        markers = new ArrayList<Marker>();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Left drawer
        mLeftDrawer = findViewById(R.id.left_drawer);
//
//        btnPointToClear = (ImageButton) findViewById(R.id.btn_point_to_clear);
//        btnPointFromClear = (ImageButton) findViewById(R.id.btn_point_from_clear);
//        btnPointToOnMap = (ImageButton) findViewById(R.id.btn_point_to_on_map);
//        btnPointFromOnMap = (ImageButton) findViewById(R.id.btn_point_from_on_map);

//        btnPointToClear.setOnClickListener(this);
//        btnPointFromClear.setOnClickListener(this);
//        btnPointToOnMap.setOnClickListener(this);
//        btnPointFromOnMap.setOnClickListener(this);
//
//        etPointFrom = (AutoCompleteTextView) findViewById(R.id.et_point_from);
//        etPointFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                BusStop busStop = mBusStopsPointFromAdapter.getItem(position);
//                setMarkerPointFrom(busStop.getName(), busStop.getPointOsm(), true);
//            }
//        });
//        etPointFrom.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() != 0) {
//                    btnPointFromClear.setVisibility(View.VISIBLE);
//                    btnPointFromOnMap.setVisibility(View.GONE);
//                } else {
//                    btnPointFromClear.setVisibility(View.GONE);
//                    btnPointFromOnMap.setVisibility(View.VISIBLE);
//                    if (mMarkerPointFrom != null) {
//                        mMarkerPointFrom.remove(mMapView);
//                        mMarkerPointFrom = null;
//                        mMapView.invalidate();
//                    }
//                    findRoutes();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//        etPointTo = (AutoCompleteTextView) findViewById(R.id.et_point_to);
//        etPointTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                BusStop busStop = mBusStopsPointToAdapter.getItem(position);
//                setMarkerPointTo(busStop.getName(), busStop.getPointOsm(), true);
//            }
//        });
//        etPointTo.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() != 0) {
//                    btnPointToClear.setVisibility(View.VISIBLE);
//                    btnPointToOnMap.setVisibility(View.GONE);
//                } else {
//                    btnPointToClear.setVisibility(View.GONE);
//                    btnPointToOnMap.setVisibility(View.VISIBLE);
//                    if (mMarkerPointTo != null) {
//                        mMarkerPointTo.remove(mMapView);
//                        mMarkerPointTo = null;
//                        mMapView.invalidate();
//                    }
//                    findRoutes();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, findViewById(R.id.left_drawer));

//        listViewFoundRoutes = (ListView) findViewById(R.id.lv_routes);

        mPagerAdapter = new MyPagerAdapter(this);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        // Right drawer
        mRightDrawer = findViewById(R.id.right_drawer);

        etSearchRoute = (EditText) findViewById(R.id.et_search_route);
        etSearchRoute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                RoutesAdapter adapter = getAdapter();
                if (adapter == null)
                    return;

                for (int i = 0; i < adapter.getCount(); i++) {
                    if (listViewRoutes.isItemChecked(i))
                        listViewRoutes.setItemChecked(i, false);
                }
                adapter.getFilter().filter(editable.toString());
            }
        });

        mRouteController = new RouteController(this);
        mBusStopController = new BusStopController(this);

        listener = new RoutesDrawerItemClickListener();

        View page1 = getLayoutInflater().inflate(R.layout.drawer_routes, null);
        View page2 = getLayoutInflater().inflate(R.layout.drawer_routes, null);
        View page3 = getLayoutInflater().inflate(R.layout.drawer_routes, null);
        listViewRoutes = (ListView) page1.findViewById(R.id.listView);
        listViewRoutes.setOnItemClickListener(listener);

        listViewFavoriteRoutes = (ListView) page2.findViewById(R.id.listView);
        listViewFavoriteRoutes.setOnItemClickListener(listener);
        // TODO: empty view for all list views
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.empty_view, null);
        listViewFavoriteRoutes.setEmptyView(v.findViewById(android.R.id.empty));

        listViewHistoryRoutes = (ListView) page3.findViewById(R.id.listView);
        listViewHistoryRoutes.setOnItemClickListener(listener);

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            listViewRoutes.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listViewFavoriteRoutes.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listViewHistoryRoutes.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
//            listViewFoundRoutes.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

            listViewRoutes.setMultiChoiceModeListener(new ModeCallback());
            listViewFavoriteRoutes.setMultiChoiceModeListener(new ModeCallback());
            listViewHistoryRoutes.setMultiChoiceModeListener(new ModeCallback());
            //listViewFoundRoutes.setMultiChoiceModeListener(new ModeCallback());
        }

        mViewPager.setOffscreenPageLimit(3);
        mPagerAdapter.addItem(listViewRoutes);
        mPagerAdapter.addItem(listViewFavoriteRoutes);
        mPagerAdapter.addItem(listViewHistoryRoutes);

        mViewPager.setAdapter(mPagerAdapter);
        TabPageIndicator pageIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        pageIndicator.setViewPager(mViewPager);
        pageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                etSearchRoute.setText("");
                switch (position) {
                    case 0:
                        if (mAdapter == null) return;
                        selectItemOnListView(listViewRoutes, mAdapter);
                        break;
                    case 1:
                        if (mFavoriteAdapter == null) return;
                        mFavoriteAdapter = new RoutesAdapter(MapOsmActivity.this, mAdapter.getFavoritesRoutes(), RoutesAdapter.TYPE.Favorites);
                        listViewFavoriteRoutes.setAdapter(mFavoriteAdapter);
                        selectItemOnListView(listViewFavoriteRoutes, mFavoriteAdapter);
                        break;
                    case 2:
                        if (mHistoryAdapter == null) return;
                        mHistoryAdapter.setData(Route.getHistoryRoutes(mDbHelper));
                        selectItemOnListView(listViewHistoryRoutes, mHistoryAdapter);
                        break;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // enable ActionBar app icon to behave as action to toggle nav drawer
        mActionBar = getActionBar();
        Weather.fetchData(this);
        if (mActionBar!=null){
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setHomeButtonEnabled(false);
            mActionBar.setIcon(getResources().getDrawable(R.drawable.ic_menu_bus));
        }
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon


        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.empty,  /* "open drawer" description for accessibility */
                R.string.empty  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                mMapView.setBuiltInZoomControls(true);
                hideKeyboard(etSearchRoute);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                mDrawerToggle.syncState();

                if (mSelectedBusStop == null && mSelectedRoute == null) {
                    setTitle(getString(R.string.app_name));
                } else if (mSelectedBusStop != null) {
                    mActionBar.setTitle(mSelectedBusStop.getName());
                    mActionBar.setSubtitle(mSelectedBusStop.getDescription());
                }
                if (mSelectedRoute != null) {
                    startBusTimer();
                    mActionBar.setTitle(getString(R.string.app_name));
                    mActionBar.setSubtitle(mSelectedRoute.toString());
                } else if (mSelectedRoutes != null && mSelectedRoutes.size() > 0) {
                    busesMarkersOverlay.getItems().clear();
                    manualMarkersOverlay.removeAllItems();
                    startBusesTimer();
                }
                stopRoutesStatisticsTimer();
            }

            public void onDrawerOpened(View drawerView) {
                mMapView.setBuiltInZoomControls(false);
                if (llWelcomeMessage.getVisibility() == View.VISIBLE) {
                    llWelcomeMessage.setVisibility(View.GONE);
                }
                switch (drawerView.getId()) {
                    case R.id.right_drawer:
                        setTitle(getString(R.string.select_route));
                        break;
                    case R.id.left_drawer:
                        mActionBar.setSubtitle("");
                        if (mMarkerPointFrom == null) {
                            GeoPoint geoPoint = null;
                            if (mMyLocationOverlay.getMyLocation() != null) {
                                geoPoint = new GeoPoint(mMyLocationOverlay.getMyLocation().getLatitude(), mMyLocationOverlay.getMyLocation().getLongitude());
                                setMarkerPointFrom(getString(R.string.from), geoPoint, false);
                            }
                            String text = geoPoint == null ? "" : getString(R.string.my_location);
                            etPointFrom.setText(text);
                        }
                        if (mMarkerPointTo == null) {
                            etPointTo.setText("");
                        }
                        findRoutes();
                        setTitle(getString(R.string.search));
                        break;
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                mDrawerToggle.syncState();
                stopBusTimer();
                stopBusesTimer();
                if (drawerView.getId() == R.id.right_drawer)
                    startRoutesStatisticsTimer();
            }

            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
        };

        //mDrawerToggle.setDrawerIndicatorEnabled(false);

        mDrawerLayout.setEnabled(false);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        initVoteAppDialog();

        mResourceProxy = new ResourceProxyImpl(getApplicationContext());
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.setBuiltInZoomControls(true);
        // mMapView.setMultiTouchControls(true);

        mController = (MapController) mMapView.getController();
        mMapView.setMinZoomLevel(10);
        //mMapView.setScrollableAreaLimit(new BoundingBoxE6(51.261781, 71.630859, 50.825755, 71.229858));

        compassOverlay = new CompassOverlay(this, mMapView);
        compassOverlay.enableCompass();
        mMapView.getOverlays().add(compassOverlay);

        mMyLocationOverlay = new MyLocationNewOverlay(this, mMapView);
        mMyLocationOverlay.setDrawAccuracyEnabled(true);
        mMyLocationOverlay.enableFollowLocation();
        mMapView.getOverlays().add(mMyLocationOverlay);

        routesPathOverlay = new FolderOverlay(this);
        mMapView.getOverlays().add(routesPathOverlay);

        busStopsMarkersOverlay = new FolderOverlay(this);
        mMapView.getOverlays().add(busStopsMarkersOverlay);

        busesMarkersOverlay = new FolderOverlay(this);
        mMapView.getOverlays().add(busesMarkersOverlay);

        manualMarkersOverlay = new ItemizedIconOverlay<OverlayItem>(new ArrayList<OverlayItem>(),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

                    @Override
                    public boolean onItemLongPress(int arg0, OverlayItem arg1) {
                        return false;
                    }

                    @Override
                    public boolean onItemSingleTapUp(int arg0, OverlayItem arg1) {
                        return false;

                    }
                },
                mResourceProxy);
        mMapView.getOverlays().add(manualMarkersOverlay);
        mapEventsOverlay = new MapEventsOverlay(this, new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                closeInfoWindow();
                if (isFindRoutesMode) {
                    switch (currentPointType) {
                        case FROM:
                            setMarkerPointFrom(getString(R.string.from), geoPoint, true);
                            etPointFrom.setText(getString(R.string.point_a));
                            break;
                        case TO:
                            setMarkerPointTo(getString(R.string.to), geoPoint, true);
                            etPointTo.setText(getString(R.string.point_b));
                            break;
                    }
                    //mDrawerLayout.openDrawer(mLeftDrawer);
                    return true;
                }
                if (mCustomLocationMarker != null && isMarkerClicked) {
                    //mCustomLocationMarker.hideInfoWindow();
                    isMarkerClicked = false;
                    return true;
                }
                isMarkerClicked = false;
                if (!isCustomLocationMode)
                    return false;
                showNearestBusStops(geoPoint);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {
                return false;
            }
        });
        mMapView.getOverlays().add(mapEventsOverlay);

        // TODO
       /* mMapView.setOnMarkerDragListener(this);
        mMapView.setOnMapClickListener(this);
        mMapView.setMyLocationEnabled(true);
        mMapView.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if (mSelectedRoute != null || mSelectedRoutes != null) {
                    if (hashMapMarkerBusStops != null
                            && hashMapMarkerBusStops.containsKey(marker)) {
                        mBusStopInfoWindowShowed = hashMapMarkerBusStops.get(marker);
                        tvBusStopTitle.setText(mBusStopInfoWindowShowed.getName());
                        tvBusStopDescription.setText(getString(R.string.aside) + " " + mBusStopInfoWindowShowed.getDescription());
                        return busStopInfoWindowView;
                    } else {
                        mBusStopInfoWindowShowed = null;
                        tvBusTitle.setText(marker.title);
                        tvBusDescription.setText(marker.snippet);
                        return busInfoWindowView;
                    }
                }
                // остановка
                int busStopServerId = -1;
                try {
                    busStopServerId = Integer.valueOf(marker.snippet);
                } catch (NumberFormatException ex) {
                    mBuses = null;
                }
                if (busStopServerId == -1)
                    return null;
                if (!TextUtils.isEmpty(marker.snippet) && ((mSelectedBusStop == null) || mSelectedBusStop.getServerId() != busStopServerId)) {
                    mSelectedBusStop = BusStop.getByServerId(mDbHelper, busStopServerId);
                }
                // маркер, который установил пользователь для показа ближайших остановок
                if (mSelectedBusStop == null || TextUtils.isEmpty(marker.snippet))
                    return null;
                mBusStopInfoWindowShowed = mSelectedBusStop;
                tvBusStopTitle.setText(mBusStopInfoWindowShowed.getName());
                tvBusStopDescription.setText(getString(R.string.aside) + " " + mBusStopInfoWindowShowed.getDescription());
                return busStopInfoWindowView;
            }
        });
        mMapView.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mBusStopInfoWindowShowed != null) {
                    Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
                    intent.putExtra(MapGoogleActivity.KEY_SELECTED_BUS_STOP_ID, mBusStopInfoWindowShowed.getServerId());
                    needFinishWhenOnPause = false;
                    startActivityForResult(intent, REQUEST_FORECAST_CODE);
                }
            }
        });

        mMapView.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            public boolean onMarkerClick(Marker marker) {
                // When a marker is clicked set it as the selected marker so
                // we can track it for the InfoWindow adapter. This will
                // make sure that the correct marker is still displayed when
                // the callback from DownloadBubbleInfo is made to
                // marker.showInfoWindow() which is needed to update the
                // InfoWindow view.
                if (isAlarmMode && hashMapMarkerBusStops.containsKey(marker)) {
                    mAlarmBusStop = hashMapMarkerBusStops.get(marker);
                    ignoreBusList.clear();
                }
                isMarkerClicked = true;
                return false;
            }
        });*/

        setDefaultCameraPosition();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(SingleWidget.WIDGET_ROUTE_NUMBER)) {
            isSessionFromWidget = true;
            needFinishWhenOnPause = true;
            int routeNumber = getIntent().getExtras().getInt(SingleWidget.WIDGET_ROUTE_NUMBER, -1);
            if (routeNumber != -1) {
                selectRoute(Route.getByNumber(mDbHelper, routeNumber), true);
            } else {
                startActivity(new Intent(this, SplashActivity.class));
                finish();
            }
        }
        checkStartsCount();
        mLoadDataAsyncTask = new LoadDataAsyncTask();
        mLoadDataAsyncTask.execute();
        if (needShowWelcomeMessage) {
            llWelcomeMessage.setVisibility(View.VISIBLE);
            llWelcomeMessage.setOnClickListener(this);
        } else if (!isSessionFromWidget) {
            SharedPreferences prefs = getSharedPreferences(MapGoogleActivity.MAIN_PREFS, MODE_PRIVATE);
            int currentTab = prefs.getInt(MapGoogleActivity.KEY_LAST_OPENED_TAB, 0);
            if (!prefs.contains(MapGoogleActivity.KEY_IS_SHOWN_SEARCH_MENU) || !prefs.getBoolean(MapGoogleActivity.KEY_IS_SHOWN_SEARCH_MENU, false)) {
                //mDrawerLayout.openDrawer(mLeftDrawer);
                prefs.edit().putBoolean(MapGoogleActivity.KEY_IS_SHOWN_SEARCH_MENU, true).commit();
            } else {
                if (!mDrawerLayout.isDrawerOpen(mLeftDrawer))
                   mDrawerLayout.openDrawer(mRightDrawer);
                mViewPager.setCurrentItem(currentTab);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        closeInfoWindow();
        lastSelectedMarker = marker;
        marker.showInfoWindow();
        return false;
    }

    private void setDefaultCameraPosition() {
        mController.setZoom(11);
        mController.setCenter(Consts.DEFAULT_CITY_LOCATION_OSM);
        mController.animateTo(Consts.DEFAULT_CAMERA_POSITION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case FORECAST_CODE:
                    if (data.getExtras() == null || !data.getExtras().containsKey(Consts.KEY_ROUTES_ID)) {
                      //  Crashlytics.log("!data.getExtras().containsKey(Consts.KEY_ROUTES_ID)");
                        return;
                    }
                    if (mAdapter == null) {
                     //   Crashlytics.log("mAdapter == null");
                        return;
                    }
                    closeInfoWindow();
                    List<Integer> selectedRouteNumbers = (ArrayList<Integer>) data.getSerializableExtra(Consts.KEY_ROUTES_ID);
                    List<Route> routes = mAdapter.getOriginalData();
                    List<Route> selectedRoutes = new ArrayList<Route>();
                    if (selectedRouteNumbers.size() == 1) {
                        for (int i = 0; i < routes.size(); i++) {
                            if (selectedRouteNumbers.contains(routes.get(i).getNumber())) {
                                selectedRoutes.add(routes.get(i));
                                break;
                            }
                        }
                        Route route = selectRoute(selectedRoutes.get(0), false);
                    } else if (selectedRouteNumbers.size() > 1) {
                        isRouteChanged = true;
                        for (int i = 0; i < routes.size(); i++) {
                            if (selectedRouteNumbers.contains(routes.get(i).getNumber())) {
                                selectedRoutes.add(routes.get(i));
                            }
                        }
                        mSelectedRoutes = selectedRoutes;
                        drawRoutes(mSelectedRoutes);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //MenuItem item = menu.findItem(R.id.menu_display_bus_stops);
        showBusStopsForRoute = mSharedPreferences.getBoolean(MapGoogleActivity.KEY_SHOW_BUS_STOPS, true);
        //item.setChecked(showBusStopsForRoute);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_routes:
                if (!mDrawerLayout.isDrawerOpen(mRightDrawer))
                    mDrawerLayout.openDrawer(mRightDrawer);
                else
                    mDrawerLayout.closeDrawer(mRightDrawer);
                mDrawerLayout.closeDrawer(mLeftDrawer);
                return true;
//            case R.id.menu_augmented_reality:
//                if (mSelectedRoute == null) {
//                    showToast(getString(R.string.first_select_route));
//                    mDrawerLayout.openDrawer(mRightDrawer);
//                    return true;
//                }
//                Intent i = new Intent(MapOsmActivity.this, AugmentedRealityActivity.class);
//                i.putExtra(AugmentedRealityActivity.KEY_SELECTED_ROUTE_ID, mSelectedRoute.getServerId());
//                startActivity(i);
//                return true;
            case R.id.menu_about:
                startActivity(new Intent(this, AboutAppActivity.class));
                break;
            case R.id.menu_find_routes:
                mDrawerLayout.closeDrawer(mRightDrawer);
                //mDrawerLayout.openDrawer(mLeftDrawer);
                break;
//            case R.id.menu_display_bus_stops:
//                item.setChecked(!item.isChecked());
//                showBusStopsForRoute = item.isChecked();
//                SharedPreferences.Editor editor = mSharedPreferences.edit();
//                editor.putBoolean(MapGoogleActivity.KEY_SHOW_BUS_STOPS, showBusStopsForRoute);
//                editor.commit();
//                if (!showBusStopsForRoute) {
//                    busStopsMarkersOverlay.setEnabled(false);
//                } else {
//                    if (busStopsMarkersOverlay.getItems().size() == 0){
//                       // drawRouteBusStops(mSelectedRoute, false);
//                    }
//                    busStopsMarkersOverlay.setEnabled(true);
//                }
//                mMapView.invalidate();
//                break;
//            case android.R.id.home:
//                if (mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
//                    mDrawerLayout.closeDrawer(mLeftDrawer);
//                    return true;
//                }
//                mDrawerLayout.closeDrawer(mRightDrawer);
//                mDrawerLayout.openDrawer(mLeftDrawer);
//                return true;
            case R.id.menu_edit_route:
                startActivity(new Intent(this, RoutesActivity.class));
                break;
//            case R.id.menu_preferences:
//                startActivity(new Intent(this, PrefsActivity.class));
//                break;
//            case R.id.menu_make_complaint:
//                ComplaintActivity.start(this, mSelectedRoute);
//                break;
            case R.id.menu_alarm:
                if (mSelectedRoute == null) {
                    showToast(getString(R.string.first_select_route));
                    return true;
                }
                isAlarmMode = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_welcome_message:
                llWelcomeMessage.setVisibility(View.GONE);
                mDrawerLayout.openDrawer(mRightDrawer);
                break;
            case R.id.btn_show_nearest_bus_stops_for_custom_location:
                isFindRoutesMode = false;
                GeoPoint myLocation = mMyLocationOverlay.getMyLocation();
                showNearestBusStops(myLocation);
                mDrawerLayout.closeDrawer(mLeftDrawer);
                isCustomLocationMode = false;
                break;
//            case R.id.btn_point_from_on_map:
//                isFindRoutesMode = true;
//                currentPointType = PointType.FROM;
//                resetTimersAndClearMap();
//                mDrawerLayout.closeDrawer(mLeftDrawer);
//                // TODO
//                if (mMarkerPointTo != null) {
//                    //setMarkerPointTo(mMarkerPointTo.title, mMarkerPointTo.latitude, mMarkerPointTo.longitude, false);
//                }
//                showToast(getString(R.string.set_point_from));
//                break;
//            case R.id.btn_point_to_on_map:
//                isFindRoutesMode = true;
//                currentPointType = PointType.TO;
//                resetTimersAndClearMap();
//                mDrawerLayout.closeDrawer(mLeftDrawer);
//                 //TODO
//                if (mMarkerPointFrom != null) {
//                    //setMarkerPointFrom(mMarkerPointFrom.title, mMarkerPointFrom.latitude, mMarkerPointFrom.longitude, false);
//                }
//                showToast(getString(R.string.set_point_to));
//                break;
//            case R.id.btn_point_to_clear:
//                etPointTo.setText("");
//                break;
//            case R.id.btn_point_from_clear:
//                etPointFrom.setText("");
//                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.btn_show_nearest_bus_stops_for_custom_location:
                closeInfoWindow();
                resetTimersAndClearMap();
                showToast(getString(R.string.click_on_map));
                isFindRoutesMode = false;
                isCustomLocationMode = true;
                break;
        }
        return true;
    }

    private class RoutesDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            resetFindRouteMode();
//            Route route = (Route) parent.getAdapter().getItem(position);
//            mSelectedRoutes = null;
//            hashMapMarkerBusStops.clear();
//            selectRoute(route, parent.getId() == R.id.lv_routes_menu);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
//        isFindRoutesMode = true;
//        if (marker.getTitle().equals(getString(R.string.from))) {
//            etPointFrom.setText(marker.getPosition().toString());
//            etPointFrom.setText(getString(R.string.point_a));
//        } else if (marker.getTitle().equals(getString(R.string.to))) {
//            etPointTo.setText(getString(R.string.point_b));
//            etPointTo.setText(marker.getPosition().toString());
//        }
        //mDrawerLayout.openDrawer(mLeftDrawer);
    }

   /* @Override
    public void onMapClick(GeoPoint GeoPoint) {
        if (isFindRoutesMode) {
                    *//*Variant #2*//*
            switch (currentPointType) {
                case FROM:

                    setMarkerPointFrom(getString(R.string.from), GeoPoint, true);
                    etPointFrom.setText(getString(R.string.point_a));
                    break;
                case TO:

                    setMarkerPointTo(getString(R.string.to), GeoPoint, true);

                    etPointTo.setText(getString(R.string.point_b));
                    break;
            }
            mDrawerLayout.openDrawer(mLeftDrawer);
            return;
        }
        if (mCustomLocationMarker != null && isMarkerClicked) {
            mCustomLocationMarker.hideInfoWindow();
            isMarkerClicked = false;
            return;
        }
        isMarkerClicked = false;
        if (!isCustomLocationMode) return;
        if (mCustomLocationMarker != null)
            mCustomLocationMarker.remove();
        showNearestBusStops(GeoPoint.latitude, GeoPoint.longitude);
        mCustomLocationMarker = mMapView.addMarker(new MarkerOptions()
                .position(GeoPoint)
                .title(getString(R.string.selected_location))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }*/

    private Timer busTimer, routeStatisticTimer, busesTimer;
    boolean isRouteChanged;
    boolean isMultiSelectRoutes;

    private Route selectRoute(final Route route, boolean removeFindMarkers) {
        hideKeyboard(etSearchRoute);
        if (route == null || (mSelectedRoute != null && route.getId() == mSelectedRoute.getId())) {
            mDrawerLayout.closeDrawer(mRightDrawer);
            mDrawerLayout.closeDrawer(mLeftDrawer);
            isRouteChanged = false;
            return mSelectedRoute;
        }
        closeInfoWindow();
        routesPathOverlay.getItems().clear();
        busesMarkersOverlay.getItems().clear();
        mSelectedRoute = route;
        mSelectedBusStop = null;
        isRouteChanged = true;
        mActionBar.setSubtitle(mSelectedRoute.toString());
        mDrawerLayout.closeDrawer(mRightDrawer);
        mDrawerLayout.closeDrawer(mLeftDrawer);

        if (mSelectedRoute.getBusStops() == null) {
            mSelectedRoute.setBusStops(new BusStopController(MapOsmActivity.this).getRouteBusStops(mSelectedRoute.getServerId()));
            if (mSelectedRoute.linkedRoute != null) {
                mSelectedRoute.linkedRoute.setBusStops(new BusStopController(MapOsmActivity.this).getRouteBusStops(
                        mSelectedRoute.linkedRoute.getServerId()));
            }
        }
        //drawRouteBusStops(mSelectedRoute, true);
        drawRouteLineGoogle(mSelectedRoute, removeFindMarkers);
        updateLastSeenDate(route);
        manualMarkersOverlay.removeAllItems();
        return mSelectedRoute;
    }

    private void updateLastSeenDate(final Route route) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    route.setLastSeenDate(new Date().getTime());
                    mDbHelper.getRouteDao().update(route);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private RoutesAdapter getAdapter() {
        if (mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
            return mFoundRoutesAdapter;
        }
        switch (mViewPager.getCurrentItem()) {
            case 0:
                return mAdapter;
            case 1:
                return mFavoriteAdapter;
            case 2:
                return mHistoryAdapter;
            default:
                return mAdapter;
        }
    }

    private ListView getListView() {
        if (mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
            return listViewFoundRoutes;
        }
        switch (mViewPager.getCurrentItem()) {
            case 0:
                return listViewRoutes;
            case 1:
                return listViewFavoriteRoutes;
            case 2:
                return listViewHistoryRoutes;
            default:
                return listViewRoutes;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mActionBar!=null)
            mActionBar.setTitle(mTitle);
    }

    @Override
    public void setTemperature(int value) {
        TextView tvTemp = (TextView) findViewById(R.id.tv_weather_term);
        tvTemp.setText(String.format(getString(R.string.temperature), value));
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        //mDrawerToggle.onConfigurationChanged(newConfig);
        // из-за флага  android:configChanges="screenSize" при повороте экрана не пересоздается вьюха,
        // поэтому делаю назначение "веса" у индикатора вручную
        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.page_indicator_weight, typedValue, true);
        float weight = typedValue.getFloat();
        float slideMenuWidth = getResources().getDimension(R.dimen.slide_menu_width);

        RelativeLayout leftDrawer = (RelativeLayout) findViewById(R.id.left_drawer);
        RelativeLayout rightDrawer = (RelativeLayout) findViewById(R.id.right_drawer);

        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) leftDrawer.getLayoutParams();
        DrawerLayout.LayoutParams params2 = (android.support.v4.widget.DrawerLayout.LayoutParams) rightDrawer.getLayoutParams();
        params.width = (int) slideMenuWidth;
        params2.width = (int) slideMenuWidth;
        leftDrawer.setLayoutParams(params);
        rightDrawer.setLayoutParams(params2);

        findViewById(R.id.indicator).setLayoutParams(new TableLayout.LayoutParams(ViewPager.LayoutParams.WRAP_CONTENT, 0, weight));
    }

    @Override
    public void onStart() {
        super.onStart();
        //Crashlytics.start(this);
        //Fabric.with(this, new Crashlytics());
        //EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Crashlytics.start(this);
        //Fabric.with(this, new Crashlytics());
        //EasyTracker.getInstance().activityStop(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DBHelper.release();
        if (mLoadDataAsyncTask != null && mLoadDataAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadDataAsyncTask.cancel(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeInfoWindow();
        stopBusTimer();
        stopBusesTimer();
        stopRoutesStatisticsTimer();
        if (needFinishWhenOnPause && isSessionFromWidget) {
            finish();
        }
        needFinishWhenOnPause = true;

        SharedPreferences.Editor editor = getSharedPreferences(MapGoogleActivity.MAIN_PREFS, MODE_PRIVATE).edit();
        editor.putInt(MapGoogleActivity.KEY_LAST_OPENED_TAB, mViewPager.getCurrentItem());
        editor.commit();

        updateWidgets();

        mMyLocationOverlay.disableFollowLocation();
        mMyLocationOverlay.disableMyLocation();
    }

    void updateWidgets() {
        Intent intent = new Intent(this, MyProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), MyProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsRouteStatisticEnabled = mSharedPreferences.getBoolean(getString(R.string.key_route_statistic), false);

        if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
            startRoutesStatisticsTimer();
        } else if (!mDrawerLayout.isDrawerOpen(mRightDrawer) && !mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
            isRouteChanged = true;
            startBusTimer();
            startBusesTimer();
        }

        mMyLocationOverlay.enableMyLocation();
    }

    private void startBusTimer() {
        isShowBusStopsMode = false;
        stopBusTimer();
        stopBusesTimer();
        if (mSelectedRoute == null) {
            Log.v(LOG_TAG, "mSelectedRoute == null, timer not started");
            return;
        }
        Log.v(LOG_TAG, "start BusTimer");
        busTimer = new Timer();
        busTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod(mSelectedRoute);
            }
        }, 0, Consts.BUS_TIMER_INTERVAL);
    }

    private void stopBusTimer() {
        if (busTimer != null) {
            busTimer.cancel();
            busTimer = null;
            Log.v(LOG_TAG, "stop BusTimer");
        }
    }

    private void startBusesTimer() {
        isShowBusStopsMode = false;
        stopBusesTimer();
        if (mSelectedRoutes == null || mSelectedRoutes.size() == 0) {
            Log.v(LOG_TAG, "mSelectedRoutes == null || mSelectedRoutes.size() == 0, timer not started");
            return;
        }
        Log.v(LOG_TAG, "start busesTimer");
        busesTimer = new Timer();
        busesTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod(mSelectedRoutes);
            }
        }, 0, Consts.BUS_TIMER_INTERVAL);
    }

    private void stopBusesTimer() {
        if (busesTimer != null) {
            busesTimer.cancel();
            busesTimer = null;
            Log.v(LOG_TAG, "stop busesTimer");
        }
    }

    private void startRoutesStatisticsTimer() {
        stopRoutesStatisticsTimer();
        Log.v(LOG_TAG, "start RoutesStatisticsTimer");
        routeStatisticTimer = new Timer();
        routeStatisticTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                RoutesStatisticTimerMethod();
            }
        }, 0, Consts.ROUTES_STATISTIC_TIMER_INTERVAL);
    }

    private void stopRoutesStatisticsTimer() {
        if (routeStatisticTimer != null) {
            routeStatisticTimer.cancel();
            routeStatisticTimer = null;
            Log.v(LOG_TAG, "stop RoutesStatisticsTimer");
        }
    }

    private void TimerMethod(Route route) {
        try {
            if (route == null) {
                stopBusTimer();
                return;
            }
           // mBuses = BusController.getRouteBuses(route);
            mBuses = BusController.getRouteBusesDaniyar(route);
            this.runOnUiThread(BusTimerTick);
        } catch (HttpException e) {
            e.printStackTrace();
            mBuses = null;
            this.runOnUiThread(new ParameterRunnable(R.string.no_internet_connection));
        } catch (IOException e) {
            e.printStackTrace();
            mBuses = null;
            this.runOnUiThread(new ParameterRunnable(R.string.no_internet_connection));
        } catch (JSONException e) {
            e.printStackTrace();
            mBuses = null;
            this.runOnUiThread(new ParameterRunnable(R.string.server_request_error));
        }
    }

    public class ParameterRunnable implements Runnable {
        String mMessage;

        public ParameterRunnable(String message) {
            this.mMessage = message;
        }

        public ParameterRunnable(int stringId) {
            this.mMessage = getString(stringId);
        }

        @Override
        public void run() {
            setErrorTextMessage(mMessage);
        }
    }

    private void setErrorTextMessage(String message) {
        if (tvInternetStatus == null) return;
        tvInternetStatus.setText(message);
        tvInternetStatus.setVisibility(View.VISIBLE);
    }

    private void TimerMethod(List<Route> routes) {
        try {
            mBuses = BusController.getBusInfoForRoutes(routes);

        } catch (HttpException e) {
            e.printStackTrace();
            mBuses = null;
        } catch (IOException e) {
            e.printStackTrace();
            mBuses = null;
        } catch (JSONException e) {
            e.printStackTrace();
            mBuses = null;
        }
        this.runOnUiThread(BusTimerTick);
    }

    private void RoutesStatisticTimerMethod() {
        try {
            statisticHashMap = !mIsRouteStatisticEnabled ? null : mRouteController.loadRoutesStatisticsFromServer();
        } catch (HttpException e) {
            e.printStackTrace();
            statisticHashMap = null;
        } catch (IOException e) {
            e.printStackTrace();
            statisticHashMap = null;
        } catch (JSONException e) {
            e.printStackTrace();
            statisticHashMap = null;
        }
        this.runOnUiThread(RoutesStatisticTimerTick);
    }

    private Runnable BusTimerTick = new Runnable() {
        public void run() {
            Log.v(LOG_TAG, String.format("bus timer tick. Selected route is %s",
                    mSelectedRoute != null ? mSelectedRoute.toString() : "null"));
            drawBusesGoogle(mBuses);
        }
    };

    private Runnable RoutesStatisticTimerTick = new Runnable() {
        public void run() {
            if (mAdapter == null)
                return;
            if (!mDrawerLayout.isDrawerOpen(mRightDrawer)) {
                Log.v(LOG_TAG, "поздний ответ. mRightDrawer is closed");
                return;
            }
            Log.v(LOG_TAG, "routes statistic timer tick");
            mAdapter.setRoutesStatistic(statisticHashMap);
        }
    };

    // Рисую линию маршрута
    private void drawRouteLineGoogle(Route route, boolean removeFindMarkers) {
        isCustomLocationMode = false;
        routesPathOverlay.getItems().clear();
        PathOverlay pathOverlay = new PathOverlay(Color.BLUE, this);
        pathOverlay.getPaint().setStrokeWidth(3);
        for (GeoPoint point : route.getTrackPointsOsm()) {
            pathOverlay.addPoint(point);
        }
        routesPathOverlay.add(pathOverlay);

        pathOverlay = new PathOverlay(Color.BLUE, this);
        pathOverlay.getPaint().setStrokeWidth(3);
        for (GeoPoint point : route.getTrackPointsForInverseCourseOsm()) {
            pathOverlay.addPoint(point);
        }
        routesPathOverlay.add(pathOverlay);

        mMapView.invalidate();
        zoomMapForPoints(route.getTrackPointsOsm());

        /*if (removeFindMarkers) {
            if (mMarkerPointFrom != null) {
                mMarkerPointFrom = mMapView.addMarker(new MarkerOptions()
                        .position(mMarkerPointFrom.getPosition())
                        .title(getString(R.string.from))
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
            if (mMarkerPointTo != null) {
                mMarkerPointTo = mMapView.addMarker(new MarkerOptions()
                        .position(mMarkerPointTo.getPosition())
                        .title(getString(R.string.to))
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }*/
    }

    ArrayList<Polyline> polylines;
    final int[] colors = new int[]{Color.RED, Color.BLUE, Color.MAGENTA};
    HashMap<Integer, Integer> hashMapRouteLineColors;

    // Рисую линии маршрутов
    private void drawRoutes(List<Route> routes) {
        mSelectedBusStop = null;
        mSelectedRoute = null;
        routesPathOverlay.getItems().clear();
        busStopsMarkersOverlay.getItems().clear();
        busesMarkersOverlay.getItems().clear();
        //mMapView.getOverlays().clear();
        isCustomLocationMode = false;
        StringBuilder sb = new StringBuilder(getString(R.string.routes) + ": ");
        hashMapRouteLineColors = new HashMap<Integer, Integer>();
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            // Условие выполняется, когда данные удаляются, а виджет остается на рабочем столе и его запускают
            if (route == null) {
                startActivity(new Intent(this, SplashActivity.class));
                finish();
                return;
            }
            sb.append(route.getNumber());
            if (routes.size() > i + 1)
                sb.append(", ");

            PathOverlay pathOverlay = new PathOverlay(colors[i], this);
            pathOverlay.getPaint().setStrokeWidth(3);
            for (GeoPoint point : route.getTrackPointsOsm()) {
                pathOverlay.addPoint(point);
            }
            routesPathOverlay.add(pathOverlay);
            updateLastSeenDate(route);
            hashMapRouteLineColors.put(route.getNumber(), colors[i]);
        }
        mActionBar.setTitle(getString(R.string.app_name));
        mActionBar.setSubtitle(sb.toString());
        setDefaultCameraPosition();
        startBusesTimer();
    }

    private void zoomMapForPoints(List<GeoPoint> points) {
        GeoPoint firstPoint = points.get(0);
        GeoPoint lastPoint = points.get(points.size() - 1);

        BoundingBoxE6 boundingBox = new BoundingBoxE6(lastPoint.getLatitudeE6(), lastPoint.getLongitudeE6(),
                firstPoint.getLatitudeE6(), firstPoint.getLongitudeE6());
        mMapView.zoomToBoundingBox(boundingBox);
    }

    private void zoomMapForBusStops(List<BusStop> points) {
        int minLat = Integer.MAX_VALUE;
        int maxLat = Integer.MIN_VALUE;
        int minLong = Integer.MAX_VALUE;
        int maxLong = Integer.MIN_VALUE;
        for (BusStop busStop : points) {
            GeoPoint point = busStop.getPointOsm();
            if (point.getLatitudeE6() < minLat)
                minLat = point.getLatitudeE6();
            if (point.getLatitudeE6() > maxLat)
                maxLat = point.getLatitudeE6();
            if (point.getLongitudeE6() < minLong)
                minLong = point.getLongitudeE6();
            if (point.getLongitudeE6() > maxLong)
                maxLong = point.getLongitudeE6();
        }

        BoundingBoxE6 boundingBox = new BoundingBoxE6(maxLat, maxLong, minLat, minLong);
        mMapView.zoomToBoundingBox(boundingBox);
    }

    private void drawBusesGoogle(List<Bus> buses) {
        /*
            Условие поставлено на случай, если пользователь быстро выбрал маршрут, а затем
            выбрал остановку из левого бокового меню, таймер не успевал остановиться и отрисовывал автобусы без маршрута
         */
        if (mDrawerLayout.isDrawerOpen(mLeftDrawer) || mDrawerLayout.isDrawerOpen(mRightDrawer) || isShowBusStopsMode || mSelectedBusStop != null) {
            return;
        }
        //isCustomLocationMode = false;
        if (buses == null) {
            if (markers != null) {
                // TODO
                /*for (Marker marker : markers) {
                    marker.remove();
                }*/
            } else {
               // Crashlytics.log("markers == null in drawBusesGoogle(ArrayList<Bus> buses)");
            }
            tvInternetStatus.setVisibility(View.VISIBLE);
            return;
        }
        tvInternetStatus.setVisibility(View.GONE);
        if (!isRouteChanged && busesMarkersOverlay.getItems().size() > 0) {
            /*  Метод setIcon() устанавиливает isInfoWindowShown() = false поэтому, при обновлении,
                автобусов infoWindow закрывается. Для этого завел переменную, в которую сохраняю состояние окна маркера
                до установки новой иконки
            */
            boolean isInfoWindowShown;
            for (Bus bus : buses) {
                if (hashMapBusMarkers.containsKey(bus.getServerId())) {
                    int routeLineColor = Color.BLUE;
                    if (mSelectedRoute == null && hashMapRouteLineColors != null && hashMapRouteLineColors.containsKey(bus.getRouteNumber()))
                        routeLineColor = hashMapRouteLineColors.get(bus.getRouteNumber());
                    Marker marker = hashMapBusMarkers.get(bus.getServerId());
                    isInfoWindowShown = lastSelectedMarker != null && lastSelectedMarker.equals(marker);
                    marker.setPosition(bus.getPointOsm());
                    //marker.setSnippet(String.format(getString(R.string.bus_marker_text_2), bus.getRouteNumber(), new Date(bus.getTime()).toString()));
                    Date currentDate = new Date(bus.getTime());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    marker.setSnippet("Маршрут №" + bus.getRouteNumber() + "\n Данные получены: " + dateFormat.format(currentDate));
                    //Log.d("TIME TIME", new Date(bus.getTime()).toString());
                    marker.setIcon(getResources().getDrawable(Bus.getDirectionIcon(MapOsmActivity.this, bus.getZ(), routeLineColor)));
                    // для обновления сниппета переоткрываю всплывающее окно
                    if (isInfoWindowShown) {
                        marker.getInfoWindow().open(marker, marker.getPosition(), 0, 0);
                    }
                    //alarm(bus);
                }
            }
            mMapView.invalidate();
            return;
        }
        isRouteChanged = false;
        if (buses == null)
            return;
        hashMapBusMarkers = new HashMap<Long, Marker>();
        busesMarkersOverlay.getItems().clear();
        InfoWindow infoWindow = new BusInfoWindow(mMapView, MapOsmActivity.this);
        for (Bus bus : buses) {
            int routeLineColor = Color.BLUE;
            if (mSelectedRoute == null && hashMapRouteLineColors != null && hashMapRouteLineColors.containsKey(bus.getRouteNumber()))
                routeLineColor = hashMapRouteLineColors.get(bus.getRouteNumber());
            Marker marker = new Marker(mMapView);
            marker.setTitle(String.format("№ %s", bus.getName()));
           // marker.setSubDescription(String.valueOf(String.format(getString(R.string.bus_marker_text_2), bus.getRouteNumber(), bus.getTime("HH:mm:ss"))));
            Date currentDate = new Date(bus.getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            marker.setSubDescription("Маршрут №" + bus.getRouteNumber() + "\n Данные получены: " + dateFormat.format(currentDate));

            marker.setSnippet(String.valueOf(bus.getServerId()));
            marker.setIcon(getResources().getDrawable(Bus.getDirectionIcon(MapOsmActivity.this, bus.getZ(), routeLineColor)));
            marker.setInfoWindow(infoWindow);
            marker.setPosition(bus.getPointOsm());
            marker.setOnMarkerClickListener(this);
            hashMapBusMarkers.put(bus.getServerId(), marker);
            busesMarkersOverlay.add(marker);
        }
        if (buses.size() == 0) {
            // TODO: решить в каких случаях сервер не вернул данные
            //setErrorTextMessage(getString(R.string.server_not_available));
        } else {
            tvInternetStatus.setVisibility(View.GONE);
        }
        mMapView.invalidate();
    }

    double prevDistance = 0;
    List<Bus> ignoreBusList = new ArrayList<Bus>();

    void alarm(Bus bus) {
        if (mAlarmBusStop != null) {
            if (ignoreBusList.contains(bus)) {
                return;
            }
            double distance = BusStopController.calcDistance(mAlarmBusStop.getPointGoogle().latitude, mAlarmBusStop.getPointGoogle().longitude,
                    bus.getPointGoogle().latitude, bus.getPointGoogle().longitude);
            if (distance <= 500) {
                if (distance > prevDistance && prevDistance != 0) {
                    ignoreBusList.add(bus);
                    prevDistance = 0;
                }
                prevDistance = distance;
                showToast(bus.getName() + ". " + String.valueOf(distance) + ". " + String.valueOf(ignoreBusList.contains(bus)));
            }
        }
    }

    private void selectItemOnListView(ListView listView, RoutesAdapter adapter) {
        List<Route> routes = adapter.getData();
        int selectedPosition = listView.getCheckedItemPosition();
        listView.setItemChecked(selectedPosition, false);
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).equals(mSelectedRoute)) {
                listView.setOnItemClickListener(null);
                listView.performItemClick(null, i, 0);
                listView.setOnItemClickListener(listener);
            }
        }
    }

    private void drawRouteBusStops(Route route, boolean needClearMap) {
        if (route == null)
            return;
        busStopsMarkersOverlay.getItems().clear();
        hashMapMarkerBusStops.clear();
        if (!showBusStopsForRoute)
            return;
        drawBusStops(route);
        if (route.getLinkedRoute() != null) {
            drawBusStops(route.getLinkedRoute());
        }
    }

    private void drawBusStops(Route route) {
        List<BusStop> busStops = route.getBusStops();
        if (busStops == null)
            return;
        InfoWindow infoWindow = new BusStopInfoWindow(mMapView, MapOsmActivity.this);
        for (int i = 0; i < busStops.size(); i++) {
            int resourceId = R.drawable.road_sign;
            BusStop busStop = busStops.get(i);
          /*  if (i == 0) {
                resourceId = R.drawable.road_sign_start;
            } else if (i == busStops.size() - 1) {
                resourceId = R.drawable.road_sign_start;
            }*/

            Marker marker = new Marker(mMapView);
            marker.setTitle(busStop.getName());
            marker.setSubDescription(busStop.getDescription());
            marker.setSnippet(String.valueOf(busStop.getServerId()));
            marker.setIcon(getResources().getDrawable(resourceId));
            marker.setInfoWindow(infoWindow);
            marker.setPosition(busStop.getPointOsm());
            marker.setOnMarkerClickListener(this);
            busStopsMarkersOverlay.add(marker);
        }
        mMapView.invalidate();
    }

    private void showNearestBusStops(GeoPoint point) {
        resetTimersAndClearMap();
        if (point != null && !point.equals(mMyLocationOverlay.getMyLocation())) {
            OverlayItem ovm = new OverlayItem(getString(R.string.selected_location), "", point);
            ovm.setMarker(getResources().getDrawable(R.drawable.road_sign_start));
            manualMarkersOverlay.addItem(ovm);
        }
        isShowBusStopsMode = true;
        tvInternetStatus.setVisibility(View.GONE);
        mSelectedRoute = null;
        stopBusTimer();
        stopBusesTimer();
        mActionBar.setSubtitle("");
        List<BusStop> nearestBusStops = BusStop.getNearestBusStops(mDbHelper, point);
        if (nearestBusStops == null) {
            showToast(getString(R.string.your_location_undefined));
            clearMap();
            return;
        }
        if (nearestBusStops.size() == 0) {
            showToast(getString(R.string.nearest_bus_stops_not_found));
            // TODO
            //mMapView.getOverlays().clear();
            return;
        }
        drawBusStops(nearestBusStops);
    }

    private void drawBusStops(List<BusStop> busStops) {
        if (busStops == null)
            return;
        closeInfoWindow();
        busStopsMarkersOverlay.getItems().clear();
        routesPathOverlay.getItems().clear();
        hashMapMarkerBusStops.clear();
        InfoWindow infoWindow = new BusStopInfoWindow(mMapView, MapOsmActivity.this);
        for (int i = 0; i < busStops.size(); i++) {
            BusStop busStop = busStops.get(i);

            Marker marker = new Marker(mMapView);
            marker.setTitle(busStop.getName());
            marker.setSubDescription(busStop.getDescription());
            marker.setSnippet(String.valueOf(busStop.getServerId()));
            marker.setIcon(getResources().getDrawable(R.drawable.road_sign));
            marker.setInfoWindow(infoWindow);
            marker.setPosition(busStop.getPointOsm());
            marker.setOnMarkerClickListener(this);
            busStopsMarkersOverlay.add(marker);
        }
        mActionBar.setSubtitle(R.string.nearest_bus_stops);
        isCustomLocationMode = false;
        mMapView.invalidate();
        zoomMapForBusStops(busStops);
    }

    Toast mToast;

    private void showToast(String text) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void hideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private class LoadDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private List<BusStop> busStops;
        private List<Route> historyRoutes;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            busStops = new ArrayList<BusStop>();
            MapOsmActivity.this.setProgressBarIndeterminate(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            MapOsmActivity.this.setProgressBarIndeterminate(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            busStops = BusStop.getAll(mDbHelper);
            mRoutes = Route.getAllGroupByNumber(mDbHelper);
            historyRoutes = Route.getHistoryRoutes(mDbHelper);
            mBusStopsPointFromAdapter = new BusStopsAdapter(MapOsmActivity.this, R.layout.bus_stops_list_item, busStops);
            mBusStopsPointToAdapter = new BusStopsAdapter(MapOsmActivity.this, R.layout.bus_stops_list_item, busStops);
            mAdapter = new RoutesAdapter(MapOsmActivity.this, mRoutes, RoutesAdapter.TYPE.All);
            mFavoriteAdapter = new RoutesAdapter(MapOsmActivity.this, Route.getFavorites(mDbHelper), RoutesAdapter.TYPE.Favorites);
            mHistoryAdapter = new RoutesAdapter(MapOsmActivity.this, historyRoutes, RoutesAdapter.TYPE.History);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
//            etPointFrom.setAdapter(mBusStopsPointFromAdapter);
//            etPointTo.setAdapter(mBusStopsPointToAdapter);

            listViewRoutes.setAdapter(mAdapter);
            listViewFavoriteRoutes.setAdapter(mFavoriteAdapter);
            listViewHistoryRoutes.setAdapter(mHistoryAdapter);
            MapOsmActivity.this.setProgressBarIndeterminate(false);
        }
    }

    private void initDialog() {
        notHaveGmsDialog = new AlertDialog.Builder(MapOsmActivity.this);
        notHaveGmsDialog.setTitle(getString(R.string.attention));  // заголовок
        notHaveGmsDialog.setMessage(getString(R.string.you_not_have_gms)); // сообщение
        notHaveGmsDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                String appPackageName = "com.google.android.gms";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                finish();
            }
        });
        notHaveGmsDialog.setCancelable(false);
    }

    private void initVoteAppDialog() {
        voteAppDialog = new AlertDialog.Builder(MapOsmActivity.this);
        voteAppDialog.setTitle(getString(R.string.attention));
        voteAppDialog.setMessage(getString(R.string.vote_application));
        voteAppDialog.setPositiveButton(R.string.vote, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                SharedPreferences.Editor editor = getSharedPreferences(MapGoogleActivity.MAIN_PREFS, MODE_PRIVATE).edit();
                editor.putBoolean(MapGoogleActivity.KEY_NOT_SHOW_VOTE_DIALOG, true);
                editor.commit();
                dialog.dismiss();
            }
        });
        voteAppDialog.setNegativeButton(R.string.not_ask, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                SharedPreferences.Editor editor = getSharedPreferences(MapGoogleActivity.MAIN_PREFS, MODE_PRIVATE).edit();
                editor.putBoolean(MapGoogleActivity.KEY_NOT_SHOW_VOTE_DIALOG, true);
                editor.commit();
                dialog.dismiss();
            }
        });
        voteAppDialog.setNeutralButton(R.string.later, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
            }
        });
        voteAppDialog.setCancelable(true);
    }

    public class ModeCallback implements AbsListView.MultiChoiceModeListener {
        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) {
            android.view.MenuInflater inflater = MapOsmActivity.this.getMenuInflater();
            inflater.inflate(R.menu.several_routes_menu, menu);
            mode.setTitle(getString(R.string.select_several_routes));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode actionMode, android.view.Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode actionMode) {

        }

        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) {
            switch (item.getItemId()) {
                case R.id.show_routes_on_map:
                    isRouteChanged = true;
                    mSelectedRoute = null;
                    //SparseBooleanArray array = getListView().getCheckedItemPositions();
                    isMultiSelectRoutes = true;
//                    ArrayList<Route> selectedRoutes = new ArrayList<Route>();
//                    for (int i = 0; i < array.size(); i++) {
//                        if (array.valueAt(i))
//                            selectedRoutes.add(getAdapter().getItem(array.keyAt(i)));
//                    }
//                    mSelectedRoutes = selectedRoutes;
//                    if (selectedRoutes.size() == 1) {
//                        selectRoute(selectedRoutes.get(0), false);
//                    } else {
//                        drawRoutes(selectedRoutes);
//                    }
                    isFindRoutesMode = false;
                    isMultiSelectRoutes = false;
                    mDrawerLayout.closeDrawer(mRightDrawer);
                    mDrawerLayout.closeDrawer(mLeftDrawer);
                    mode.finish();
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode,
                                              int position, long id, boolean checked) {
//            if (getListView().getCheckedItemCount() > Consts.MAX_ROUTES_ON_MAP) {
//                getListView().setItemChecked(position, !checked);
//                showToast(getString(R.string.max_select_routes));
//            }
        }
    }

    private void checkStartsCount() {
        int startsCount;
        SharedPreferences sp = getSharedPreferences(MapGoogleActivity.MAIN_PREFS, MODE_PRIVATE);
        startsCount = sp.getInt(MapGoogleActivity.KEY_STARTS_COUNT, 0) + 1;
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(MapGoogleActivity.KEY_STARTS_COUNT, startsCount);
        editor.commit();
        if (startsCount % 10 == 0 && !sp.getBoolean(MapGoogleActivity.KEY_NOT_SHOW_VOTE_DIALOG, false)) {
            voteAppDialog.show();
        }
    }

    void findRoutes() {
        if (mMarkerPointFrom == null && mMarkerPointTo == null) {
            listViewFoundRoutes.setAdapter(null);
            return;
        }
        if (mMarkerPointFrom == null || mMarkerPointTo == null) {
            return;
        }
        List<Route> routes = Route.findRoutesForPoints(DBHelper.getHelper(), mMarkerPointFrom.getPosition(), mMarkerPointTo.getPosition());
        mFoundRoutesAdapter = new RoutesAdapter(MapOsmActivity.this, routes, RoutesAdapter.TYPE.History);
        listViewFoundRoutes.setAdapter(mFoundRoutesAdapter);
        listViewFoundRoutes.setOnItemClickListener(listener);
    }

    void setMarkerPointFrom(String title, double lat, double lon, boolean isNeedFind) {
        setMarkerPointFrom(title, new GeoPoint(lat, lon), isNeedFind);
    }

    void setMarkerPointFrom(String title, GeoPoint position, boolean isNeedFind) {
        resetTimersAndClearMap();
        if (mMarkerPointFrom != null) {
            mMarkerPointFrom.remove(mMapView);
        }
        mMarkerPointFrom = new Marker(mMapView);
        mMarkerPointFrom.setTitle(getString(R.string.from));
        mMarkerPointFrom.setIcon(getResources().getDrawable(R.drawable.road_sign_start));
        mMarkerPointFrom.setPosition(position);
        // mMarkerPointFrom.setOnMarkerClickListener(this);
        mMarkerPointFrom.setDraggable(true);
        mMarkerPointFrom.setOnMarkerDragListener(this);
        mMapView.getOverlays().add(mMarkerPointFrom);
        mMapView.invalidate();
        //if (isNeedFind)
            //findRoutes();
    }

    void setMarkerPointTo(String title, double lat, double lon, boolean isNeedFind) {
        setMarkerPointTo(title, new GeoPoint(lat, lon), isNeedFind);
    }

    void setMarkerPointTo(String title, GeoPoint position, boolean isNeedFind) {
        // TODO
        //resetTimersAndClearMap();
        if (mMarkerPointTo != null) {
            mMarkerPointTo.remove(mMapView);
        }
        mMarkerPointTo = new Marker(mMapView);
        mMarkerPointTo.setTitle(getString(R.string.to));
        mMarkerPointTo.setIcon(getResources().getDrawable(R.drawable.road_sign_start));
        mMarkerPointTo.setPosition(position);
        // mMarkerPointTo.setOnMarkerClickListener(this);
        mMarkerPointTo.setDraggable(true);
        mMarkerPointTo.setOnMarkerDragListener(this);
        mMapView.getOverlays().add(mMarkerPointTo);
        mMapView.invalidate();
//        if (isNeedFind)
//            findRoutes();
    }

    void resetFindRouteMode() {
        if (tvInternetStatus != null) tvInternetStatus.setVisibility(View.GONE);
        isFindRoutesMode = false;
        if (mToast != null) mToast.cancel();
    }

    void resetTimersAndClearMap() {
        resetTimers();
        clearMap();
    }

    void resetTimers() {
        mSelectedRoute = null;
        mSelectedRoutes = null;
        mBuses = null;
        stopBusTimer();
        stopBusesTimer();
        stopRoutesStatisticsTimer();
    }

    void closeInfoWindow() {
        if (lastSelectedMarker != null) {
            lastSelectedMarker.hideInfoWindow();
            lastSelectedMarker = null;
        }
    }

    void clearMap() {
        busStopsMarkersOverlay.getItems().clear();
        busesMarkersOverlay.getItems().clear();
        routesPathOverlay.getItems().clear();
        manualMarkersOverlay.removeAllItems();
        mMapView.invalidate();
    }
}