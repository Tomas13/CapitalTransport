package kz.itsolutions.businformator.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.ScheduleInsideListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleInsideFragment extends Fragment {

     EditText etSearchRouteSchedules;
    ListView listView;
    ScheduleInsideListAdapter scheduleInsideListAdapter;

    public ScheduleInsideFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_schedule_inside, container, false);
        listView = (ListView) rootView.findViewById(R.id.list_view_schedules);

        ArrayList<String> arrayList = new ArrayList<>();
        Bundle bundle = getArguments();
        if (getArguments() != null) {
            arrayList = bundle.getStringArrayList("json_edges");
        }

        scheduleInsideListAdapter =
                new ScheduleInsideListAdapter(getActivity().getApplicationContext(),
                        arrayList);

        listView.setAdapter(scheduleInsideListAdapter);

        etSearchRouteSchedules = (EditText) rootView.findViewById(R.id.et_search_route_schedules);
        etSearchRouteSchedules.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (scheduleInsideListAdapter == null)
                    return;

                for (int i = 0; i < scheduleInsideListAdapter.getCount(); i++) {
                    if (listView.isItemChecked(i))
                        listView.setItemChecked(i, false);
                }
                scheduleInsideListAdapter.getFilter().filter(editable.toString());
            }
        });

        return rootView;
    }


}
