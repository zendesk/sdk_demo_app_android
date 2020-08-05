package com.zendesk.rememberthedate;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zendesk.rememberthedate.ui.MainActivity;
import com.zendesk.util.StringUtils;

public class LocalNotification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String message = intent.getExtras().getString("message", StringUtils.EMPTY_STRING);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        final String channelId = context.getResources().getString(R.string.app_name);

        final Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_date_24dp)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context)
                .notify(0, notification);
    }
}
