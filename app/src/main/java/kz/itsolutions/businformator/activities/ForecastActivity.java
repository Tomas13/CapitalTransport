package kz.itsolutions.businformator.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.ForecastsAdapter;
import kz.itsolutions.businformator.controllers.BusStopController;
import kz.itsolutions.businformator.controllers.RouteController;
import kz.itsolutions.businformator.db.DBHelper;
import kz.itsolutions.businformator.model.BusStop;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.utils.Consts;
import org.apache.http.HttpException;
import org.json.JSONException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

//import com.google.analytics.tracking.android.EasyTracker;
//import com.google.analytics.tracking.android.Tracker;

public class ForecastActivity extends ListActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String KEY_SELECTED_BUS_STOP_ID = "selected_bus_stop_id";

    BusStopController mBusStopController;
    BusStop mBusStop;
    //Tracker mTracker;
    SwipeRefreshLayout mSwipeRefreshLayout;
    LoadDataAsyncTask mAsyncTask;
    ListView mListView;
    Toast toastMultiSelect;
    private static String LOG_TAG = "astana_bus";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forecast_activity);
        DBHelper.init(this);
//        EasyTracker.getInstance().setContext(this);
//        mTracker = EasyTracker.getTracker();
        findViewById(R.id.btn_repeat_load_forecast).setOnClickListener(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // делаем повеселее
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, R.color.yellow, R.color.red);
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_menu_bus);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        int busStopId = intent.getIntExtra(MapGoogleActivity.KEY_SELECTED_BUS_STOP_ID, 0);
        mBusStopController = new BusStopController(this);
        mListView = getListView();
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
            mListView.setMultiChoiceModeListener(new ModeCallback());
        BusStop busStop = BusStop.getByServerId(DBHelper.getHelper(), busStopId);
        if (busStop == null) {
            double pointFromLat = intent.getDoubleExtra(MapGoogleActivity.KEY_FIND_ROUTES_FROM_LAT, 0);
            double pointFromLon = intent.getDoubleExtra(MapGoogleActivity.KEY_FIND_ROUTES_FROM_LON, 0);
            double pointToLat = intent.getDoubleExtra(MapGoogleActivity.KEY_FIND_ROUTES_TO_LAT, 0);
            double pointToLon = intent.getDoubleExtra(MapGoogleActivity.KEY_FIND_ROUTES_TO_LON, 0);

            // TODO
           /* List<Route> routes = Route.findRoutesForPoints(DBHelper.getHelper(), new LatLng(pointFromLat, pointFromLon),
                    new LatLng(pointToLat, pointToLon));
            mListView.setAdapter(new ForecastsAdapter(ForecastActivity.this,  R.layout.forecast_list_item, routes));     */
        } else {
            actionBar.setTitle(busStop.getName());
            actionBar.setSubtitle(busStop.getDescription());
            mBusStop = busStop;
            mAsyncTask = new LoadDataAsyncTask();
            mAsyncTask.execute(busStop);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Route route = (Route) getListView().getAdapter().getItem(position);
        if (route == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(Consts.KEY_ROUTES_ID, new ArrayList<Integer>() {
            {
                add(route.getNumber());
            }
        });
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        //mTracker.sendEvent("UX", "click", getResources().getResourceName(view.getId()), null);
        switch (view.getId()) {
            case R.id.btn_repeat_load_forecast:
                if (mBusStop == null) return;
                new LoadDataAsyncTask().execute(mBusStop);
                ((View) view.getParent()).setVisibility(View.GONE);
                break;
        }
    }

    Toast toast;

    @Override
    public void onRefresh() {
        if (mMode != null) {
            mMode.finish();
        }
        if (mAsyncTask != null && mAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }
        mAsyncTask = new LoadDataAsyncTask();
        mAsyncTask.execute(mBusStop);
    }

    private class LoadDataAsyncTask extends AsyncTask<BusStop, Void, ArrayList<Route>> {
        Exception ex;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ArrayList<Route> doInBackground(BusStop... params) {
            try {
                if (params[0] != null)
                    return mBusStopController.getForecastForBusStop(params[0]);
            } catch (HttpException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                ex = e;
                if (params[0] != null && params[0] instanceof BusStop)
                    return new RouteController(ForecastActivity.this).getRoutes(params[0].getServerId());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Route> result) {
            if (ex != null) {
                findViewById(R.id.ll_no_internet_controls).setVisibility(View.VISIBLE);
                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(ForecastActivity.this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG);
                toast.show();
            }
            mSwipeRefreshLayout.setRefreshing(false);
            if (result != null) {
                ForecastsAdapter mAdapter = new ForecastsAdapter(ForecastActivity.this,
                        R.layout.forecast_list_item, result);
                mListView.setAdapter(mAdapter);
            }
            mListView.setEmptyView(findViewById(R.id.ll_no_internet_controls));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //EasyTracker.getInstance().activityStop(this);
    }

    ActionMode mMode;

    public class ModeCallback implements AbsListView.MultiChoiceModeListener {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            android.view.MenuInflater inflater = ForecastActivity.this.getMenuInflater();
            inflater.inflate(R.menu.several_routes_menu, menu);
            mode.setTitle(getString(R.string.select_several_routes));
            mMode = mode;
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
                    SparseBooleanArray array = mListView.getCheckedItemPositions();
                    ArrayList<Integer> selectedRouteNumbers = new ArrayList<>();
                    ForecastsAdapter adapter = (ForecastsAdapter) mListView.getAdapter();
                    for (int i = 0; i < array.size(); i++) {
                        if (array.valueAt(i))
                            selectedRouteNumbers.add(adapter.getItem(array.keyAt(i)).getNumber());
                    }
                    Intent intent = new Intent();
                    intent.putExtra(Consts.KEY_ROUTES_ID, selectedRouteNumbers);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode,
                                              int position, long id, boolean checked) {
            if (mListView.getCheckedItemCount() > Consts.MAX_ROUTES_ON_MAP) {
                mListView.setItemChecked(position, !checked);
                if (toastMultiSelect != null) {
                    toastMultiSelect.cancel();
                }

                toastMultiSelect = Toast.makeText(ForecastActivity.this, getString(R.string.max_select_routes), Toast.LENGTH_SHORT);
//                toastMultiSelect = Toast.makeText(ForecastActivity.this, getString(R.string.max5_select_routes), Toast.LENGTH_SHORT);
                toastMultiSelect.show();
            }
        }
    }

    public static void start(Activity activity, int busStopServerId) {
        Intent intent = new Intent(activity, ForecastActivity.class);
        intent.putExtra(KEY_SELECTED_BUS_STOP_ID, busStopServerId);
        activity.startActivityForResult(intent, MapOsmActivity.FORECAST_CODE);
    }
}