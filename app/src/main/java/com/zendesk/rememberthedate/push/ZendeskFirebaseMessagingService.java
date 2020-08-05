package com.zendesk.rememberthedate.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.ui.MainActivity;
import com.zendesk.util.StringUtils;

import zendesk.core.Zendesk;
import zendesk.support.Support;
import zendesk.support.request.RequestActivity;

public class ZendeskFirebaseMessagingService extends FirebaseMessagingService {

    private static final int NOTIFICATION_ID = 134345;
    private static final String ZD_REQUEST_ID_KEY = "zendesk_sdk_request_id";
    private static final String ZD_MESSAGE_KEY = "message";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        final String requestId = remoteMessage.getData().get(ZD_REQUEST_ID_KEY);
        final String message = remoteMessage.getData().get(ZD_MESSAGE_KEY);

        if (StringUtils.hasLengthMany(requestId, message)) {
            handleZendeskSdkPush(requestId, message);
        }
    }

    private void handleZendeskSdkPush(String requestId, String message) {
        // Initialise the SDK
        // This IntentService could be called and any point. So, if the main app was killed,
        // there won't be any Zendesk login information. Moreover, we presume at this point, that
        // an valid identity was set.
        if (!Zendesk.INSTANCE.isInitialized()) {
            Context context = getApplicationContext();
            Zendesk.INSTANCE.init(context, context.getString(R.string.zd_url), context.getString(R.string.zd_appid), context.getString(R.string.zd_oauth));
            Support.INSTANCE.init(Zendesk.INSTANCE);
        }

        // If the Fragment with the pushed request id is visible,
        // this will cause a reload of the screen.
        // #refreshRequest(id) will return true if it was successful.
        if (Support.INSTANCE.refreshRequest(requestId, getApplicationContext())) {
            return;
        }

        showNotification(requestId, message);
    }

    private void showNotification(String requestId, String message) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final String channelId = getApplicationContext().getResources().getString(R.string.app_name);
        createNotificationChannel(notificationManager, channelId);

        final Intent requestIntent = getDeepLinkIntent(requestId);
        final PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, requestIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final Notification notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_date)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel(NotificationManager notificationManager, String channelId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Create the notification channel. As per the documentation, "Attempting to create an
            // existing notification channel with its original values performs no operation, so it's safe
            // to perform the above sequence of steps when starting an app."
            // The user-visible name of the channel.
            CharSequence name = getString(R.string.app_name);
            // The user-visible description of the channel.
            String description = getString(R.string.push_notification_fallback_title);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            // Configure the notification channel.
            channel.setDescription(description);
            channel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Intent getDeepLinkIntent(String requestId) {

        // Utilize SDK's deep linking functionality to get an Intent which opens a specified request.
        // We'd like to achieve a certain behaviour, if the user navigates back from the request activity.
        // Expected: [Request] --> [Request list] -> [MainActivity | HelpFragment]


        // ZendeskDeepLinking.INSTANCE.getRequestIntent automatically pushed the request list activity into
        // backstack. So we just have to add MainActivity.

        final Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra(MainActivity.EXTRA_VIEWPAGER_POSITION, MainActivity.POS_HELP);

        return RequestActivity.builder()
                .withRequestId(requestId)
                .deepLinkIntent(getApplicationContext(), mainActivity);
    }
}
