package kz.itsolutions.astanabusinfo.widgets.infoWindows;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import kz.itsolutions.astanabusinfo.R;
import kz.itsolutions.astanabusinfo.activities.ForecastActivity;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

/**
 * Created by arna on 16.11.14.
 */
public class BusStopInfoWindow extends InfoWindow {

    Activity activity;

    public BusStopInfoWindow(MapView mapView, Activity activity) {
        super(R.layout.bus_stop_window_osm, mapView);
        this.activity = activity;
    }

    public void onClose() {
    }

    public void onOpen(Object arg0) {
        final Marker marker = (Marker) arg0;
        Button btnCalculate = (Button) mView.findViewById(R.id.btn_calc_forecast);
        TextView txtTitle = (TextView) mView.findViewById(R.id.tv_bus_stop_title);
        TextView txtDescription = (TextView) mView.findViewById(R.id.tv_bus_stop_description);

        txtTitle.setText(marker.getTitle());
        txtDescription.setText(mView.getResources().getString(R.string.aside) + marker.getSubDescription());
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ForecastActivity.start(activity, Integer.parseInt(marker.getSnippet()));
            }
        });
    }
}
