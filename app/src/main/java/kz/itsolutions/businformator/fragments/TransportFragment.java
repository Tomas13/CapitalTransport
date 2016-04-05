package kz.itsolutions.businformator.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.CirclePageIndicator;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.animations.ZoomOutPageTransformer;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransportFragment extends Fragment {

    private static final int NUM_PAGES = 10;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    public TransportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_trasport, container, false);

        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getActivity().getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());

        //Bind the title indicator to the adapter
        CirclePageIndicator titleIndicator = (CirclePageIndicator) rootView.findViewById(R.id.tabPageIndicator);
        if (titleIndicator != null) {
            titleIndicator.setViewPager(mPager);
        }

        return  rootView;
    }


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        Fragment fragment;

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    fragment = new FirstSliderFragment();
                    break;
                case 1:
                    fragment = new SecondSliderFragment();
                    break;
                case 2:
                    fragment = new ThirdSliderFragment();
                    break;
                case 3:
                    fragment = new FourthSliderFragment();
                    break;
                case 4:
                    fragment = new FifthSliderFragment();
                    break;
                case 5:
                    fragment = new SixthSliderFragment();
                    break;
                case 6:
                    fragment = new SeventhSliderFragment();
                    break;
                case 7:
                    fragment = new EigthSliderFragment();
                    break;
                case 8:
                    fragment = new NinthSliderFragment();
                    break;
                case 9:
                    fragment = new TenthSliderFragment();
                    break;
                default:
                    fragment = new SecondSliderFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

    }

}
