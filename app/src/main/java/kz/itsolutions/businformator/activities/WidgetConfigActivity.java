package kz.itsolutions.businformator.activities;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;

import java.util.List;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.adapters.RoutesAdapter;
import kz.itsolutions.businformator.db.DBHelper;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.widgets.SingleWidget;

//import com.google.analytics.tracking.android.EasyTracker;
//import com.google.analytics.tracking.android.Tracker;

public class WidgetConfigActivity extends SherlockListActivity implements View.OnClickListener {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;
    RoutesAdapter mAdapter;
    Toast mToast;
//    Tracker mTracker;

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_ROUTE_NUMBER = "widget_route_number";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DBHelper.init(getApplicationContext());
//        EasyTracker.getInstance().setContext(this);
//        mTracker = EasyTracker.getTracker();
        setContentView(R.layout.widget_config_activity);
        ActionBar ab = getSupportActionBar();
        ab.setIcon(R.drawable.ic_menu_bus);
        ab.setTitle(getString(R.string.select_route_for_widget));
        Button btnOk = (Button) findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);
        // извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // и проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // формируем intent ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        // отрицательный ответ
        setResult(RESULT_CANCELED, resultValue);

        List<Route> mRoutes = Route.getAllGroupByNumber(DBHelper.getHelper());

        mAdapter = new RoutesAdapter(this, mRoutes, RoutesAdapter.TYPE.Widget);
        this.getListView().setAdapter(mAdapter);

        if (mRoutes.size() == 0) {
            findViewById(R.id.ll_no_data_for_widget).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_start_application).setOnClickListener(this);
            btnOk.setVisibility(View.GONE);
            getListView().setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
//        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DBHelper.release();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_application:
                startActivity(new Intent(this, SplashActivity.class));
                finish();
                break;
            case R.id.btn_ok:
                int position = getListView().getCheckedItemPosition();
                if (position == -1) {
                    if (mToast != null)
                        mToast.cancel();
                    mToast = Toast.makeText(this, getString(R.string.need_select_route), Toast.LENGTH_SHORT);
                    mToast.show();
                    return;
                }
                Route route = mAdapter.getItem(position);
//                mTracker.sendEvent("Route select", "Widget", String.format("%s. %s", route.getNumber(), route.getServerId()), null);
                SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt(WIDGET_ROUTE_NUMBER + widgetID, route.getNumber());
                editor.commit();
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                SingleWidget.updateWidget(this, appWidgetManager, sp, widgetID);
                // положительный ответ
                setResult(RESULT_OK, resultValue);
                finish();
                break;
        }
    }
}