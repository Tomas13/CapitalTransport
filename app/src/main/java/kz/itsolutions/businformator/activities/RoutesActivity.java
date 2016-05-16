package kz.itsolutions.businformator.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;


import java.util.List;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.RoutesAdapter;
import kz.itsolutions.businformator.db.DBHelper;
import kz.itsolutions.businformator.model.Route;

public class RoutesActivity extends ListActivity implements AdapterView.OnItemClickListener {

    RoutesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBHelper.init(getApplicationContext());
        setContentView(R.layout.routes_activity);
        getActionBar().setTitle(getString(R.string.select_route));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        List<Route> routes = Route.getAllGroupByNumber(DBHelper.getHelper());
        mAdapter = new RoutesAdapter(this, routes, RoutesAdapter.TYPE.Widget);
        getListView().setAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, EditRouteActivity.class);
        intent.putExtra(EditRouteActivity.KEY_ROUTE_SERVER_ID, mAdapter.getItem(position).getServerId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DBHelper.release();
    }
}