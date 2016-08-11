package com.zendesk.rememberthedate.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.zendesk.logger.Logger;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.ui.MainActivity;
import com.zendesk.sdk.deeplinking.ZendeskDeepLinking;
import com.zendesk.sdk.model.request.CommentResponse;
import com.zendesk.sdk.model.request.CommentsResponse;
import com.zendesk.sdk.model.request.Request;
import com.zendesk.sdk.model.request.User;
import com.zendesk.sdk.network.RequestProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.sdk.network.impl.ZendeskPicassoProvider;
import com.zendesk.sdk.ui.ZendeskPicassoTransformationFactory;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;
import com.zendesk.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ZendeskGcmListenerService extends GcmListenerService {

    private final static String LOG_TAG = ZendeskGcmListenerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 134345;
    public static final String ZD_REQUEST_ID_EXTRA = "zendesk_sdk_request_id";


    @Override
    public void onMessageReceived(final String from, final Bundle data) {
        super.onMessageReceived(from, data);


        final String requestId = data.getString(ZD_REQUEST_ID_EXTRA);

        // Check if a valid request id was provided
        if(!StringUtils.hasLength(requestId)){
            return;
        }

        // If the Fragment with the pushed request id is visible,
        // this will cause a reload of the screen.
        // #refreshComments(id) will return true if it was successful.
        if(ZendeskDeepLinking.INSTANCE.refreshComments(requestId)){
            return;
        }

        // Initialise the SDK
        // This IntentService could be called and any point. So, if the main app was killed,
        // there won't be any Zendesk login information. Moreover, we presume at this point, that
        // an valid identity was set.
        if(!ZendeskConfig.INSTANCE.isInitialized()){
            ZendeskConfig.INSTANCE.init(this, getResources().getString(R.string.zd_url), getResources().getString(R.string.zd_appid), getResources().getString(R.string.zd_oauth));
        }

        final RequestProvider requestProvider = ZendeskConfig.INSTANCE.provider().requestProvider();

        // To take advantage of the highly customable notifications on Android, we will try to download
        // some extra information. First we will download all comments associated with this request.
        requestProvider.getComments(requestId, new ZendeskCallback<CommentsResponse>() {
            @Override
            public void onSuccess(final CommentsResponse result) {

                // Search for the newest comment.
                final CommentResponse newestResponse = getNewestResponse(result.getComments());
                if (newestResponse == null) {
                    Logger.d(LOG_TAG, "No comments with requId: " + requestId);
                    fallbackNotification(requestId);
                    return;
                }

                // Check if there is a valid user, associated with user id.
                final User user = findUser(result.getUsers(), newestResponse.getAuthorId());
                if (user == null) {
                    Logger.d(LOG_TAG, "No user with authorId: " + newestResponse.getAuthorId());
                    fallbackNotification(requestId);
                    return;
                }

                // Download more information about the request itself.
                requestProvider.getRequest(requestId, new ZendeskCallback<Request>() {
                    @Override
                    public void onSuccess(Request request) {
                        showFullNotification(newestResponse, request, user);
                    }

                    @Override
                    public void onError(ErrorResponse error) {
                        fallbackNotification(requestId);
                    }
                });

            }

            @Override
            public void onError(ErrorResponse error) {
                fallbackNotification(requestId);
            }
        });

    }

    private void showFullNotification(final CommentResponse commentResponse, final Request request, final User user){

        // Download agent's avatar picture to show it in the notification. Utilize Picasso to get
        // the agent's picture as Bitmap.

        final int dp = (int) (getResources().getDimension(com.zendesk.sdk.R.dimen.view_request_comment_avatar_size) / getResources().getDisplayMetrics().density);
        RequestCreator requestCreator = null;

        if (user.getPhoto() != null) {
            requestCreator = ZendeskPicassoProvider.getInstance(getApplicationContext())
                    .load(user.getPhoto().getContentUrl())
                    .error(com.zendesk.sdk.R.drawable.zd_user_default_avatar)
                    .transform(ZendeskPicassoTransformationFactory.INSTANCE.getRoundedTransformation((2 * dp), 0));
        } else {
            requestCreator = ZendeskPicassoProvider.getInstance(getApplicationContext())
                    .load(com.zendesk.sdk.R.drawable.zd_user_default_avatar)
                    .transform(ZendeskPicassoTransformationFactory.INSTANCE.getRoundedTransformation((2 * dp), 0));
        }

        requestCreator.into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                showFullFeaturedNotification(user, commentResponse, request, bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                showFullFeaturedNotification(user, commentResponse, request, BitmapFactory.decodeResource(getResources(), R.drawable.ic_conversations));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Intentionally empty
            }
        });
    }


    private void showFullFeaturedNotification(User user, CommentResponse commentResponse, Request request, Bitmap bitmap){

        // Expected behaviour: Updated requests should get one notification each. Every new comment on
        // an already shown notification should update the notification.
        // Therefore we will use request.getId().hashCode() as request code and PendingIntent.FLAG_UPDATE_CURRENT as flag
        // for the PendingIntent.
        // Further information: http://developer.android.com/reference/android/app/PendingIntent.html

        // Create a PendingIntent, that will lead the user to the pushed request, if he taps on notification.
        final Intent deepLinkIntent = getDeepLinkIntent(commentResponse.getRequestId(), request.getSubject());
        final PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), request.getId().hashCode(), deepLinkIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Format the shown notification text. ("[user]: [message]")
        // User string should be bold.
        final SpannableString spannableString;
        if (StringUtils.hasLength(user.getName())) {
            spannableString = new SpannableString(String.format("%s:  %s", user.getName(), commentResponse.getBody()));
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, user.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        } else {
            spannableString = new SpannableString(commentResponse.getBody());

        }

        // Put everything together.
        final Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(request.getSubject())
                .setContentText(spannableString)
                .setSmallIcon(R.drawable.ic_conversations)
                .setLargeIcon(bitmap)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(spannableString))
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();


        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Show the notification.
        // Again, use request.getId().hashCode() as notification id.
        mNotificationManager.notify(request.getId().hashCode(), notification);
    }



    private void fallbackNotification(String requestId){

        // If an error occurs during downloading request information, a simple notification will be shown.

        final Intent requestIntent = getDeepLinkIntent(requestId, null);
        final PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, requestIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(com.zendesk.sdk.R.drawable.ic_list_light)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.push_notification_fallback_title))
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build();

        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Intent getDeepLinkIntent(String requestId, String subject){

        // Utilize SDK's deep linking functionality to get an Intent which opens a specified request.
        // We'd like to achieve a certain behaviour, if the user navigates back from the request activity.
        // Expected: [Request] --> [Request list] -> [MainActivity | HelpFragment]


        // ZendeskDeepLinking.INSTANCE.getRequestIntent automatically pushed the request list activity into
        // backstack. So we just have to add MainActivity.

        final Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra(MainActivity.EXTRA_VIEWPAGER_POSITION, MainActivity.VIEWPAGER_POS_HELP);

        final ArrayList<Intent> backStackItems = new ArrayList<>();
        backStackItems.add(mainActivity);

        return ZendeskDeepLinking.INSTANCE.getRequestIntent(getApplicationContext(), requestId, subject, backStackItems, mainActivity);
    }


    private CommentResponse getNewestResponse(List<CommentResponse> comments){
        return Collections.max(new ArrayList<>(comments), new Comparator<CommentResponse>() {
            @Override
            public int compare(CommentResponse lhs, CommentResponse rhs) {
                return lhs.getCreatedAt().compareTo(rhs.getCreatedAt());
            }
        });
    }


    private User findUser(List<User> users, long userId){
        for(User user : users){
            if(user.getId() == userId){
                return user;
            }
        }
        return null;
    }
}
