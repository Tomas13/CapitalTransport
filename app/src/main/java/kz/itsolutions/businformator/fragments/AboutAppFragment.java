package kz.itsolutions.businformator.fragments;


import android.content.pm.PackageManager;
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
public class AboutAppFragment extends Fragment {


    public AboutAppFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root_layout = inflater.inflate(R.layout.fragment_about_app, container, false);

        try {
            ((TextView) root_layout.findViewById(R.id.tv_version))
                    .setText(getActivity().getPackageManager().getPackageInfo(
                            getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return root_layout;
    }

}
