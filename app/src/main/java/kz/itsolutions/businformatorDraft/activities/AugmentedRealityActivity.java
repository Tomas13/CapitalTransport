package kz.itsolutions.businformatordraft.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jwetherell.augmented_reality.activity.AugmentedReality;
import com.jwetherell.augmented_reality.data.ARData;
import com.jwetherell.augmented_reality.data.BusesDataSource;
import com.jwetherell.augmented_reality.data.NetworkDataSource;
import com.jwetherell.augmented_reality.ui.Marker;
import com.jwetherell.augmented_reality.widget.VerticalTextView;

import org.apache.http.HttpException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import kz.itsolutions.businformatordraft.R;
import kz.itsolutions.businformatordraft.controllers.BusController;
import kz.itsolutions.businformatordraft.db.DBHelper;
import kz.itsolutions.businformatordraft.model.Bus;
import kz.itsolutions.businformatordraft.model.Route;
import kz.itsolutions.businformatordraft.utils.Consts;

/**
 * This class extends the AugmentedReality and is designed to be an example on
 * how to extends the AugmentedReality class to show multiple data sources.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class AugmentedRealityActivity extends AugmentedReality {

    private static final String LOG_TAG = "Demo";
    public static final String KEY_SELECTED_ROUTE_ID = "key_selected_route_id";
    private static final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
    private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS, queue);
    private Map<String, NetworkDataSource> sources = new ConcurrentHashMap<>();

    private static Toast myToast = null;
    private static VerticalTextView text = null;

    Route mSelectedRoute;
    ArrayList<Bus> mBuses;
    private Timer busTimer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBHelper.init(this);
        // Create toast
        myToast = new Toast(getApplicationContext());
        myToast.setGravity(Gravity.CENTER, 0, 0);
        // Creating our custom text view, and setting text/rotation
        text = new VerticalTextView(getApplicationContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        text.setLayoutParams(params);
        text.setBackgroundResource(android.R.drawable.toast_frame);
        text.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Small);
        text.setShadowLayer(2.75f, 0f, 0f, Color.parseColor("#BB000000"));
        myToast.setView(text);
        // Setting duration and displaying the toast
        myToast.setDuration(Toast.LENGTH_SHORT);

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
            return;
        }
        int routeServerId = intent.getExtras().getInt(KEY_SELECTED_ROUTE_ID, -1);
        mSelectedRoute = Route.getByServerId(DBHelper.getHelper(), routeServerId);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mSelectedRoute.toString());
            actionBar.setIcon(R.drawable.ic_menu_bus);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        startBusTimer();
    }

    private void startBusTimer() {
        stopBusTimer();
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

    private void TimerMethod(Route route) {
        try {
            if (route == null) {
                stopBusTimer();
                return;
            }
            mBuses = BusController.getBusInfoByRouteNumber(route.getNumber());
            this.runOnUiThread(BusTimerTick);
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
    }

    private Runnable BusTimerTick = new Runnable() {
        public void run() {
            Log.v(LOG_TAG, "bus timer tick");

            NetworkDataSource googlePlaces = new BusesDataSource(AugmentedRealityActivity.this, AugmentedRealityActivity.this.getResources(), mSelectedRoute);
            sources.put("busStops", googlePlaces);
            updateDataOnZoom();
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();

        Location last = ARData.getCurrentLocation();
        updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
    }

    @Override
    public void onStop() {
        super.onStop();
        stopBusTimer();
        finish();
        DBHelper.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.augmented_reality_menu, menu);*/

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.showRadar:
                showRadar = !showRadar;
                item.setTitle(getString(showRadar ? R.string.hide_radar : R.string.show_radar));
                break;
            case R.id.showZoomBar:
                showZoomBar = !showZoomBar;
                item.setTitle(getString(showZoomBar ? R.string.hide_zoom_bar : R.string.show_zoom_bar));
                zoomLayout.setVisibility((showZoomBar) ? LinearLayout.VISIBLE : LinearLayout.GONE);
                break;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        updateData(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void markerTouched(Marker marker) {
        text.setText(marker.getName());
        myToast.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateDataOnZoom() {
        super.updateDataOnZoom();
        Location last = ARData.getCurrentLocation();
        updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
    }

    private void updateData(final double lat, final double lon, final double alt) {
        try {
            exeService.execute(new Runnable() {
                @Override
                public void run() {
                    for (NetworkDataSource source : sources.values())
                        download(source, lat, lon, alt);
                }
            });
        } catch (RejectedExecutionException rej) {
            Log.w(LOG_TAG, "Not running new download Runnable, queue is full.");
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception running download Runnable.", e);
        }
    }

    private static boolean download(NetworkDataSource source, double lat, double lon, double alt) {
        if (source == null) return false;

        List<Marker> markers;
        try {
            markers = source.parse("");
            if (markers == null) return false;
        } catch (NullPointerException e) {
            return false;
        }
        ARData.clearMarkerList();
        ARData.addMarkers(markers);
        return true;
    }
}
