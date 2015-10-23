package kz.itsolutions.astanabusinfo.widgets;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;
import kz.itsolutions.astanabusinfo.R;
import kz.itsolutions.astanabusinfo.activities.MapGoogleActivity;

public class MyProvider extends AppWidgetProvider {

    final String ACTION_ON_CLICK = "ru.startandroid.develop.p1211listwidget.itemonclick";
    final static String ROUTE_NUMBER = "route_number";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    void updateWidget(Context context, AppWidgetManager appWidgetManager,
                      int appWidgetId) {
        RemoteViews rv = new RemoteViews(context.getPackageName(),
                R.layout.widget_favorites);
        rv.setEmptyView(R.id.lvList, R.id.widget_empty_view);

        Intent configIntent = new Intent(context, MapGoogleActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, appWidgetId,
                configIntent, 0);
        rv.setOnClickPendingIntent(R.id.btn_start_application, pIntent);

        setList(rv, context, appWidgetId);

        setListClick(rv, context, appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                R.id.lvList);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setList(RemoteViews rv, Context context, int appWidgetId) {
        Intent adapter = new Intent(context, MyService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        Uri data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME));
        adapter.setData(data);
        rv.setRemoteAdapter(R.id.lvList, adapter);
    }

    void setListClick(RemoteViews rv, Context context, int appWidgetId) {
        Intent listClickIntent = new Intent(context, MyProvider.class);
        listClickIntent.setAction(ACTION_ON_CLICK);
        PendingIntent listClickPIntent = PendingIntent.getBroadcast(context, 0,
                listClickIntent, 0);
        rv.setPendingIntentTemplate(R.id.lvList, listClickPIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equalsIgnoreCase(ACTION_ON_CLICK)) {
            int routeNumber = intent.getIntExtra(ROUTE_NUMBER, -1);
            if (routeNumber != -1) {
                Intent runActivityIntent = new Intent(context, MapGoogleActivity.class);
                runActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                runActivityIntent.putExtra(SingleWidget.WIDGET_ROUTE_NUMBER, routeNumber);
                context.startActivity(runActivityIntent);
            }
        } else if (intent.getAction().equalsIgnoreCase(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            ComponentName thisAppWidget = new ComponentName(
                    context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(context);
            int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
            for (int appWidgetID : ids) {
                updateWidget(context, appWidgetManager, appWidgetID);
            }
        }
    }
}