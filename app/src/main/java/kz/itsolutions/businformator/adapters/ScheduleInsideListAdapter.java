package kz.itsolutions.businformator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kz.itsolutions.businformator.R;

public class ScheduleInsideListAdapter extends ArrayAdapter<String> {

    protected ScheduleFilter mFilter;

    private Context context;
    private ArrayList<String> data;
    private ArrayList<String> originalList;


    public ScheduleInsideListAdapter(Context context, ArrayList<String> values) {
        super(context, -1, values);
        this.context = context;
        this.data = values;

        this.originalList = new ArrayList<>();
        this.originalList.addAll(data);
    }

    static class ViewHolder {
        public TextView routeNumberTV;
        public TextView startTV;
        public TextView endTV;
        public TextView frwd_start;
        public TextView frwd_end;
        public TextView bkwd_start;
        public TextView bkwd_end;
        public TextView interval;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.draft, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.routeNumberTV = (TextView) rowView.findViewById(R.id.tv_route_number);
            viewHolder.startTV = (TextView) rowView.findViewById(R.id.tv_point_from);
            viewHolder.endTV = (TextView) rowView.findViewById(R.id.tv_point_to);
            viewHolder.frwd_start = (TextView) rowView.findViewById(R.id.tv_frwd_start);
            viewHolder.frwd_end = (TextView) rowView.findViewById(R.id.tv_frwd_end);
            viewHolder.bkwd_start = (TextView) rowView.findViewById(R.id.tv_bkwd_start);
            viewHolder.bkwd_end = (TextView) rowView.findViewById(R.id.tv_bkwd_end);
            viewHolder.interval = (TextView) rowView.findViewById(R.id.tv_interval);

            rowView.setTag(viewHolder);
        }


        //fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        String object = data.get(position);
        try {
            JSONObject jsonObject = new JSONObject(object);
            holder.routeNumberTV.setText(jsonObject.getString("route"));
            holder.startTV.setText(jsonObject.getString("start"));
            holder.endTV.setText(jsonObject.getString("end"));
            holder.frwd_start.setText(jsonObject.getString("frwd_start"));
            holder.frwd_end.setText(jsonObject.getString("frwd_end"));
            holder.bkwd_start.setText(jsonObject.getString("bkwd_start"));
            holder.bkwd_end.setText(jsonObject.getString("bkwd_end"));
            holder.interval.setText(jsonObject.getString("interval"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rowView;
    }


    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new ScheduleFilter();
        return mFilter;
    }

    private class ScheduleFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence value) {
            String constraint = value.toString().toLowerCase();
            FilterResults result = new FilterResults();

            if (constraint.length() > 0) {
                ArrayList<String> filteredItems = new ArrayList<>();
                for (String route : originalList) {
                    if (route.toLowerCase().contains(constraint.trim())) {
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
            data = (ArrayList<String>) results.values;
            notifyDataSetChanged();
            clear();
            for (String aData : data)
                add(aData);
            notifyDataSetInvalidated();
        }
    }
}
