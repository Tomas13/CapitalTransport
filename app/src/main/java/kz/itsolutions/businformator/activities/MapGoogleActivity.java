package kz.itsolutions.businformator.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.http.HttpException;
import org.json.JSONException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.itsolutions.businformator.AstanaBusApplication;
import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.AtoBAdapter;
import kz.itsolutions.businformator.adapters.BusStopsAdapter;
import kz.itsolutions.businformator.adapters.LeftDrawerAdapter2;
import kz.itsolutions.businformator.adapters.MyPagerAdapter;
import kz.itsolutions.businformator.adapters.RoutesAdapter;
import kz.itsolutions.businformator.controllers.BusController;
import kz.itsolutions.businformator.controllers.BusStopController;
import kz.itsolutions.businformator.controllers.RouteController;
import kz.itsolutions.businformator.db.DBHelper;
import kz.itsolutions.businformator.fragments.AboutAppFragment;
import kz.itsolutions.businformator.fragments.ComplaintsFragment;
import kz.itsolutions.businformator.fragments.ContactCentrFragment;
import kz.itsolutions.businformator.fragments.NewsFragment;
import kz.itsolutions.businformator.fragments.ScheduleInsideFragment;
import kz.itsolutions.businformator.fragments.SchedulesFragment;
import kz.itsolutions.businformator.fragments.TransportFragment;
import kz.itsolutions.businformator.model.Bus;
import kz.itsolutions.businformator.model.BusStop;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.model.RouteStatistic;
import kz.itsolutions.businformator.utils.Consts;
import kz.itsolutions.businformator.widgets.MyProvider;
import kz.itsolutions.businformator.widgets.SingleWidget;
import kz.itsolutions.businformator.widgets.tabPageIndicator.TabPageIndicator;

import static kz.itsolutions.businformator.activities.Methods.getCurrentFragment;
import static kz.itsolutions.businformator.activities.Methods.removeFragments;
import static kz.itsolutions.businformator.activities.Methods.showToast;
import static kz.itsolutions.businformator.utils.Consts.KEY_IS_SHOWN_SEARCH_MENU;
import static kz.itsolutions.businformator.utils.Consts.KEY_LAST_OPENED_TAB;
import static kz.itsolutions.businformator.utils.Consts.KEY_NOT_SHOW_VOTE_DIALOG;
import static kz.itsolutions.businformator.utils.Consts.KEY_SELECTED_ROUTE_SERVER_ID;
import static kz.itsolutions.businformator.utils.Consts.KEY_SHOW_BUS_STOPS;
import static kz.itsolutions.businformator.utils.Consts.KEY_SHOW_WELCOME_MESSAGE;
import static kz.itsolutions.businformator.utils.Consts.KEY_STARTS_COUNT;
import static kz.itsolutions.businformator.utils.Consts.LOG_TAG;
import static kz.itsolutions.businformator.utils.Consts.MAIN_PREFS;
import static kz.itsolutions.businformator.utils.Consts.REQUEST_FORECAST_CODE;
import static kz.itsolutions.businformator.utils.Consts.REQUEST_LOCATION;

public class MapGoogleActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnLongClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraChangeListener, ActivityCompat.OnRequestPermissionsResultCallback {

    ArrayList<Route> severalRoutes = new ArrayList<>(); // for drawing bus stops for several routes
    Route routeForZoom = null; //route for drawing bus stops
    Menu menu1;

    boolean isRoutesMenu = true;

