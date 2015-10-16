package kz.itsolutions.businformator.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import kz.itsolutions.businformator.R;

public class PrefsActivity extends SherlockPreferenceActivity {

    String oldMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        oldMap = PreferenceManager.getDefaultSharedPreferences(
                this).getString(getString(R.string.key_map), "google");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_menu_bus);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!oldMap.equals(PreferenceManager.getDefaultSharedPreferences(
                this).getString(getString(R.string.key_map), "google"))) {
            Toast.makeText(this, getString(R.string.map_changed_need_restart_app), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}