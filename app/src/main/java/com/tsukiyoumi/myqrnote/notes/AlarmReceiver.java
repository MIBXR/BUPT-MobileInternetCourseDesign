package com.tsukiyoumi.myqrnote.notes;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.tsukiyoumi.myqrnote.HomeActivity;
import com.tsukiyoumi.myqrnote.R;

public class AlarmReceiver extends BroadcastReceiver {

    private String channelId = "MyQRNote";
    private String name = "提醒";
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getExtras().getString("title");
        String content = intent.getExtras().getString("content");
        int id = intent.getExtras().getInt("id");
        Intent intent1 = new Intent(context, HomeActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent1, 0);
        intent1.putExtra("mode", 1);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(true);
            manager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title).setContentText(content).setSmallIcon(R.drawable.prim_alarm_24dp)
                .setContentIntent(pendingIntent).setAutoCancel(true).setFullScreenIntent(pendingIntent, true);

        Notification notification = builder.build();

        manager.notify(1, notification);
    }
}
