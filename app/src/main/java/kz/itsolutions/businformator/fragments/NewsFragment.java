package kz.itsolutions.businformator.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import kz.itsolutions.businformator.AstanaBusApplication;
import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.NewsAdapter;
import kz.itsolutions.businformator.model.News;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {

    TextView noNewsTextView;


    private static final String TAG = "NewsFragment";
    private static final String urlJsonObj = "http://crm.astanalrt.com/notifications/getnotifications/20";

    private RecyclerView newsRecyclerView;
    //    SwipyRefreshLayout swipeRefreshLayout;
    private List<News> newsList = new ArrayList<>();
    private NewsAdapter mAdapter;

    private ProgressBar progressBar;

    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    LinearLayoutManager mLayoutManager;

    public NewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.news_progressbar);

//        progressBar.setVisibility(View.VISIBLE);


        noNewsTextView = (TextView) view.findViewById(R.id.no_news_tv);
        newsRecyclerView = (RecyclerView) view.findViewById(R.id.news_fragment_recycler_view);
        mAdapter = new NewsAdapter(newsList);

        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        newsRecyclerView.setLayoutManager(mLayoutManager);
        newsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        newsRecyclerView.setAdapter(mAdapter);

        newsList.clear();
        makeJsonObjectRequest();

        return view;
    }


    private void makeJsonObjectRequest() {

        progressBar.setVisibility(View.VISIBLE);


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                urlJsonObj, new JSONObject(), new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                String json = response.toString();

                JSONTokener tokener = new JSONTokener(json);
                JSONObject finalResult = null;
                try {
                    finalResult = new JSONObject(tokener);

                    if (finalResult.getJSONArray("data").length() == 0) {

                        noNewsTextView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);

                    } else {

                        for (int i = 0; i < finalResult.getJSONArray("data").length(); i++) {

                            Object object = finalResult.getJSONArray("data").getJSONArray(i);
                            JSONArray jsonArray = new JSONArray(object.toString());

                            String date = jsonArray.getString(0);
                            String text = jsonArray.getString(1);
                            News movie = new News(date, text);

                            newsList.add(movie);
                        }

                        mAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                    }

                    mAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.getMessage());

                progressBar.setVisibility(View.GONE);
            }
        });

        // Adding request to request queue
        AstanaBusApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

}
