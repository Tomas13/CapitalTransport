package kz.itsolutions.astanabusinfo.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import kz.itsolutions.astanabusinfo.R;
import kz.itsolutions.astanabusinfo.model.BusStop;

import java.util.ArrayList;
import java.util.List;

public class BusStopsAdapter extends ArrayAdapter<BusStop> {

    Context context;
    Activity activity;
    int layoutResourceId;
    List<BusStop> data = null;
    private List<BusStop> originalList;
    protected BusStopFilter mFilter;

    public BusStopsAdapter(Activity context, int layoutResourceId, List<BusStop> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context.getBaseContext();
        this.activity = context;
        this.data = data;

        this.originalList = new ArrayList<>();
        this.originalList.addAll(data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.tvBusStopTitle = (TextView) row.findViewById(R.id.tv_bus_stop_title);
            holder.tvBusStopDesc = (TextView) row.findViewById(R.id.tv_bus_stop_description);
            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }

        BusStop busStop = data.get(position);
        holder.tvBusStopTitle.setText(busStop.getName());
        holder.tvBusStopDesc.setText(String.format("%s %s", context.getString(R.string.aside), busStop.getDescription()));
        return row;
    }


    @Override
    public BusStop getItem(int position) {
        return data.get(position);
    }

    static class Holder {
        TextView tvBusStopTitle;
        TextView tvBusStopDesc;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null)
            mFilter = new BusStopFilter();
        return mFilter;
    }

    private class BusStopFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence value) {
            FilterResults result = new FilterResults();
            String constraint = value == null ? "" : value.toString().toLowerCase().trim();
            if (TextUtils.isEmpty(constraint)) return result;
            if (constraint.length() > 0) {
                ArrayList<BusStop> filteredItems = new ArrayList<>();
                for (BusStop route : originalList) {
                    if (route.toString().toLowerCase().contains(constraint)) {
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
        protected void publishResults(CharSequence constraint, FilterResults results) {
            data = (ArrayList<BusStop>) results.values;
            if (data == null) data = new ArrayList<>();
           /* notifyDataSetChanged();*/
            clear();
            for (BusStop aData : data)
                add(aData);
            //notifyDataSetInvalidated();
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}