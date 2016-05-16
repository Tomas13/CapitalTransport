package kz.itsolutions.businformator;

import android.*;
import android.Manifest;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;
import com.parse.Parse;
import com.parse.PushService;

import kz.itsolutions.businformator.activities.SplashActivity;


public class AstanaBusApplication extends MultiDexApplication {

    public AstanaBusApplication() {
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Fabric.with(this, new Answers(), new Crashlytics());

        // Initialize the Parse SDK.
        Parse.initialize(this, "FfVWcrEn0AF7QnazZ6zEUzhMTpidv9vfU5ZXesuW", "V520JX0ABeonV609n0DEqXnhnldN1MfsbyPiSQCL");
        Parse.setLogLevel(Parse.LOG_LEVEL_NONE);
        // Specify an Activity to handle all pushes by default.
        PushService.setDefaultPushCallback(this, SplashActivity.class);
    }
}