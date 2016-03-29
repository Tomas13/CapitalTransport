package kz.itsolutions.businformator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import kz.itsolutions.businformator.R;

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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.left_drawer_item, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.text_left_drawer_custom);
            textView.setText(values[position]);

            return rowView;
        }

    }
