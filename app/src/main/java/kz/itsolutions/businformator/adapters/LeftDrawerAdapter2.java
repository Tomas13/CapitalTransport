package kz.itsolutions.businformator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.widgets.MyTextView;

/**
 * Created by jean on 3/24/2016.
 */
public class LeftDrawerAdapter2 extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public LeftDrawerAdapter2(Context context, String[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    static class ViewHolder {
        public MyTextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.left_drawer_item, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.textView = (MyTextView) rowView.findViewById(R.id.text_left_drawer_custom);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.textView.setText(values[position]);

        return rowView;
    }

}
