package kz.itsolutions.businformator.activities;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import kz.itsolutions.businformator.R;

/**
 * Created by root on 9/12/17.
 */

// callback для режима MultiSelect (маршруты)
public class ModeCallback implements AbsListView.MultiChoiceModeListener {

    private MapGoogleView view;

    public ModeCallback(MapGoogleView view) {
        this.view = view;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        view.onCreateActionMode(mode, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
    }


    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_routes_on_map:
                view.showRoutesOnMap(mode, item);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode,
                                          int position, long id, boolean checked) {
        view.onItemCheckedStateChanged(mode, position, id, checked);
    }
}