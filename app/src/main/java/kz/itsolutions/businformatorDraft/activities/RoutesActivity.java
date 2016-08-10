package kz.itsolutions.businformatordraft.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import java.util.List;

import kz.itsolutions.businformatordraft.R;
import kz.itsolutions.businformatordraft.adapters.RoutesAdapter;
import kz.itsolutions.businformatordraft.db.DBHelper;
import kz.itsolutions.businformatordraft.model.Route;

public class RoutesActivity extends SherlockListActivity implements AdapterView.OnItemClickListener {

    RoutesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBHelper.init(getApplicationContext());
        setContentView(R.layout.routes_activity);
        getSupportActionBar().setTitle(getString(R.string.select_route));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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