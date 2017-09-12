package kz.itsolutions.businformator.activities;

/**
 * Created by root on 9/12/17.
 */

public class ParameterRunnable implements Runnable {
    String mMessage;

    MapGoogleActivity view;

    public ParameterRunnable(String message, MapGoogleActivity v) {
        this.mMessage = message;
        this.view = v;
    }

    public ParameterRunnable(int stringId) {
        this.mMessage = view.getString(stringId);
    }

    @Override
    public void run() {
        view.setErrorTextMessage(mMessage);
    }
}
