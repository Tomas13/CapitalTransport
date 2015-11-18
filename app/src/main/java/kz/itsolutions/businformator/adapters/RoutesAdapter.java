package kz.itsolutions.businformator.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;
import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.db.DBHelper;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.model.RouteStatistic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoutesAdapter extends ArrayAdapter<Route> {

    public static final String CODE_FILTER_FAVORITES = "#$%^";

    Context context;
    Activity activity;
    int layoutResourceId;
    List<Route> data = null;
    protected RouteFilter mFilter;
    private ArrayList<Route> originalList;
    private TYPE mType;
    private HashMap<Integer, RouteStatistic> statisticHashMap;

    public enum TYPE {
        All, Favorites, History, Widget
    }

    public RoutesAdapter(Activity context, List<Route> data, TYPE type) {
        super(context, R.layout.routes_list_item, data);
        this.layoutResourceId = R.layout.routes_list_item;
        this.context = context.getBaseContext();
        this.activity = context;
        this.data = data;

        this.originalList = new ArrayList<>();
        this.originalList.addAll(data);
        mType = type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        WeatherHolder holder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new WeatherHolder();
            holder.tvRouteStatistic = (TextView) row.findViewById(R.id.tv_route_statistic);
            holder.tvPointFrom = (TextView) row.findViewById(R.id.tv_point_from);
            holder.tvPointTo = (TextView) row.findViewById(R.id.tv_point_to);
            holder.tvNumber = (TextView) row.findViewById(R.id.tv_route_number);
            holder.tbtnFavorite = (ImageButton) row.findViewById(R.id.tbtn_favorite);
            holder.tbtnFavorite.setVisibility(View.INVISIBLE);
            holder.tbtnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = (Integer) view.getTag();
                    Route route1 = getItem(pos);
                    route1.setFavorite(!route1.isFavorite());
                    try {
                        DBHelper.getHelper().getRouteDao().update(route1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    switch (mType) {
                        case Favorites:
                            data.remove(route1);
                            originalList.remove(route1);
                            RoutesAdapter.this.notifyDataSetChanged();
                            break;
                        case All:
                        case History:
                            data.set(pos, route1);
                            break;
                    }
                    ((ImageButton) view).setImageDrawable(context.getResources().getDrawable(route1.isFavorite() ?
                            R.drawable.star_active : R.drawable.star_not_active));
                }
            });
            if (mType == TYPE.Widget || mType == TYPE.History) {
                holder.tbtnFavorite.setVisibility(View.INVISIBLE);
            }
            row.setTag(holder);
        } else {
            holder = (WeatherHolder) row.getTag();
        }

        Route route = data.get(position);
        holder.tvPointFrom.setText("Автобус №" + route.getNumber());
        //holder.tvPointTo.setText(route.getPointTo());
        holder.tvPointTo.setText("");
        holder.tvNumber.setText(String.valueOf(route.getNumber()));
        holder.tbtnFavorite.setTag(position);
        holder.tbtnFavorite.setImageDrawable(context.getResources().getDrawable(route.isFavorite() ?
                R.drawable.star_active : R.drawable.star_not_active));
        if (mType == TYPE.All && statisticHashMap != null && statisticHashMap.containsKey(route.getServerId())) {
            RouteStatistic statistic = statisticHashMap.get(route.getServerId());
            holder.tvRouteStatistic.setText(String.format(context.getString(R.string.buses_count_avg_speed),
                    statistic.getBusesCount(), statistic.getAvgSpeed()));
            holder.tvRouteStatistic.setVisibility(View.VISIBLE);
        } else {
            holder.tvRouteStatistic.setVisibility(View.GONE);
        }
        return row;
    }

    @Override
    public Route getItem(int position) {
        if (position == -1)
            return null;
        return data.get(position);
    }

    public void setData(List<Route> data) {
        this.data.clear();
        this.data.addAll(data);
        originalList.clear();
        originalList.addAll(data);
    }

    public void setRoutesStatistic(HashMap<Integer, RouteStatistic> map) {
        statisticHashMap = map;
        notifyDataSetChanged();
    }

    public List<Route> getData() {
        return this.data;
    }

    public ArrayList<Route> getOriginalData() {
        return this.originalList;
    }

    public ArrayList<Route> getFavoritesRoutes() {
        ArrayList<Route> routes = new ArrayList<>();
        for (Route route : originalList) {
            if (route.isFavorite()) {
                routes.add(route);
            }
        }
        return routes;
    }

    static class WeatherHolder {
        TextView tvPointFrom;
        TextView tvPointTo;
        TextView tvRouteStatistic;
        TextView tvNumber;
        ImageButton tbtnFavorite;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new RouteFilter();
        return mFilter;
    }

    private class RouteFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence value) {
            String constraint = value.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint.equals(CODE_FILTER_FAVORITES)) {
                ArrayList<Route> filteredItems = new ArrayList<>();
                for (Route route : originalList) {
                    if (route.isFavorite()) {
                        filteredItems.add(route);
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else if (constraint.length() > 0) {
                ArrayList<Route> filteredItems = new ArrayList<>();
                for (Route route : originalList) {
                    if (route.toString().toLowerCase().contains(constraint.trim())) {
                        filteredItems.add(route);
                    }
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = originalList;
                    result.count = originalList.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
            data = (ArrayList<Route>) results.values;
            notifyDataSetChanged();
            clear();
            for (Route aData : data)
                add(aData);
            notifyDataSetInvalidated();
        }
    }
}