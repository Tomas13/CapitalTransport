package kz.itsolutions.businformator.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.widgets.MyTextView;

/**
 * Created by jean on 3/24/2016.
 */
public class LeftDrawerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] names;

    public LeftDrawerAdapter(Context context, String[] names) {
        super(context, R.layout.left_drawer_item, names);
        this.context = context;
        this.names = names;
    }

    static class ViewHolder {
        public MyTextView text;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService( Context.LAYOUT_INFLATER_SERVICE );;
            rowView = inflater.inflate(R.layout.left_drawer_item, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (MyTextView) rowView.findViewById(R.id.text_left_drawer_custom);
//            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        String s = names[position];
        holder.text.setText(s);


        return rowView;
    }
}
