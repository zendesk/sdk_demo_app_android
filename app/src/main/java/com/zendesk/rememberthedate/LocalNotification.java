package com.zendesk.rememberthedate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zendesk.rememberthedate.ui.MainActivity;

public class LocalNotification extends BroadcastReceiver {

    NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {

        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence from = "Remember the Date";
        CharSequence message = intent.getExtras().getString("message");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        Notification notif = new Notification(R.drawable.ic_notification, message, System.currentTimeMillis());
        notif.setLatestEventInfo(context, from, message, pendingIntent);
        notif.flags = Notification.FLAG_AUTO_CANCEL;
        nm.notify((int)System.currentTimeMillis(),notif);
    }
}