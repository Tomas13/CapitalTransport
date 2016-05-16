package kz.itsolutions.businformator.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;


import kz.itsolutions.businformator.R;

public class PrefsActivity extends PreferenceActivity {

    String oldMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        oldMap = PreferenceManager.getDefaultSharedPreferences(
                this).getString(getString(R.string.key_map), "osm");

        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_menu_bus);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!oldMap.equals(PreferenceManager.getDefaultSharedPreferences(
                this).getString(getString(R.string.key_map), "osm"))) {
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