package com.jwetherell.augmented_reality.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import com.jwetherell.augmented_reality.ui.IconMarker;
import com.jwetherell.augmented_reality.ui.Marker;
import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.controllers.BusController;
import kz.itsolutions.businformator.model.Bus;
import kz.itsolutions.businformator.model.BusStop;
import kz.itsolutions.businformator.model.Route;
import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class extends DataSource to fetch data from Google Places.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class BusStopsDataSource extends NetworkDataSource {

    private static Bitmap icon = null;
    private Context mContext;
    private List<BusStop> mBusStops;

    public BusStopsDataSource(Context c, Resources res, List<BusStop> items) {
        if (res == null) throw new NullPointerException();

        mBusStops = items;
        mContext = c;
        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res == null) throw new NullPointerException();

        icon = BitmapFactory.decodeResource(res, R.drawable.road_sign);
    }

    @Override
    public String createRequestURL(double lat, double lon, double alt, float radius, String locale) {
        try {
            return null;// URL + "location=" + lat + "," + lon + "&radius=" + (radius * 1000.0f) + "&types=" + TYPES + "&sensor=true&key=" + key;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public List<Marker> parse(String URL) {
        return getMarkers();
    }

    public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        try {
            for (BusStop busStop : mBusStops) {
                String name = busStop.getName();
                Marker ma = new IconMarker(name, busStop.getPointGoogle().latitude,
                        busStop.getPointGoogle().longitude, 0, Color.RED, icon);
                markers.add(ma);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return markers;
    }

    @Override
    public List<Marker> parse(JSONObject root) {
        if (root == null) throw new NullPointerException();

        JSONObject jo = null;
        JSONArray dataArray = null;
        List<Marker> markers = new ArrayList<Marker>();

        try {
            if (root.has("results")) dataArray = root.getJSONArray("results");
            if (dataArray == null) return markers;
            int top = Math.min(MAX, dataArray.length());
            for (int i = 0; i < top; i++) {
                jo = dataArray.getJSONObject(i);
                Marker ma = processJSONObject(jo);
                if (ma != null) markers.add(ma);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return markers;
    }

    private Marker processJSONObject(JSONObject jo) {
        if (jo == null) throw new NullPointerException();

        if (!jo.has("geometry")) throw new NullPointerException();

        Marker ma = null;
        try {
            Double lat = null, lon = null;

            if (!jo.isNull("geometry")) {
                JSONObject geo = jo.getJSONObject("geometry");
                JSONObject coordinates = geo.getJSONObject("location");
                lat = Double.parseDouble(coordinates.getString("lat"));
                lon = Double.parseDouble(coordinates.getString("lng"));
            }
            if (lat != null) {
                String user = jo.getString("name");

                ma = new IconMarker(user + ": " + jo.getString("name"), lat, lon, 0, Color.RED, icon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ma;
    }
}