    float maxZoom = 15.0f;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.btn_show_nearest_bus_stops_for_custom_location)
    ImageButton btnShowNearestBusStops;
    @BindView(R.id.search_a_b)
    ImageButton fabSearchAtoB;
    @BindView(R.id.close_toolbar_btn)
    ImageButton closeToolbar;
    @BindView(R.id.recyclerview_map)
    RecyclerView recyclerViewMap;
    @BindView(R.id.point_a_btn)
    Button pointABtn;
    @BindView(R.id.point_b_btn)
    Button pointBBtn;
    @BindView(R.id.linear_a_b_btns)
    LinearLayout linearLayoutAtoB;
    @BindView(R.id.bottom_toolbar)
    FrameLayout bottomToolbar;
    @BindView(R.id.fab_map)
    ImageButton fabMain;
    @BindView(R.id.tv_internet_status)
    TextView tvInternetStatus;
    @BindView(R.id.ll_welcome_message)
    LinearLayout llWelcomeMessage;
    @BindView(R.id.left_drawer)
    RelativeLayout mLeftDrawer;
    @BindView(R.id.et_search_route)
    EditText etSearchRoute;
    @BindView(R.id.pager)
    ViewPager mViewPager;
    @BindView(R.id.indicator)
    TabPageIndicator pageIndicator;
    @BindView(R.id.right_drawer)
    RelativeLayout mRightDrawer;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;


    @BindView(R.id.lv_routes_menu)
    ListView listviewLeftMenu;

    ActionBar mActionBar;
    View busStopInfoWindowView, busInfoWindowView;
    ListView listViewRoutes, listViewFavoriteRoutes, listViewHistoryRoutes;
    TextView tvBusStopTitle, tvBusStopDescription, tvBusTitle, tvBusDescription;
    SupportMapFragment mMapFragment;
    ActionBarDrawerToggle mDrawerToggle;


    DBHelper mDbHelper;
    SharedPreferences mSharedPreferences;
    boolean mIsRouteStatisticEnabled = false;

    GoogleMap mMap;
    Polyline line;
    Marker mCustomLocationMarker, mMarkerPointFrom, mMarkerPointTo;

    private CharSequence mTitle;
    LoadDataAsyncTask mLoadDataAsyncTask;

    RoutesAdapter mAdapter, mHistoryAdapter, mFavoriteAdapter;
    RouteController mRouteController;
    BusStopController mBusStopController;
    List<Route> mRoutes, mSelectedRoutes;
    List<Bus> mBuses;
    Route mSelectedRoute;
    BusStop mSelectedBusStop, mAlarmBusStop;
    HashMap<Integer, RouteStatistic> statisticHashMap;
    HashMap<Marker, BusStop> hashMapMarkerBusStops;
    ArrayList<Marker> markers;
    HashMap<Long, Marker> hashMapBusMarkers;

    BusStopsAdapter mBusStopsPointToAdapter, mBusStopsPointFromAdapter;

    MyPagerAdapter mPagerAdapter;

    RoutesDrawerItemClickListener listener;
    boolean isMarkerClicked, isSessionFromWidget, needFinishWhenOnPause, isFindRoutesMode, isCustomLocationMode,
            isShowBusStopsMode, showBusStopsForRoute;
    AlertDialog.Builder notHaveGmsDialog, voteAppDialog;

    public enum PointType {FROM, TO}

    private PointType currentPointType;
    AtoBAdapter atoBAdapter;

    Tracker mTracker;
    private Fragment fragment, scheduleFragment, newsFragment, scheduleInsideFragment,
            transportFragment, contactCentrFragment, complaintsFragment;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    String packageApp = "kz.itsolutions.businformator";
                    Intent launchIntent = getApplicationContext()
                            .getPackageManager().getLaunchIntentForPackage(packageApp);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(launchIntent);

                } else {

                    Snackbar.make(findViewById(R.id.container),
                            "Вы можете открыть доступ в настройках или при следующем запуске приложения",
                            Snackbar.LENGTH_SHORT).show();

                }
                return;
            }
            default:
                return;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //get permissions
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
        }

        MapGooglePresenter presenter = new MapGooglePresenter();

        SharedPreferences sp = getSharedPreferences("push", Activity.MODE_PRIVATE);
        if (!sp.contains("fcm_token")) {
            String token = FirebaseInstanceId.getInstance().getToken();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("fcm_token", token);
            editor.apply();
            presenter.makePostRequestOnNewThread();
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        DBHelper.init(getApplicationContext());
        mDbHelper = DBHelper.getHelper();

        setContentView(R.layout.map_google_activity);
        ButterKnife.bind(this);

        mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));


        mMap = mMapFragment.getMap();
        hashMapMarkerBusStops = new HashMap<>();

        // Obtain the shared Tracker instance.
        AstanaBusApplication astanaBusApplication = (AstanaBusApplication) getApplication();
        mTracker = astanaBusApplication.getDefaultTracker();


        boolean needShowWelcomeMessage = getIntent().getBooleanExtra(KEY_SHOW_WELCOME_MESSAGE, false);
        btnShowNearestBusStops.setOnClickListener(this);
        btnShowNearestBusStops.setOnLongClickListener(this);
        busStopInfoWindowView = getLayoutInflater().inflate(R.layout.bus_stop_window_google, null);
        tvBusStopTitle = (TextView) busStopInfoWindowView.findViewById(R.id.tv_bus_stop_title);
        tvBusStopDescription = (TextView) busStopInfoWindowView.findViewById(R.id.tv_bus_stop_description);
        busInfoWindowView = getLayoutInflater().inflate(R.layout.bus_window_google, null);
        tvBusTitle = (TextView) busInfoWindowView.findViewById(R.id.tv_bus_title);
        tvBusDescription = (TextView) busInfoWindowView.findViewById(R.id.tv_bus_description);


        mActionBar = getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setHomeAsUpIndicator(R.drawable.logo_drawer_fourth);
        }


        fabSearchAtoB.setOnClickListener(v -> {


            if (mMarkerPointFrom == null) {
                Toast.makeText(MapGoogleActivity.this, getString(R.string.set_point_from), Toast.LENGTH_SHORT).show();
            } else if (mMarkerPointTo == null) {
                Toast.makeText(MapGoogleActivity.this, getString(R.string.set_point_to), Toast.LENGTH_SHORT).show();
            } else {

                linearLayoutAtoB.setVisibility(View.GONE);
                recyclerViewMap.setHasFixedSize(true);
                recyclerViewMap.setVisibility(View.VISIBLE);

                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                llm.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerViewMap.setLayoutManager(llm);

            }

        });

        fabMain.setOnClickListener(v -> {
            bottomToolbar.setVisibility(View.VISIBLE);
            fabMain.setVisibility(View.GONE);
            btnShowNearestBusStops.setVisibility(View.GONE);
        });


        pointABtn.setOnClickListener(v -> {

            pointABtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_red_button));
            pointABtn.setTextColor(getResources().getColor(R.color.splash_bg));
            isFindRoutesMode = true;
            currentPointType = PointType.FROM;
            resetTimersAndClearMap();
            if (mMarkerPointTo != null) {
                setMarkerPointTo(mMarkerPointTo.getPosition(), false);
            }
            showToast(getApplicationContext(), mToast, getString(R.string.set_point_from));
        });

        pointBBtn.setOnClickListener(v -> {

            pointBBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_red_button));
            pointBBtn.setTextColor(getResources().getColor(R.color.splash_bg));

            isFindRoutesMode = true;
            currentPointType = PointType.TO;
            resetTimersAndClearMap();
            if (mMarkerPointFrom != null) {
                setMarkerPointFrom(mMarkerPointFrom.getPosition(), false);
            }
            showToast(getApplicationContext(), mToast, getString(R.string.set_point_to));

        });


        closeToolbar.setOnClickListener(v -> {
            resetTimersAndClearMap();

            resetFindRouteMode();


            mActionBar.setSubtitle("");

            pointABtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_button));
            pointBBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_button));
            pointABtn.setTextColor(getResources().getColor(R.color.black));
            pointBBtn.setTextColor(getResources().getColor(R.color.black));

            bottomToolbar.setVisibility(View.INVISIBLE);
            fabMain.setVisibility(View.VISIBLE);
            btnShowNearestBusStops.setVisibility(View.VISIBLE);
            recyclerViewMap.setVisibility(View.INVISIBLE);
            linearLayoutAtoB.setVisibility(View.VISIBLE);

        });


        markers = new ArrayList<>();

        ArrayAdapter<String> leftAdapter = new LeftDrawerAdapter2(getApplicationContext(),
                getResources().getStringArray(R.array.left_menu));

        try {
            listviewLeftMenu.setAdapter(leftAdapter);
        } catch (Exception e) {
            Log.e(LOG_TAG, "set adapter line:470 - " + e);
        }

        fragment = new AboutAppFragment();
        scheduleFragment = new SchedulesFragment();
        newsFragment = new NewsFragment();
        scheduleInsideFragment = new ScheduleInsideFragment();
        transportFragment = new TransportFragment();
        contactCentrFragment = new ContactCentrFragment();
        complaintsFragment = new ComplaintsFragment();

        if (getCurrentFragment(this) != fragment && getCurrentFragment(this) != scheduleFragment
                || getCurrentFragment(this) != newsFragment && getCurrentFragment(this) != scheduleInsideFragment &&
                getCurrentFragment(this) != transportFragment && getCurrentFragment(this) != contactCentrFragment &&
                getCurrentFragment(this) != complaintsFragment) {


            mActionBar.setSubtitle("");

            isRoutesMenu = true;

            removeFragments(this, fragment, transportFragment, contactCentrFragment,
                    scheduleFragment, scheduleInsideFragment, complaintsFragment, newsFragment);
//            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//            getSupportFragmentManager().beginTransaction().remove(transportFragment).commit();
//            getSupportFragmentManager().beginTransaction().remove(contactCentrFragment).commit();
//            getSupportFragmentManager().beginTransaction().remove(scheduleFragment).commit();
//            getSupportFragmentManager().beginTransaction().remove(scheduleInsideFragment).commit();
//            getSupportFragmentManager().beginTransaction().remove(complaintsFragment).commit();
//            getSupportFragmentManager().beginTransaction().remove(newsFragment).commit();


        }


        try {
            listviewLeftMenu.setOnItemClickListener((parent, view, position, id) -> {

                switch (position) {
                    case 0: //menu routes

                        getSupportFragmentManager().popBackStack();

                        mActionBar.setSubtitle("");

                        isRoutesMenu = true;

                        mDrawerLayout.closeDrawer(mLeftDrawer);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mRightDrawer);

                        mDrawerLayout.openDrawer(mRightDrawer);
                        ViewGroup.LayoutParams params0 = mMapFragment.getView().getLayoutParams();
                        params0.height = FrameLayout.LayoutParams.MATCH_PARENT;
                        params0.width = FrameLayout.LayoutParams.MATCH_PARENT;
                        mMapFragment.getView().setLayoutParams(params0);


                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        getSupportFragmentManager().beginTransaction().remove(transportFragment).commit();
                        getSupportFragmentManager().beginTransaction().remove(contactCentrFragment).commit();
                        getSupportFragmentManager().beginTransaction().remove(scheduleFragment).commit();
                        getSupportFragmentManager().beginTransaction().remove(scheduleInsideFragment).commit();
                        getSupportFragmentManager().beginTransaction().remove(complaintsFragment).commit();
                        getSupportFragmentManager().beginTransaction().remove(newsFragment).commit();

                        break;
                    case 1: //schedule

                        getSupportFragmentManager().popBackStack();
                        resetTimersAndClearMap();

                        hideMapFragment();

                        mActionBar.setSubtitle("АВТОБУСЫ");

                        mDrawerLayout.closeDrawer(mLeftDrawer);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mRightDrawer);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, scheduleFragment).commit();
                        break;

                    case 2: //news

                        getSupportFragmentManager().popBackStack();
                        resetTimersAndClearMap();

                        mActionBar.setSubtitle("НОВОСТИ");

                        hideMapFragment();

                        mDrawerLayout.closeDrawer(mLeftDrawer);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mRightDrawer);

                        getSupportFragmentManager().beginTransaction().replace(R.id.container, newsFragment).commit();
                        break;

                    case 3: //transport

                        getSupportFragmentManager().popBackStack();
                        resetTimersAndClearMap();

                        mActionBar.setSubtitle("ТРАНСПОРТ СТОЛИЦЫ");

                        hideMapFragment();

                        mDrawerLayout.closeDrawer(mLeftDrawer);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mRightDrawer);

                        getSupportFragmentManager().beginTransaction().replace(R.id.container, transportFragment).commit();
                        break;

                    case 4: //contact centr

                        getSupportFragmentManager().popBackStack();
                        resetTimersAndClearMap();

                        mActionBar.setSubtitle("КОНТАКТ-ЦЕНТР");

                        hideMapFragment();

                        mDrawerLayout.closeDrawer(mLeftDrawer);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mRightDrawer);
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, contactCentrFragment).commit();
                        break;

                    case 5: //complaints

                        getSupportFragmentManager().popBackStack();
                        resetTimersAndClearMap();

                        mActionBar.setSubtitle("ЖАЛОБЫ");

                        hideMapFragment();

                        mDrawerLayout.closeDrawer(mLeftDrawer);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mRightDrawer);
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, complaintsFragment).commit();
                        break;
                    case 6: //about app

                        getSupportFragmentManager().popBackStack();
                        resetTimersAndClearMap();

                        mActionBar.setSubtitle("О ПРИЛОЖЕНИИ");

                        hideMapFragment();

                        mDrawerLayout.closeDrawer(mLeftDrawer);
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mRightDrawer);
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                        break;
                }
            });
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }


        mPagerAdapter = new MyPagerAdapter(this);


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
        listViewRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listViewRoutes.setItemChecked(position, true);
            }
        });

        listViewFavoriteRoutes = (ListView) page2.findViewById(R.id.listView);
        listViewFavoriteRoutes.setOnItemClickListener(new AbsListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listViewFavoriteRoutes.setItemChecked(position, true);
            }
        });
        // TODO: empty view for all list views
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.empty_view, null);
        listViewFavoriteRoutes.setEmptyView(v.findViewById(android.R.id.empty));

        listViewHistoryRoutes = (ListView) page3.findViewById(R.id.listView);
        listViewHistoryRoutes.setOnItemClickListener(new AbsListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listViewHistoryRoutes.setItemChecked(position, true);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            listViewRoutes.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listViewFavoriteRoutes.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listViewHistoryRoutes.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

            listViewRoutes.setMultiChoiceModeListener(new ModeCallback());
            listViewFavoriteRoutes.setMultiChoiceModeListener(new ModeCallback());
            listViewHistoryRoutes.setMultiChoiceModeListener(new ModeCallback());
        }

        mViewPager.setOffscreenPageLimit(3);
        mPagerAdapter.addItem(listViewRoutes);
        mPagerAdapter.addItem(listViewFavoriteRoutes);
        mPagerAdapter.addItem(listViewHistoryRoutes);

        mViewPager.setAdapter(mPagerAdapter);
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
                        mFavoriteAdapter = new RoutesAdapter(MapGoogleActivity.this, mAdapter.getFavoritesRoutes(), RoutesAdapter.TYPE.Favorites);
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

