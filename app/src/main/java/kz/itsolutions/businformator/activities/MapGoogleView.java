package kz.itsolutions.businformator.activities;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by root on 9/12/17.
 */

public interface MapGoogleView {

    void onCreateActionMode(ActionMode mode, Menu menu);

    void showRoutesOnMap(ActionMode mode, MenuItem menuItem);

    void onItemCheckedStateChanged(ActionMode mode,
                                   int position, long id, boolean checked);
}
