package kz.itsolutions.astanabusinfo.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import com.viewpagerindicator.IconPagerAdapter;
import kz.itsolutions.astanabusinfo.R;

import java.util.ArrayList;
import java.util.List;

public class MyPagerAdapter extends android.support.v4.view.PagerAdapter implements IconPagerAdapter {
    private static final int[] CONTENT = new int[]{R.string.all, R.string.favorites, R.string.history};
    private static final int[] ICONS = new int[]{
            R.drawable.tab_all_routes,
            R.drawable.tab_favorites,
            R.drawable.tab_history,
    };

    List<View> pages = null;

    Context mContext;

    public MyPagerAdapter(Context context) {
        pages = new ArrayList<>();
        this.mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        View v = pages.get(position);
        collection.removeView(v);
        collection.addView(v, 0);

        return v;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(CONTENT[position % CONTENT.length]).toUpperCase();
    }

    public void addItem(View view) {
        pages.add(view);
    }

    @Override
    public int getIconResId(int index) {
        return ICONS[index];
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}

