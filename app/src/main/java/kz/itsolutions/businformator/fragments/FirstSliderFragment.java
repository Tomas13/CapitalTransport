package kz.itsolutions.businformator.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kz.itsolutions.businformator.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FirstSliderFragment extends Fragment {


    public FirstSliderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_first_slider, container, false);
        TextView textViewTitle = (TextView) rootView.findViewById(R.id.first_slider_title);
        textViewTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://alrt.kz/project/15"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                getActivity().startActivity(intent);
//                getActivity().getSupportFragmentManager().beginTransaction().remove(getTargetFragment()).commit();
//                getParentFragment().getActivity()
//                        .getSupportFragmentManager().beginTransaction().remove(getParentFragment())
//                        .commit();

            }
        });

        return rootView;

    }

}
