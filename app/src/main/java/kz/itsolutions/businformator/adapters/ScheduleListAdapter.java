package kz.itsolutions.businformator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import kz.itsolutions.businformator.R;

/**
 * Created by jean on 4/22/2016.
 */
public class ScheduleListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public ScheduleListAdapter(Context context, String[] values) {
        super(context, R.layout.schedule_layout_item, values);
        this.context = context;
        this.values = values;
    }

    static class ViewHolder {
        public TextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.schedule_fragment_item, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) rowView.findViewById(R.id.schedule_frag_item);

            rowView.setTag(viewHolder);
        }

        //fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.textView.setText(values[position]);

        return rowView;
    }

}