package kz.itsolutions.businformator.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.TrainingFragmentAdapter;

public class TrainingActivity extends FragmentActivity implements View.OnClickListener {
    TrainingFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;
    ImageButton btnNextStep, btnPrevStep;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_activity);
        mAdapter = new TrainingFragmentAdapter(getSupportFragmentManager());

        btnPrevStep = (ImageButton) findViewById(R.id.btn_prev_step);
        btnNextStep = (ImageButton) findViewById(R.id.btn_next_step);
        btnPrevStep.setOnClickListener(this);
        btnNextStep.setOnClickListener(this);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    btnPrevStep.setVisibility(View.INVISIBLE);
                } else {
                    btnNextStep.setVisibility(View.VISIBLE);
                    btnPrevStep.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mIndicator.setCurrentItem(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next_step:
                if (mPager.getCurrentItem() == mAdapter.getCount() - 1) {
                    //Intent intent = new Intent(TrainingActivity.this, MapGoogleActivity.class);
                    Intent intent = new Intent(TrainingActivity.this, MapOsmActivity.class);
                    intent.putExtra(MapGoogleActivity.KEY_SHOW_WELCOME_MESSAGE, true);
                    startActivity(intent);
                    finish();
                } else {
                    mIndicator.setCurrentItem(mPager.getCurrentItem() + 1);
                }
                break;
            case R.id.btn_prev_step:
                if (mPager.getCurrentItem() > 0) {
                    mIndicator.setCurrentItem(mPager.getCurrentItem() - 1);
                }
                break;
        }
    }
}