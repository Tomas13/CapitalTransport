package kz.itsolutions.astanabusinfo.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import kz.itsolutions.astanabusinfo.R;
import kz.itsolutions.astanabusinfo.db.DBHelper;
import kz.itsolutions.astanabusinfo.model.Route;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MyFactory implements RemoteViewsFactory {

    List<Route> data;
    Context mContext;
    SimpleDateFormat sdf;
    int widgetID;

    MyFactory(Context ctx, Intent intent) {
        mContext = ctx;
        sdf = new SimpleDateFormat("HH:mm:ss");
        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        data = new ArrayList<Route>();
        DBHelper.init(mContext);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rView = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_favorite_route_item);
        rView.setTextViewText(R.id.tv_route_number, String.valueOf(data.get(position).getNumber()));
        rView.setTextViewText(R.id.tv_point_from, data.get(position).getPointFrom());
        rView.setTextViewText(R.id.tv_point_to, data.get(position).getPointTo());

        Intent clickIntent = new Intent();
        clickIntent.putExtra(MyProvider.ROUTE_NUMBER, data.get(position).getNumber());
        rView.setOnClickFillInIntent(R.id.rl_widget_item_root, clickIntent);
        return rView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        data.clear();
        List<Route> routeList = Route.getFavorites(DBHelper.getHelper());
        for (Route route : routeList) {
            data.add(route);
        }
    }

    @Override
    public void onDestroy() {
       DBHelper.release();
    }

}