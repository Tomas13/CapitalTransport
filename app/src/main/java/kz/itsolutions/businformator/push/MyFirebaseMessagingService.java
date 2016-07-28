package kz.itsolutions.businformator.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import kz.itsolutions.businformator.R;
import kz.itsolutions.businformator.activities.MapGoogleActivity;
import kz.itsolutions.businformator.fragments.NewsFragment;

/**
 * Created by jean on 7/4/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        showNotification(remoteMessage.getData().get("message"));
    }

    private void showNotification(String messageBody) {
        Intent intent = new Intent(this, MapGoogleActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("menuFragment", "favoritesMenuItem");

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getNotificationIcon())
                .setColor(NotificationCompat.COLOR_DEFAULT)
                .setContentTitle("Astrabus")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.notif : R.drawable.ic_launcher;
    }
}