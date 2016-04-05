package kz.itsolutions.businformator.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import kz.itsolutions.businformator.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SchedulesFragment extends Fragment {


    public SchedulesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root_layout = inflater.inflate(R.layout.fragment_schedules, container, false);


        String[] values = new String[] { "Городские маршруты", "Экспресс маршруты",
        "Пригородные маршруты", "Междугородние маршруты"};


        ListView listView = (ListView) root_layout.findViewById(R.id.list_view_schedules);

        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, values);

        listView.setAdapter(arrayAdapter);

        return root_layout;
    }

}
