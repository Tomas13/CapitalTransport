package kz.itsolutions.businformatordraft;

import com.parse.Parse;
import com.parse.PushService;

import kz.itsolutions.businformatordraft.activities.SplashActivity;

public class AstanaBusApplication extends android.app.Application {

    public AstanaBusApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the Parse SDK.
        Parse.initialize(this, "FfVWcrEn0AF7QnazZ6zEUzhMTpidv9vfU5ZXesuW", "V520JX0ABeonV609n0DEqXnhnldN1MfsbyPiSQCL");
        Parse.setLogLevel(Parse.LOG_LEVEL_NONE);
        // Specify an Activity to handle all pushes by default.
        PushService.setDefaultPushCallback(this, SplashActivity.class);
    }
}