package kz.itsolutions.businformatordraft.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import kz.itsolutions.businformatordraft.R;
import kz.itsolutions.businformatordraft.activities.MapGoogleActivity;
import kz.itsolutions.businformatordraft.activities.WidgetConfigActivity;
import kz.itsolutions.businformatordraft.db.DBHelper;
import kz.itsolutions.businformatordraft.model.Route;

import java.util.Arrays;

public class SingleWidget extends AppWidgetProvider {

    final String LOG_TAG = "myLogs";
    public static String WIDGET_BUTTON = "kz.itsolutions.businformatorDraft.WIDGET_BUTTON";
    public static String WIDGET_ROUTE_NUMBER = "widget_route_number_id";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(LOG_TAG, "onEnabled");
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));
        SharedPreferences sp = context.getSharedPreferences(WidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, sp, id);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));

        // Удаляем Preferences
        SharedPreferences.Editor editor = context.getSharedPreferences(
                WidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(WidgetConfigActivity.WIDGET_ROUTE_NUMBER + widgetID);
        }
        editor.commit();
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        if (WIDGET_BUTTON.equals(intent.getAction())) {
            Toast.makeText(context, "click", Toast.LENGTH_SHORT).show();
        }
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    SharedPreferences sp, int appWidgetId) {
        DBHelper.init(context);
        // Настраиваем внешний вид виджета
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_single);
        // Читаем параметры Preferences
        int routeNumber = sp.getInt(WidgetConfigActivity.WIDGET_ROUTE_NUMBER + appWidgetId, -1);
        if (routeNumber == -1)
        {
            Intent configIntent = new Intent(context, WidgetConfigActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, appWidgetId,
                    configIntent, 0);
            widgetView.setOnClickPendingIntent(R.id.root_widget, pIntent);
            return;
        }
        Route route = Route.getByNumber(DBHelper.getHelper(), routeNumber);

        if (route == null) return;
        widgetView.setTextViewText(R.id.tv_route_number, String.valueOf(route.getNumber()));

        Intent configIntent = new Intent(context, MapGoogleActivity.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        configIntent.putExtra(WIDGET_ROUTE_NUMBER, route.getNumber());
        PendingIntent pIntent = PendingIntent.getActivity(context, appWidgetId,
                configIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.root_widget, pIntent);

        // Обновляем виджет
        appWidgetManager.updateAppWidget(appWidgetId, widgetView);
        DBHelper.release();
    }
}
