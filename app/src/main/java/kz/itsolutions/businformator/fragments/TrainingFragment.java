package kz.itsolutions.businformator.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import kz.itsolutions.businformator.R;

public final class TrainingFragment extends Fragment {
    private static final String KEY_CONTENT = "TestFragment:Content";

    public static TrainingFragment newInstance(int drawableId) {
        TrainingFragment fragment = new TrainingFragment();


        fragment.mContent = drawableId;

        return fragment;
    }

    private int mContent = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getInt(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.training_fragment, null);
        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        imageView.setImageResource(mContent);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, mContent);
    }
}
