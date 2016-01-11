package kz.itsolutions.businformator.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.db.DBHelper;
import kz.itsolutions.businformator.model.BusStop;
import kz.itsolutions.businformator.model.Route;
import kz.itsolutions.businformator.utils.Consts;

//import com.google.analytics.tracking.android.EasyTracker;
//import com.google.analytics.tracking.android.Tracker;

public class SplashActivity extends Activity implements View.OnClickListener {
    //    Tracker mTracker;
    ProgressBar progressBar;
    SharedPreferences prefs;
    private static String KEY_IS_FIRST_LAUNCH = "first_launch_application";
    private static String KEY_NEED_UPDATE_FROM_IB = "key_need_update_from_ib";

    TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Track app opens.
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        setContentView(R.layout.splash_activity);
        findViewById(R.id.btn_repeat_load).setOnClickListener(this);
        progressBar = ((ProgressBar) findViewById(R.id.splash_progress_bar));
        tvProgress = (TextView) findViewById(R.id.tv_progress);
        prefs = getSharedPreferences(MapGoogleActivity.MAIN_PREFS, MODE_PRIVATE);
        DBHelper.init(getApplicationContext());
        SplashActivity.setIsFree();
        try {
            ((TextView) findViewById(R.id.tv_version))
                    .setText(String.format("%s %s", getString(R.string.version)
                            , this.getPackageManager().getPackageInfo(
                            this.getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new LoadDataAsyncTask().execute();
//        EasyTracker.getInstance().setContext(this);
//        mTracker = EasyTracker.getTracker();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onStart() {
        super.onStart();
        //Crashlytics.start(this);
        //Fabric.with(this, new Crashlytics());
    }

    @Override
    public void onClick(View view) {
//        mTracker.sendEvent("UX", "click", getResources().getResourceName(view.getId()), null);
        switch (view.getId()) {
            case R.id.btn_repeat_load:
                progressBar.setVisibility(View.VISIBLE);
                findViewById(R.id.ll_no_internet_controls).setVisibility(View.INVISIBLE);
                new LoadDataAsyncTask().execute();
                break;
        }
    }

    public static void updateCurrentInstallationMap(Context context) {
        String useMap = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_map), "google");
        String prevMap = ParseInstallation.getCurrentInstallation().getString("a_map");
        // if map changed, then update value in parse.com
        if (!useMap.equals(prevMap)) {
            ParseInstallation.getCurrentInstallation().put("a_map", useMap);
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }
    }

    public static void setIsFree() {
        ParseInstallation.getCurrentInstallation().put("a_free_version", Consts.IS_FREE);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public class LoadDataAsyncTask extends AsyncTask<Void, Integer, Void> {
        Exception exception;
        String mPrefix;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (Route.hasRecords(DBHelper.getHelper()) && prefs.getBoolean(KEY_NEED_UPDATE_FROM_IB, true)) {
                DBHelper.getHelper().updateRoutesFromAssets();
                prefs.edit().putBoolean(KEY_NEED_UPDATE_FROM_IB, false).apply();
            }
            if (Route.hasRecords(DBHelper.getHelper()))
                return null;
            DBHelper.getHelper().populateFromAssets(this);
            DBHelper.getHelper().updateRoutesFromAssets();
            prefs.edit().putBoolean(KEY_NEED_UPDATE_FROM_IB, false).apply();
            return null;
        }

        public void publishProgress(int prefixResId, int progress) {
            mPrefix = getString(prefixResId);
            super.publishProgress(progress);
        }

        public void publishProgress(String prefix, int progress) {
            mPrefix = prefix;
            super.publishProgress(progress);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            String percent;
            percent = progress[0] == -1 ? "" : String.format("- %s %%", String.valueOf(progress[0]));
            tvProgress.setText(String.format("%s %s", mPrefix, percent));
        }

        @Override
        protected void onPostExecute(Void result) {
            if (exception != null) {
                findViewById(R.id.ll_no_internet_controls).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                exception = null;
                return;
            }
            String useMap = PreferenceManager.getDefaultSharedPreferences(
                    SplashActivity.this).getString(getString(R.string.key_map), "google");
            Class mapActivityClass = MapGoogleActivity.class;
            if (useMap.equals("osm")) {
                mapActivityClass = MapOsmActivity.class;
            }
            Intent intent = new Intent(SplashActivity.this, mapActivityClass);
            if (!prefs.contains(KEY_IS_FIRST_LAUNCH)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(KEY_IS_FIRST_LAUNCH, false);
                editor.putBoolean(MapGoogleActivity.KEY_NOT_SHOW_VOTE_DIALOG, false);
                editor.apply();
                intent = new Intent(SplashActivity.this, TrainingActivity.class);
            }
            startActivity(intent);
            finish();
        }
    }
}
