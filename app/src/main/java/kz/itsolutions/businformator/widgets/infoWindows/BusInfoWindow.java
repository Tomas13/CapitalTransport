package kz.itsolutions.businformator.widgets.infoWindows;

import android.app.Activity;
import android.widget.TextView;
import kz.itsolutions.businformator.R;

import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

/**
 * Created by arna on 16.11.14.
 */
public class BusInfoWindow extends InfoWindow {

    Activity activity;

    public BusInfoWindow(MapView mapView, Activity activity) {
        super(R.layout.bus_window_osm, mapView);
        this.activity = activity;
    }

    public void onClose() {
    }

    public void onOpen(Object arg0) {
        final Marker marker = (Marker) arg0;
        TextView txtTitle = (TextView) mView.findViewById(R.id.tv_bus_title);
        TextView txtDescription = (TextView) mView.findViewById(R.id.tv_bus_description);

        txtTitle.setText(marker.getTitle());
        txtDescription.setText(marker.getSubDescription());
    }
}
