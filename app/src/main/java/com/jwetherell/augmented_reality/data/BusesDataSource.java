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
public class BusesDataSource extends NetworkDataSource {

    private static Bitmap icon = null;
    private Context mContext;
    private Route mRoute;

    public BusesDataSource(Context c, Resources res, Route route) {
        if (res == null) throw new NullPointerException();

        mContext = c;
        this.mRoute = route;
        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res == null) throw new NullPointerException();

        icon = BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
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
        if (mRoute == null) return null;
        List<Bus> buses = null;
        try {
            buses = BusController.getBusInfoByRouteNumber(mRoute.getNumber());
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getMarkers(buses);
    }

    private List<Marker> getMarkers(List<Bus> buses) {
        List<Marker> markers = new ArrayList<Marker>();
        try {
            for (Bus bus : buses) {
                String name = String.format("№ %s. \n%s %s", bus.getName(), bus.getSpeed(), mContext.getString(R.string.km_hour), bus.getTime("HH:mm:ss"));
                Marker ma = new IconMarker(name, bus.getPointGoogle().latitude,
                        bus.getPointGoogle().longitude, 0, Color.RED, icon);
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