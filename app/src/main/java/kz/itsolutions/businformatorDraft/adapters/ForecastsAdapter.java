package kz.itsolutions.businformatordraft.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import kz.itsolutions.businformatordraft.R;
import kz.itsolutions.businformatordraft.controllers.RouteController;
import kz.itsolutions.businformatordraft.model.Forecast;
import kz.itsolutions.businformatordraft.model.Route;

import java.util.List;

public class ForecastsAdapter extends ArrayAdapter<Route> {

    Context context;
    Activity activity;
    int layoutResourceId;
    List<Route> data = null;
    RouteController mRouteController;

    public ForecastsAdapter(Activity context, int layoutResourceId, List<Route> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context.getBaseContext();
        this.activity = context;
        this.data = data;

        mRouteController = new RouteController(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ForecastHolder holder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ForecastHolder();
            holder.tvRouteNumber = (TextView) row.findViewById(R.id.tv_route_number);
            holder.tvBus1Time = (TextView) row.findViewById(R.id.tv_next_bus1_time);
            holder.tvBus2Time = (TextView) row.findViewById(R.id.tv_next_bus2_time);
            holder.tvBus1Distance = (TextView) row.findViewById(R.id.tv_next_bus1_distance);
            holder.tvBus2Distance = (TextView) row.findViewById(R.id.tv_next_bus2_distance);
            holder.tvPointFrom = (TextView) row.findViewById(R.id.tv_point_from);
            holder.tvPointTo = (TextView) row.findViewById(R.id.tv_point_to);

            row.setTag(holder);
        } else {
            holder = (ForecastHolder) row.getTag();
        }

        Route route = data.get(position);
        Forecast forecast = route.getForecast();
        String busTime1 = forecast == null ? "---" : formatTimeMessage(forecast.getNextBus1Distance(), forecast.getNextBus1Time(), 0);
        String busTime2 = forecast == null ? "---" : formatTimeMessage(forecast.getNextBus2Distance(), forecast.getNextBus2Time(), 0);
        String distance1 = forecast == null ? "---" : String.format("%d м.", forecast.getNextBus1Distance());
        String distance2 = forecast == null ? "---" : String.format("%d м.", forecast.getNextBus2Distance());
        holder.tvRouteNumber.setText(String.valueOf(route.getNumber()));
        holder.tvPointFrom.setText(route.getPointFrom());
        holder.tvPointTo.setText(route.getPointTo());
        holder.tvBus1Time.setText(busTime1);
        holder.tvBus2Time.setText(busTime2);
        holder.tvBus1Distance.setText(distance1);
        holder.tvBus2Distance.setText(distance2);
        if (forecast != null) {
            if (forecast.getNextBus1Distance() == 99999 || forecast.getNextBus1Distance() == 100000) {
                holder.tvBus1Distance.setVisibility(View.GONE);
            } else {
                holder.tvBus1Distance.setVisibility(View.VISIBLE);
            }
            if (forecast.getNextBus2Distance() == 99999 || forecast.getNextBus2Distance() == 100000) {
                holder.tvBus2Distance.setVisibility(View.GONE);
            } else {
                holder.tvBus2Distance.setVisibility(View.VISIBLE);
            }
        }
        int sdk = android.os.Build.VERSION.SDK_INT;

        if (position % 2 == 0) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                row.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.forecast_list_item_bg_selector1));
            } else {
                row.setBackground(context.getResources().getDrawable(R.drawable.forecast_list_item_bg_selector1));
            }
        } else {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                row.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.forecast_list_item_bg_selector2));
            } else {
                row.setBackground(context.getResources().getDrawable(R.drawable.forecast_list_item_bg_selector2));
            }
        }
        return row;
    }

    @Override
    public Route getItem(int position) {
        return data.get(position);
    }

    static class ForecastHolder {
        TextView tvRouteNumber;
        TextView tvPointFrom;
        TextView tvPointTo;
        TextView tvBus1Time, tvBus2Time;
        TextView tvBus1Distance, tvBus2Distance;
    }

    private String formatTimeMessage(int dist, int min, int sec) {
        String formattedTime = "";

        if (dist < 30) {
            formattedTime = context.getString(R.string.about_bus_stop);
        } else if (dist == 99999) {
            formattedTime = context.getString(R.string.departure_expected);
        } else if (dist >= 100000) {
            formattedTime = context.getString(R.string.no_information);
        } else if (min < 1) {
            formattedTime = context.getString(R.string.less_one_minute);
        } else if (min > 5) {
            formattedTime = "> " + String.valueOf(min) + context.getString(R.string.minute);
        } else {
            formattedTime = context.getString(R.string.about) + " " + String.valueOf(min) + " " + context.getString(R.string.minute);
        }

        return formattedTime;
    }

}