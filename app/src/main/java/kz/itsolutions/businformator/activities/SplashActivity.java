package kz.itsolutions.businformator.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.db.DBHelper;
import kz.itsolutions.businformator.model.Route;

import static kz.itsolutions.businformator.utils.Consts.MAIN_PREFS;


public class SplashActivity extends Activity implements View.OnClickListener {
    ProgressBar progressBar;
    SharedPreferences prefs;
//    private static String KEY_IS_FIRST_LAUNCH = "first_launch_application";
    private static String KEY_NEED_UPDATE_FROM_IB = "key_need_update_from_ib";

    TextView tvProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_activity);
        findViewById(R.id.btn_repeat_load).setOnClickListener(this);
        progressBar = ((ProgressBar) findViewById(R.id.splash_progress_bar));
        tvProgress = (TextView) findViewById(R.id.tv_progress);
        prefs = getSharedPreferences(MAIN_PREFS, MODE_PRIVATE);
        DBHelper.init(getApplicationContext());
//        SplashActivity.setIsFree();
        try {
            ((TextView) findViewById(R.id.tv_version))
                    .setText(String.format("%s %s", getString(R.string.version)
                            , this.getPackageManager().getPackageInfo(
                            this.getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new LoadDataAsyncTask().execute();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_repeat_load:
                progressBar.setVisibility(View.VISIBLE);
                findViewById(R.id.ll_no_internet_controls).setVisibility(View.INVISIBLE);
                new LoadDataAsyncTask().execute();
                break;
        }
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
                prefs.edit().putBoolean(KEY_NEED_UPDATE_FROM_IB, false).apply();
            }
            if (Route.hasRecords(DBHelper.getHelper())) return null;
            DBHelper.getHelper().populateFromAssets(this);
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
            Class mapActivityClass = MapGoogleActivity.class;
            Intent intent = new Intent(SplashActivity.this, mapActivityClass);
            startActivity(intent);
            finish();
        }
    }
}
