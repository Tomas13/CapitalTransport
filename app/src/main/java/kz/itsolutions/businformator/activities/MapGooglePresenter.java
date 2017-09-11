package kz.itsolutions.businformator.activities;

import com.google.firebase.iid.FirebaseInstanceId;

import static kz.itsolutions.businformator.activities.Methods.makePostRequest;

/**
 * Created by root on 9/11/17.
 */

public class MapGooglePresenter {

    public MapGooglePresenter() {
    }

    void makePostRequestOnNewThread() {
        Thread t = new Thread(() -> makePostRequest(FirebaseInstanceId.getInstance().getToken()));
        t.start();
    }

}
