package kz.itsolutions.businformator.fragments;


import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.ScheduleListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class SchedulesFragment extends Fragment {

    Fragment scheduleInsideFragment;

    public SchedulesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root_layout = inflater.inflate(R.layout.fragment_schedules, container, false);


        String[] values = new String[]{"Городские маршруты", "Экспресс маршруты",
                "Пригородные маршруты"};
//        , "Междугородние маршруты"

        ListView listView = (ListView) root_layout.findViewById(R.id.list_view_schedules);

        ScheduleListAdapter arrayAdapter = new ScheduleListAdapter(getActivity().getApplicationContext(),
                values);


        scheduleInsideFragment = new ScheduleInsideFragment();
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONArray sortedArray = new JSONArray();

                Bundle bundle = new Bundle();

                AssetManager assetManager = getActivity().getAssets();
                InputStream ims = null;
                try {
                    ims = assetManager.open("Aliya.json");

                    InputStreamReader inputStreamReader = new InputStreamReader(ims);
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    JSONArray jsonArray = new JSONArray(sb.toString());

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.getInt("group") == position) {
                            sortedArray.put(jsonObject);
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < sortedArray.length(); i++) {
                    try {
                        list.add(sortedArray.getJSONObject(i).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                bundle.putStringArrayList("json_edges", list);

                try {
                    scheduleInsideFragment.setArguments(bundle);
                }catch (IllegalStateException e){
                    Log.d("astana", "shedulefrag - " + e);
                }
//                switch (position) {
//                    case 0:
//                        getActivity().getActionBar().setSubtitle("ГОРОДСКИЕ МАРШРУТЫ");
//                        break;
//                    case 1:
//                        getActivity().getActionBar().setSubtitle("ЭКСПРЕСС МАРШРУТЫ");
//                        break;
//                    case 2:
//                        getActivity().getActionBar().setSubtitle("ПРИГОРОДНЫЕ МАРШРУТЫ");
//                        break;
//                    case 3:
//                        getActivity().getActionBar().setSubtitle("МЕЖДУГОРОДНИЕ МАРШРУТЫ");
//                        break;
//                }


//                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
                        .replace(R.id.container,
                                scheduleInsideFragment, "TAG_CHILD").commit();
            }
        });

        return root_layout;
    }


//    @Override
//    public void onDestroy() {
//        super.onDestroy();

//        Log.d("OVER", scheduleInsideFragment.isVisible() +"kl");
//        if (getChildFragmentManager().findFragmentByTag("TAG_CHILD") == null) {
//            getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null)
//                    .remove(scheduleInsideFragment).commit();
//        }
//    }
}