//        Weather.fetchData(this);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
//        mActionBar.setIcon(getResources().getDrawable(R.drawable.ic_menu_bus));
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.empty,  /* nav drawer image to replace 'Up' caret */
                R.string.empty,  /* "open drawer" description for accessibility */
                R.string.empty  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
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
                    startBusesTimer();
                }
                stopRoutesStatisticsTimer();
            }

            public void onDrawerOpened(View drawerView) {
                if (llWelcomeMessage.getVisibility() == View.VISIBLE) {
                    llWelcomeMessage.setVisibility(View.GONE);
                }
                switch (drawerView.getId()) {
                    case R.id.right_drawer:
                        setTitle(getString(R.string.select_route));
                        break;
                    case R.id.left_drawer:

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
        mDrawerLayout.setEnabled(false);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        MapsInitializer.initialize(MapGoogleActivity.this);
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (status != ConnectionResult.SUCCESS) {
            initDialog();
            notHaveGmsDialog.show();
            return;
        }
        initVoteAppDialog();

        // GoogleMap settings
        if (isRoutesMenu) {

            mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map));
            setMapTransparent((ViewGroup) mMapFragment.getView());
            mMap = mMapFragment.getMap();

            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.setOnMarkerDragListener(this);
            mMap.setOnMapClickListener(this);
//            mMap.setOnInfoWindowClickListener(this);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.setOnCameraChangeListener(this);

            setDefaultCameraPosition();
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.containsKey(SingleWidget.WIDGET_ROUTE_NUMBER)) {
                isSessionFromWidget = true;
                needFinishWhenOnPause = true;
                int routeNumber = getIntent().getExtras().getInt(SingleWidget.WIDGET_ROUTE_NUMBER, -1);
                if (routeNumber != -1) {
                    routeForZoom = Route.getByNumber(mDbHelper, routeNumber);
                    severalRoutes.clear();
                    selectRoute(Route.getByNumber(mDbHelper, routeNumber), true, false);
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
                SharedPreferences prefs = getSharedPreferences(MAIN_PREFS, MODE_PRIVATE);
                int currentTab = prefs.getInt(KEY_LAST_OPENED_TAB, 0);
                if (!prefs.contains(KEY_IS_SHOWN_SEARCH_MENU) || !prefs.getBoolean(KEY_IS_SHOWN_SEARCH_MENU, false)) {
                    mDrawerLayout.openDrawer(mLeftDrawer);
                    prefs.edit().putBoolean(KEY_IS_SHOWN_SEARCH_MENU, true).apply();
                } else {
                    if (!mDrawerLayout.isDrawerOpen(mLeftDrawer))
                        mDrawerLayout.openDrawer(mRightDrawer);
                    mViewPager.setCurrentItem(currentTab);
                }
            }

            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(KEY_SELECTED_ROUTE_SERVER_ID)) {
                    long routeServerId = savedInstanceState.getLong(KEY_SELECTED_ROUTE_SERVER_ID);
                    savedInstanceState.remove(KEY_SELECTED_ROUTE_SERVER_ID);
                    mSelectedRoute = Route.getByServerId(mDbHelper, routeServerId);
                    if (mSelectedRoute != null) {
                        routeForZoom = mSelectedRoute;
                        severalRoutes.clear();
                        selectRoute(mSelectedRoute, true, true);
                    }
                }
            }
        }


        //PUSH NOTIFICATIONS
        String menuFragment = getIntent().getStringExtra("menuFragment");
        if (menuFragment != null) {
            if (menuFragment.equals("favoritesMenuItem")) {
                showNewsFragment();
            }
        } else {
//            StandardFragment standardFragment = new StandardFragment();
//            fragmentTransaction.replace(android.R.id.content, standardFragment);
        }
        //END PUSH

    }


    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putLong(KEY_SELECTED_ROUTE_SERVER_ID, mSelectedRoute != null ? mSelectedRoute.getServerId() : -1);
    }

    /*
    * Установка прозрачности для view карты
    * Проблема описана тут http://stackoverflow.com/questions/14486223/supportmapfragment-in-nexus-one
    */
    private void setMapTransparent(ViewGroup group) {
        int childCount = group.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = group.getChildAt(i);

            if (child instanceof ViewGroup) {
                setMapTransparent((ViewGroup) child);
            } else if (child instanceof SurfaceView) {
                child.setBackgroundColor(0x00000000);
            }
        }
    }


    // показываем Астану
    private void setDefaultCameraPosition() {
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(Consts.DEFAULT_CITY_LOCATION, 11);
        mMap.moveCamera(yourLocation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_FORECAST_CODE:
                    if (data.getExtras() == null || !data.getExtras().containsKey(Consts.KEY_ROUTES_ID)) {
                        //Crashlytics.log("!data.getExtras().containsKey(Consts.KEY_ROUTES_ID)");
                        return;
                    }
                    if (mAdapter == null) {
                        //Crashlytics.log("mAdapter == null");
                        return;
                    }
                    ArrayList<Integer> selectedRouteNumbers = (ArrayList<Integer>) data.getSerializableExtra(Consts.KEY_ROUTES_ID);
                    ArrayList<Route> routes = mAdapter.getOriginalData();
                    ArrayList<Route> selectedRoutes = new ArrayList<>();
                    if (selectedRouteNumbers.size() == 1) {
                        for (int i = 0; i < routes.size(); i++) {
                            if (selectedRouteNumbers.contains(routes.get(i).getNumber())) {
                                selectedRoutes.add(routes.get(i));
                                break;
                            }
                        }
                        routeForZoom = selectedRoutes.get(0);
                        severalRoutes.clear();
                        selectRoute(selectedRoutes.get(0), false, false);

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
        if (isRoutesMenu)
            inflater.inflate(R.menu.main_menu, menu);

        menu1 = menu;
//        MenuItem item = menu.findItem(R.id.menu_display_bus_stops);
        showBusStopsForRoute = mSharedPreferences.getBoolean(KEY_SHOW_BUS_STOPS, true);
//        item.setChecked(showBusStopsForRoute);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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

            case R.id.menu_find_routes:
                mDrawerLayout.closeDrawer(mRightDrawer);
                mDrawerLayout.openDrawer(mLeftDrawer);
                break;
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
                    mDrawerLayout.closeDrawer(mLeftDrawer);
                    return true;
                }
                mDrawerLayout.closeDrawer(mRightDrawer);
                mDrawerLayout.openDrawer(mLeftDrawer);
                return true;
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
                Location myLocation = null;
                try {
                    myLocation = mMap.getMyLocation();
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, "layer android 6 " + e);
                }
                double lat = 0, lon = 0;
                if (myLocation != null) {
                    lat = myLocation.getLatitude();
                    lon = myLocation.getLongitude();
                }
                showNearestBusStops(lat, lon);
                mDrawerLayout.closeDrawer(mLeftDrawer);
                isCustomLocationMode = false;
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.btn_show_nearest_bus_stops_for_custom_location:
                showToast(getApplicationContext(), mToast, getString(R.string.click_on_map));
                isFindRoutesMode = false;
                isCustomLocationMode = true;
                break;
        }
        return true;
    }


    private class RoutesDrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            resetFindRouteMode();
            Route route = (Route) parent.getAdapter().getItem(position);
            mSelectedRoutes = null;
            hashMapMarkerBusStops.clear();
            routeForZoom = route;
            severalRoutes.clear();
            selectRoute(route, parent.getId() == R.id.lv_routes_menu, false);
        }
    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        //if zoom is close than 14, show bus stops, otherwise clear them
        if (cameraPosition.zoom > 13) {
            if (routeForZoom != null) {
                drawBusStops(routeForZoom);
            }
            if (!severalRoutes.isEmpty()) {
                for (Route route : severalRoutes) {
                    drawBusStops(route);
                }
            }
        } else {
            clearBusStops();
        }


        if (cameraPosition.zoom > maxZoom)
            mMap.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    // используется при поиске маршрутов от точки А до точки Б
    @Override
    public void onMarkerDragEnd(Marker marker) {
        isFindRoutesMode = true;
        if (marker.getTitle().equals(getString(R.string.from))) {
//            etPointFrom.setText(getString(R.string.point_a));
        } else if (marker.getTitle().equals(getString(R.string.to))) {
//            etPointTo.setText(getString(R.string.point_b));
        }
//        mDrawerLayout.openDrawer(mLeftDrawer);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (isFindRoutesMode) {
                    /*Variant #2*/
            switch (currentPointType) {
                case FROM:
                    setMarkerPointFrom(latLng, true);
                    break;
                case TO:
                    setMarkerPointTo(latLng, true);
                    break;
            }
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
        showNearestBusStops(latLng.latitude, latLng.longitude);
        mCustomLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.selected_location))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private Timer busTimer, routeStatisticTimer, busesTimer;
    boolean isRouteChanged;
    boolean isMultiSelectRoutes;

    /*
    * отрисовка линии маршрута, отрисовка маркеров остановок для выбранного маршрута
    */
    private Route selectRoute(final Route route, boolean removeFindMarkers, boolean isDisplayOrientationChanged) {
        hideKeyboard(etSearchRoute);

        if (!isDisplayOrientationChanged && (route == null || (mSelectedRoute != null && route.getId() == mSelectedRoute.getId()))) {
            mDrawerLayout.closeDrawer(mRightDrawer);
            isRouteChanged = false;
            return mSelectedRoute;
        }
        mSelectedRoute = route;
        mSelectedBusStop = null;
        isRouteChanged = true;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(mSelectedRoute.toString());
        }
        mDrawerLayout.closeDrawer(mRightDrawer);
        mDrawerLayout.closeDrawer(mLeftDrawer);

        if (mSelectedRoute.getBusStops() == null) {
            mSelectedRoute.setBusStops(new BusStopController(MapGoogleActivity.this).getRouteBusStops(mSelectedRoute.getServerId()));
            if (mSelectedRoute.linkedRoute != null) {
                mSelectedRoute.linkedRoute.setBusStops(new BusStopController(MapGoogleActivity.this).getRouteBusStops(
                        mSelectedRoute.linkedRoute.getServerId()));
            }
        }


        mMap.clear();

        drawRouteLineGoogle(mSelectedRoute, removeFindMarkers);
        updateLastSeenDate(route);

        return mSelectedRoute;
    }

    // обновляем дату просмотра маршрута, используется для сортировки на вкладке "История" в правом боковом меню
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
//            return mFoundRoutesAdapter;
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
            return listviewLeftMenu;
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
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
        // из-за флага  android:configChanges="screenSize" при повороте экрана не пересоздается вьюха,
        // поэтому делаю назначение "веса" у индикатора вручную
        /*TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.page_indicator_weight, typedValue, true);
        float weight = typedValue.getFloat();
        float slideMenuWidth = getResources().getDimension(R.dimen.slide_menu_width);

        RelativeLayout leftDrawer = (RelativeLayout) findViewById(R.id.left_drawer);
        LinearLayout rightDrawer = (LinearLayout) findViewById(R.id.right_drawer);

        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) leftDrawer.getLayoutParams();
        DrawerLayout.LayoutParams params2 = (DrawerLayout.LayoutParams) rightDrawer.getLayoutParams();
        params.width = (int) slideMenuWidth;
        params2.width = (int) slideMenuWidth;
        leftDrawer.setLayoutParams(params);
        rightDrawer.setLayoutParams(params2);

        findViewById(R.id.indicator).setLayoutParams(new TableLayout.LayoutParams(ViewPager.LayoutParams.WRAP_CONTENT, 0, weight));*/
    }


    @Override
    public void onDestroy() {
//        if (mAdView != null) {
//            mAdView.destroy();
//        }
        DBHelper.release();
        if (mLoadDataAsyncTask != null && mLoadDataAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadDataAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBusTimer();
        stopBusesTimer();
        stopRoutesStatisticsTimer();
        if (needFinishWhenOnPause && isSessionFromWidget) {
            finish();
        }
        needFinishWhenOnPause = true;

        SharedPreferences.Editor editor = getSharedPreferences(MAIN_PREFS, MODE_PRIVATE).edit();
        editor.putInt(KEY_LAST_OPENED_TAB, mViewPager.getCurrentItem());
        editor.apply();

        updateWidgets();

//        if (mAdView != null) {
//            mAdView.pause();
//        }
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

        String name = "MapGoogleActivity";
        Log.i(LOG_TAG, "Setting screen name: " + name);
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        mIsRouteStatisticEnabled = mSharedPreferences.getBoolean(getString(R.string.key_route_statistic), false);

        if (isFindRoutesMode)
            startRoutesStatisticsTimer();

        if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
            startRoutesStatisticsTimer();
        } else if (!mDrawerLayout.isDrawerOpen(mRightDrawer) && !mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
            isRouteChanged = true;
            startBusTimer();
            startBusesTimer();
        }
//        if (mAdView != null) {
//            mAdView.resume();
//        }
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
                if (busTimer == null) { //29.07
                    busTimer.cancel();
                    busTimer.purge();
                }
                TimerMethod(mSelectedRoute);
            }
        }, 0, Consts.BUS_TIMER_INTERVAL);
    }

    private void stopBusTimer() {
        if (busTimer != null) {

            busTimer.cancel();
            busTimer.purge();     //29.07
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
                mBuses = null;  //28.07
                stopBusTimer();
                return;
            }

            Log.d("astanaMapGoogle", "got in Timer Method for " + route.getNumber()); //29.07
            Log.d("astanaTimerMethod", "mSelected route is " + mSelectedRoute);
            mBuses = BusController.getRouteBuses(route);


            if (mBuses.size() > 0 && mSelectedRoute != null) {
                if (mBuses.get(0).getRouteNumber() != mSelectedRoute.getNumber()) {
                    Log.d("astanaTimerMethod", " DoNotrunonUi");
                } else {
                    Log.d("astanaTimerMethod", " runonUi");

                    this.runOnUiThread(BusTimerTick);
                }
            }
        } catch (HttpException | IOException e) {
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
            mBuses = BusController.getSeveralRouteBuses(routes);
            //Log.d("1274", mBuses.toString());
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
        } catch (Exception e) {
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
        if (line != null)
            line.remove();
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (LatLng point : route.getTrackPoints()) {
            options.add(point);
        }
        line = mMap.addPolyline(options);

        PolylineOptions options2 = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (LatLng point : route.getTrackPointsForInverseCourse()) {
            options2.add(point);
        }
        mMap.addPolyline(options2);
        zoomMapForPoints(route.getTrackPoints());

        if (removeFindMarkers) {
            if (mMarkerPointFrom != null) {
                mMarkerPointFrom = mMap.addMarker(new MarkerOptions()
                        .position(mMarkerPointFrom.getPosition())
                        .title(getString(R.string.from))
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.point_a_new)));
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
            if (mMarkerPointTo != null) {
                mMarkerPointTo = mMap.addMarker(new MarkerOptions()
                        .position(mMarkerPointTo.getPosition())
                        .title(getString(R.string.to))
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.point_b_new)));
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
    }

    ArrayList<Polyline> polylines;
    HashMap<Integer, Integer> hashMapRouteLineColors;
    // цвета используемые при отрисовке маршрутов в режиме MultiSelect
    final int[] colors = new int[]{Consts.BLUE, Consts.GREEN, Consts.ORANGE,
            Consts.PINK, Consts.PURPLE};

    final int[] colorsRoute = new int[]{Color.rgb(74, 108, 252), Color.rgb(27, 189, 92),
            Color.rgb(238, 135, 3), Color.rgb(252, 74, 91), Color.rgb(99, 65, 226)
    };

    // Рисую линии маршрутов
    private void drawRoutes(List<Route> routes) {
        mSelectedBusStop = null;
        mSelectedRoute = null;
        mMap.clear();

        if (polylines == null) {
            polylines = new ArrayList<>();
        }
        isCustomLocationMode = false;
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
        StringBuilder sb = new StringBuilder(getString(R.string.routes) + ": ");
        hashMapRouteLineColors = new HashMap<>();

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

            PolylineOptions options = new PolylineOptions().width(5).color(colorsRoute[i]).geodesic(true);
            for (LatLng point : route.getTrackPoints()) {
                options.add(point);
            }
            line = mMap.addPolyline(options);
            polylines.add(line);
            updateLastSeenDate(route);
            hashMapRouteLineColors.put(route.getNumber(), colors[i]);
        }
        mActionBar.setTitle(getString(R.string.app_name));
        mActionBar.setSubtitle(sb.toString());
        setDefaultCameraPosition();

        startBusesTimer();
    }

    // Изменение масштаба карты, т.о. чтобы все точки маршрута вместились на экране
    private void zoomMapForPoints(List<LatLng> points) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    // Изменение масштаба карты, т.о. чтобы все остановки вместились на экране
    private void zoomMapForBusStops(List<BusStop> points) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (BusStop busStop : points) {
            builder.include(busStop.getPointGoogle());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
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
                for (Marker marker : markers) {
                    marker.remove();
                }
            } else {
                // Crashlytics.log("markers == null in drawBusesGoogle(ArrayList<Bus> buses)");
            }
            tvInternetStatus.setVisibility(View.VISIBLE);
            return;
        }
        tvInternetStatus.setVisibility(View.GONE);
        if (!isRouteChanged && hashMapBusMarkers != null && hashMapBusMarkers.size() > 0) {
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
                    isInfoWindowShown = marker.isInfoWindowShown();
                    marker.setPosition(bus.getPointGoogle());
                    marker.setSnippet(String.format(getString(R.string.bus_marker_text), bus.getRouteNumber(), bus.getSpeed(), bus.getTime("HH:mm:ss")));
                    try {

                        marker.setIcon(BitmapDescriptorFactory.fromResource(Bus.getDirectionIcon(MapGoogleActivity.this, bus.getZ(), routeLineColor)));
                    } catch (IllegalArgumentException e) {
                        Log.e(LOG_TAG, "Stack trace - " + e);
                    }
                    // для обновления сниппета переоткрываю всплывающее окно
                    if (isInfoWindowShown) {
                        marker.showInfoWindow();
                    }
                    alarm(bus);
                }
            }
            return;
        }
        isRouteChanged = false;
        for (Marker marker : markers) {
            marker.remove();
        }
        hashMapBusMarkers = new HashMap<>();
        for (Bus bus : buses) {
            int routeLineColor = Color.BLUE;
            if (mSelectedRoute == null && hashMapRouteLineColors != null && hashMapRouteLineColors.containsKey(bus.getRouteNumber()))
                routeLineColor = hashMapRouteLineColors.get(bus.getRouteNumber());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(bus.getPointGoogle())
                    .title(String.format("№ %s", bus.getName()))
                    .snippet(String.format(getString(R.string.bus_marker_text), bus.getRouteNumber(), bus.getSpeed(), bus.getTime("HH:mm:ss")))
                    .icon(BitmapDescriptorFactory.fromResource(Bus.getDirectionIcon(MapGoogleActivity.this, bus.getZ(), routeLineColor))));
            hashMapBusMarkers.put(bus.getServerId(), marker);
            markers.add(marker);
            alarm(bus);
        }
        if (buses.size() == 0) {
            // TODO: решить в каких случаях сервер не вернул данные
//            showToast("Sorry, no bus");
            //setErrorTextMessage(getString(R.string.server_not_available));
        } else {
            tvInternetStatus.setVisibility(View.GONE);
        }
    }

    double prevDistance = 0;
    List<Bus> ignoreBusList = new ArrayList<>();

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
                showToast(getApplicationContext(), mToast,
                        bus.getName() + ". " + String.valueOf(distance) + ". " + String.valueOf(ignoreBusList.contains(bus)));
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


    private void drawBusStops(Route route) {
        List<BusStop> busStops = route.getBusStops();
        if (busStops == null)
            return;
        for (int i = 0; i < busStops.size(); i++) {
            int resourceId = R.drawable.road_sign;
            BusStop busStop = busStops.get(i);
//            if (i == 0) {     //that was for end sings i think
//                resourceId = R.drawable.road_sign;
//            } else if (i == busStops.size() - 1) {
//                resourceId = R.drawable.road_sign;
//            }
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(busStop.getPointGoogle())
                    .title(busStop.getName())
                    .snippet(String.valueOf(busStop.getServerId()))
                    .icon(BitmapDescriptorFactory.fromResource(resourceId)));
            hashMapMarkerBusStops.put(marker, busStop);
        }
    }

    private void clearBusStops() {
        for (Marker marker : hashMapMarkerBusStops.keySet()) {
            marker.remove();
        }
        hashMapMarkerBusStops.clear();
    }

    private void showNearestBusStops(double lat, double lon) {
        resetTimersAndClearMap();
        isShowBusStopsMode = true;
        tvInternetStatus.setVisibility(View.GONE);
        mSelectedRoute = null;
        stopBusTimer();
        stopBusesTimer();
        mActionBar.setSubtitle("");
        List<BusStop> nearestBusStops = BusStop.getNearestBusStops(mDbHelper, lat, lon);
        if (nearestBusStops == null) {
            showToast(getApplicationContext(), mToast, getString(R.string.your_location_undefined));
            mMap.clear();
//            clearAllMapMarkers();
            return;
        }
        if (nearestBusStops.size() == 0) {
            showToast(getApplicationContext(), mToast, getString(R.string.nearest_bus_stops_not_found));
            mMap.clear();
//            clearAllMapMarkers();

            return;
        }
        drawBusStops(nearestBusStops);
    }

    private void drawBusStops(List<BusStop> busStops) {
        if (busStops == null)
            return;
        mMap.clear();
//        clearAllMapMarkers();

        hashMapMarkerBusStops.clear();
        for (int i = 0; i < busStops.size(); i++) {
            int resourceId = R.drawable.road_sign;
            BusStop busStop = busStops.get(i);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(busStop.getPointGoogle())
                    .title(busStop.getName())
                    .snippet(String.valueOf(busStop.getServerId()))
                    .icon(BitmapDescriptorFactory.fromResource(resourceId)));
            hashMapMarkerBusStops.put(marker, busStop);
        }
        mActionBar.setSubtitle(R.string.nearest_bus_stops);
        zoomMapForBusStops(busStops);
    }

    Toast mToast;


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
            busStops = new ArrayList<>();
            MapGoogleActivity.this.setSupportProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            MapGoogleActivity.this.setSupportProgressBarIndeterminateVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            busStops = BusStop.getAll(mDbHelper);
            mRoutes = Route.getAllGroupByNumber(mDbHelper);
            historyRoutes = Route.getHistoryRoutes(mDbHelper);
            mBusStopsPointFromAdapter = new BusStopsAdapter(MapGoogleActivity.this, R.layout.bus_stops_list_item, busStops);
            mBusStopsPointToAdapter = new BusStopsAdapter(MapGoogleActivity.this, R.layout.bus_stops_list_item, busStops);
            mAdapter = new RoutesAdapter(MapGoogleActivity.this, mRoutes, RoutesAdapter.TYPE.All);
            mFavoriteAdapter = new RoutesAdapter(MapGoogleActivity.this, Route.getFavorites(mDbHelper), RoutesAdapter.TYPE.Favorites);
            mHistoryAdapter = new RoutesAdapter(MapGoogleActivity.this, historyRoutes, RoutesAdapter.TYPE.History);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            listViewRoutes.setAdapter(mAdapter);
            listViewFavoriteRoutes.setAdapter(mFavoriteAdapter);
            listViewHistoryRoutes.setAdapter(mHistoryAdapter);
            MapGoogleActivity.this.setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    private void initDialog() {
        notHaveGmsDialog = new AlertDialog.Builder(MapGoogleActivity.this);
        notHaveGmsDialog.setTitle(getString(R.string.attention));  // заголовок
        notHaveGmsDialog.setMessage(getString(R.string.you_not_have_gms)); // сообщение
        notHaveGmsDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                String appPackageName = "com.google.android.gms";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                finish();
            }
        });
        notHaveGmsDialog.setCancelable(false);
    }

    // просим оценить приложение в маркете
    private void initVoteAppDialog() {
        voteAppDialog = new AlertDialog.Builder(MapGoogleActivity.this);
        voteAppDialog.setTitle(getString(R.string.attention));
        voteAppDialog.setMessage(getString(R.string.vote_application));
        voteAppDialog.setPositiveButton(R.string.vote, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                SharedPreferences.Editor editor = getSharedPreferences(MAIN_PREFS, MODE_PRIVATE).edit();
                editor.putBoolean(KEY_NOT_SHOW_VOTE_DIALOG, true);
                editor.apply();
                dialog.dismiss();
            }
        });
        voteAppDialog.setNegativeButton(R.string.not_ask, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                SharedPreferences.Editor editor = getSharedPreferences(MAIN_PREFS, MODE_PRIVATE).edit();
                editor.putBoolean(KEY_NOT_SHOW_VOTE_DIALOG, true);
                editor.apply();
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


    // callback для режима MultiSelect (маршруты)
    public class ModeCallback implements AbsListView.MultiChoiceModeListener {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = MapGoogleActivity.this.getMenuInflater();
            inflater.inflate(R.menu.several_routes_menu, menu);
            mode.setTitle(getString(R.string.select_several_routes));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
        }


        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.show_routes_on_map:
                    isRouteChanged = true;
                    mSelectedRoute = null;
                    SparseBooleanArray array = getListView().getCheckedItemPositions();
                    isMultiSelectRoutes = true;
                    ArrayList<Route> selectedRoutes = new ArrayList<>();
                    for (int i = 0; i < array.size(); i++) {
                        if (array.valueAt(i))
                            selectedRoutes.add(getAdapter().getItem(array.keyAt(i)));
                    }
                    mSelectedRoutes = selectedRoutes;


                    if (selectedRoutes.size() == 1) {
                        routeForZoom = selectedRoutes.get(0);
                        severalRoutes.clear();
                        selectRoute(routeForZoom, false, false);
                    } else {
                        severalRoutes = selectedRoutes;
                        routeForZoom = null;
                        drawRoutes(selectedRoutes);
                    }
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
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked) {
            if (getListView().getCheckedItemCount() > Consts.MAX_ROUTES_ON_MAP) {
                getListView().setItemChecked(position, !checked);
                showToast(getApplicationContext(), mToast, getString(R.string.max5_select_routes));
            }
        }
    }

    /*
    * проверяем кол-во запусков приложения
    * показываем каждый 5й раз, если юзер не нажимал "не спрашивать"
    */
    private void checkStartsCount() {
        int startsCount;
        SharedPreferences sp = getSharedPreferences(MAIN_PREFS, MODE_PRIVATE);
        startsCount = sp.getInt(KEY_STARTS_COUNT, 0) + 1;
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_STARTS_COUNT, startsCount);
        editor.apply();
        if (startsCount % 5 == 0 && !sp.getBoolean(KEY_NOT_SHOW_VOTE_DIALOG, false)) {
            voteAppDialog.show();
        }
    }

    // поиск маршрутов по точкам "от" и "до"
    void findRoutes() {
        if (mMarkerPointFrom == null && mMarkerPointTo == null) {
//            listviewLeftMenu.setAdapter(null);
            return;
        }
        if (mMarkerPointFrom == null || mMarkerPointTo == null) {
//            if (mMarkerPointFrom == null)
//                Toast.makeText(MapGoogleActivity.this, "Вы не выбрали точку отправления", Toast.LENGTH_SHORT).show();
//            if (mMarkerPointFrom == null)
//                Toast.makeText(MapGoogleActivity.this, "Вы не выбрали точку прибытия", Toast.LENGTH_SHORT).show();
            return;
        }

        routes = Route.findRoutesForPoints(DBHelper.getHelper(), mMarkerPointFrom.getPosition(), mMarkerPointTo.getPosition());
//        mFoundRoutesAdapter = new RoutesAdapter(MapGoogleActivity.this, routes, RoutesAdapter.TYPE.History);

        atoBAdapter = new AtoBAdapter(routes);

        if (atoBAdapter.getItemCount() == 0) {
            Toast.makeText(MapGoogleActivity.this, "Нет маршрутов в выбранном направлении", Toast.LENGTH_SHORT).show();
        }

        recyclerViewMap.setAdapter(atoBAdapter);

        recyclerViewMap.addOnItemTouchListener(new AtoBAdapter(MapGoogleActivity.this, routes,
                (childView, childAdapterPosition) -> {

                    resetFindRouteMode();
                    Route route = routes.get(childAdapterPosition);

                    mSelectedRoutes = null;
                    hashMapMarkerBusStops.clear();
                    routeForZoom = route;
                    severalRoutes.clear();


                    selectRoute(route, true, false);

                    mSelectedRoute = route;
                    startBusTimer();

                }));

    }

    // ставим маркер "От" для поиска маршрута А-Б
    void setMarkerPointFrom(LatLng position, boolean isNeedFind) {
        resetTimersAndClearMap();
        if (mMarkerPointFrom != null) {
            mMarkerPointFrom.remove();
        }
        if (mMarkerPointTo != null) {
            mMarkerPointFrom = null;
            setMarkerPointTo(mMarkerPointTo.getPosition(), false);
        }
        mMarkerPointFrom = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(getString(R.string.from))
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.point_a_new)));
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        if (isNeedFind)
            findRoutes();
    }

    // ставим маркер "До" для поиска маршрута А-Б
    void setMarkerPointTo(LatLng position, boolean isNeedFind) {
        resetTimersAndClearMap();
        if (mMarkerPointTo != null) {
            mMarkerPointTo.remove();
        }
        if (mMarkerPointFrom != null) {
            mMarkerPointTo = null;
            setMarkerPointFrom(mMarkerPointFrom.getPosition(), false);
        }
        mMarkerPointTo = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(getString(R.string.to))
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.point_b_new)));

