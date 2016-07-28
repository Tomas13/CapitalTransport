package kz.itsolutions.businformator.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.widgets.MyTextView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Bitmap> mThumbIdsNew;
    OnItemClickListener listener;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public ImageAdapter(Context c, ArrayList<Bitmap> mThumbId2, OnItemClickListener listener2) {
        mContext = c;
        mThumbIdsNew = mThumbId2;
        listener = listener2;
    }

    public int getCount() {
        return mThumbIdsNew.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public interface OnItemClickListener {
        void onItemClick(View childView, int childAdapterPosition);
    }


    static class ViewHolder {
        public ImageView imageView;
        public ImageButton deleteImageButton;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.gridview_item, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) rowView.findViewById(R.id.imageView);
            viewHolder.deleteImageButton = (ImageButton) rowView.findViewById(R.id.delete_image_btn);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.imageView.setImageBitmap(mThumbIdsNew.get(position));

        holder.deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(v, position);

            }
        });
        return rowView;


      /*  ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(mThumbIdsNew.get(position));
        return imageView;*/
    }

}