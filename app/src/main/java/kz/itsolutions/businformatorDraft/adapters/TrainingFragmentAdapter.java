package kz.itsolutions.businformatordraft.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.viewpagerindicator.IconPagerAdapter;
import kz.itsolutions.businformatordraft.R;
import kz.itsolutions.businformatordraft.fragments.TrainingFragment;

public class TrainingFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final int[] CONTENT = new int[]{R.drawable.training_step1, R.drawable.training_step2};

    private int mCount = CONTENT.length;

    public TrainingFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return TrainingFragment.newInstance(CONTENT[position % CONTENT.length]);
    }

    @Override
    public int getIconResId(int index) {
        return 0;
//        return ICONS[index % ICONS.length];
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
        //return TestFragmentAdapter.CONTENT[position % CONTENT.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}