//        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        if (isNeedFind)
            findRoutes();
    }

    void resetFindRouteMode() {
        if (tvInternetStatus != null)
            tvInternetStatus.setVisibility(View.GONE);
        isFindRoutesMode = false;
        if (mToast != null)
            mToast.cancel();
    }

    // останавливаем таймеры загрузки данных
    void resetTimersAndClearMap() {
        mSelectedRoute = null;
        mSelectedRoutes = null;
        mBuses = null;
        routeForZoom = null;
        stopBusTimer();
        stopBusesTimer();
        stopRoutesStatisticsTimer();
        mMap.clear();

    }


    @Override
    public void onBackPressed() {

        if (mMapFragment != null && mMapFragment.getView() != null) {

            if (mMapFragment.getView().getWidth() != 0) {
                if (mDrawerLayout.isDrawerOpen(mRightDrawer)) {
                    mDrawerLayout.closeDrawer(mRightDrawer);
                    getSupportFragmentManager().beginTransaction().addToBackStack("right").commit();
                }
            }

            //checking if map fragment is hidden and if schduleinsidefrag is closed
            if (mMapFragment.getView().getWidth() == 0 &&
                    getSupportFragmentManager().getBackStackEntryCount() == 0) {

                mActionBar.setSubtitle("");

                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                getSupportFragmentManager().beginTransaction().remove(transportFragment).commit();
                getSupportFragmentManager().beginTransaction().remove(contactCentrFragment).commit();
                getSupportFragmentManager().beginTransaction().remove(scheduleFragment).commit();
                getSupportFragmentManager().beginTransaction().remove(scheduleInsideFragment).commit();
                getSupportFragmentManager().beginTransaction().remove(complaintsFragment).commit();
                getSupportFragmentManager().beginTransaction().remove(newsFragment).commit();

                isRoutesMenu = true;

                mDrawerLayout.closeDrawer(mLeftDrawer);
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mRightDrawer);

                mDrawerLayout.openDrawer(mRightDrawer);
                ViewGroup.LayoutParams params0 = mMapFragment.getView().getLayoutParams();
                params0.height = FrameLayout.LayoutParams.MATCH_PARENT;
                params0.width = FrameLayout.LayoutParams.MATCH_PARENT;
                mMapFragment.getView().setLayoutParams(params0);

            } else {
                try {
                    super.onBackPressed();
                } catch (IllegalStateException e) {
                    Log.d("astana", e.getMessage());
                }
            }
        }

    }


    void hideMapFragment() {
        fabMain.setVisibility(View.GONE);
        if (mMapFragment != null) {
            ViewGroup.LayoutParams params = mMapFragment.getView().getLayoutParams();

            params.height = 0;
            params.width = 0;
            mMapFragment.getView().setLayoutParams(params);
        } else {

        }
        isRoutesMenu = false;


        bottomToolbar.setVisibility(View.INVISIBLE);
        recyclerViewMap.setVisibility(View.INVISIBLE);
    }

    private List<Route> routes;


    public void showNewsFragment() {
        getSupportFragmentManager().popBackStack();
        resetTimersAndClearMap();

        mActionBar.setSubtitle("НОВОСТИ");

        hideMapFragment();

        mDrawerLayout.closeDrawer(mLeftDrawer);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mRightDrawer);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, newsFragment).commit();
    }


}