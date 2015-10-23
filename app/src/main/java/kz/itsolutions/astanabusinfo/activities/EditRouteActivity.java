package kz.itsolutions.astanabusinfo.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import kz.itsolutions.astanabusinfo.R;
import kz.itsolutions.astanabusinfo.db.DBHelper;
import kz.itsolutions.astanabusinfo.model.Route;
import kz.itsolutions.astanabusinfo.utils.Consts;

public class EditRouteActivity extends SherlockFragmentActivity implements GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    public static String KEY_ROUTE_SERVER_ID = "key_route_server_id";

    SupportMapFragment mMapFragment;
    GoogleMap mMap;
    Polyline line;
    Marker markerFirst, markerSecond, newMarker;

    List<LatLng> mRoutePoints;
    List<Marker> mMarkers = new ArrayList<Marker>();

    boolean isRemoveMarkerMode, isAddMarkerMode;

    Toast mToast;
    ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_edit_activity);

        Intent intent = getIntent();
        int routeServerId = intent.getIntExtra(KEY_ROUTE_SERVER_ID, -1);

        if (routeServerId == -1) {
            Toast.makeText(this, "bad serverId", Toast.LENGTH_LONG).show();
            return;
        }

        Route route = Route.getByServerId(DBHelper.getHelper(), routeServerId);

        if (route == null) {
            Toast.makeText(this, "route not found", Toast.LENGTH_LONG).show();
            return;
        }

        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.edit_route));
        actionBar.setSubtitle(route.toString());
        actionBar.setDisplayHomeAsUpEnabled(true);

        // GoogleMap settings
        mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));
        mMap = mMapFragment.getMap();
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(Consts.DEFAULT_CITY_LOCATION, 11);
        mMap.moveCamera(yourLocation);

        drawRouteLineGoogle(route.getTrackPoints());
        mRoutePoints = route.getTrackPoints();
    }

    // Рисую линию маршрута
    private void drawRouteLineGoogle(List<LatLng> routePoints) {
        if (line != null)
            line.remove();
        PolylineOptions options = new PolylineOptions().width(3).color(Color.BLUE).geodesic(true);
        int markerId = 1;
        mMap.clear();
        mMarkers.clear();
        Marker marker;
        for (LatLng latLng : routePoints) {
            options.add(latLng);
            marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(String.valueOf(markerId))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMarkers.add(marker);
            markerId++;
        }
        line = mMap.addPolyline(options);
    }

    // Send edited route on server
    private void sendRoute() {
        showToast("Данная функция еще не реализована");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.edit_route_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_item_save_route:
                sendRoute();
                break;
            case R.id.menu_item_add_marker:
                actionBar.setSubtitle(getString(R.string.click_on_map_for_add_marker));
                isAddMarkerMode = true;
                isRemoveMarkerMode = false;
                showToast(getString(R.string.click_on_map_for_add_marker));
                break;
            case R.id.menu_item_remove_marker:
                actionBar.setSubtitle(getString(R.string.select_markers_to_remove));
                isAddMarkerMode = false;
                isRemoveMarkerMode = true;
                showToast(getString(R.string.select_markers_to_remove));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        if (mMarkers.contains(marker)) {
            reDraw();
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (isRemoveMarkerMode) {
            marker.remove();
            if (mMarkers.contains(marker)) {
                mMarkers.remove(marker);
                reDraw();
            }
        } else if (isAddMarkerMode) {
            if (markerFirst == null) {
                markerFirst = marker;
                showToast(getString(R.string.select_second_marker));
                actionBar.setSubtitle(R.string.select_second_marker);
            } else if (markerSecond == null) {
                markerSecond = marker;
                int markerFirstPosition = mMarkers.indexOf(markerFirst);
                int markerSecondPosition = mMarkers.indexOf(markerSecond);
                if (markerFirstPosition == markerSecondPosition) {
                    showToast(getString(R.string.selected_markers_equals));
                    markerSecond = null;
                    return false;
                }
                int positionToAddMarker = markerFirstPosition > markerSecondPosition ? markerSecondPosition : markerFirstPosition;
                mMarkers.add(positionToAddMarker + 1, mMap.addMarker(new MarkerOptions()
                        .position(newMarker.getPosition())
                        .title("new marker")
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
                markerFirst = null;
                markerSecond = null;
                isAddMarkerMode = false;
                newMarker = null;
                reDraw();
                actionBar.setSubtitle("");
            }
        }
        return false;
    }

    private void reDraw() {
        mRoutePoints.clear();
        for (Marker m : mMarkers) {
            mRoutePoints.add(m.getPosition());
        }
        drawRouteLineGoogle(mRoutePoints);
    }

    private void showToast(String text) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (isAddMarkerMode) {
            if (newMarker != null) {
                newMarker.remove();
                newMarker = null;
            }
            newMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("new marker")
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            showToast(getString(R.string.select_two_markers_for_add_marker));
        }
    }
}