package com.zendesk.rememberthedate;

import android.app.Application;
import android.content.Context;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.squareup.picasso.Picasso;
import com.zendesk.logger.Logger;
import com.zendesk.rememberthedate.storage.AppStorage;
import com.zendesk.util.StringUtils;

import zendesk.answerbot.AnswerBot;
import zendesk.chat.Chat;
import zendesk.core.Zendesk;
import zendesk.support.Guide;
import zendesk.support.Support;

public class Global extends Application {

    public final static String LOG_TAG = "RTD";

    private AppStorage storage;

    public static AppStorage getStorage(@Nullable Context context) {
        if (context != null && context.getApplicationContext() instanceof Global) {
            return ((Global) context.getApplicationContext()).storage;
        }

        throw new IllegalArgumentException("Can't find global Application");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initialiseFcm();

        storage = new AppStorage(this);

        Picasso.with(this).setLoggingEnabled(true);
        // Enable logging in Support and Chat SDK
        Logger.setLoggable(true);

        // Init Support SDK
        Zendesk.INSTANCE.init(this, getResources().getString(R.string.zd_url),
                getResources().getString(R.string.zd_appid),
                getResources().getString(R.string.zd_oauth));
        Support.INSTANCE.init(Zendesk.INSTANCE);
        AnswerBot.INSTANCE.init(Zendesk.INSTANCE, Guide.INSTANCE);

        // Init Chat SDK
        if ("replace_me_chat_account_id".equals(getString(R.string.zopim_account_id))) {
            Log.w(LOG_TAG, "=========================================================================================================================");
            Log.w(LOG_TAG, "Zendesk chat is not connected to an account, if you wish to try chat please add your Zendesk Chat account key to 'zd.xml'");
            Log.w(LOG_TAG, "=========================================================================================================================");
        }
        Chat.INSTANCE.init(this, getString(R.string.zopim_account_id));
    }

    private void initialiseFcm() {
        String googleApiKey = getString(R.string.google_api_key);
        String fcmApplicationId = getString(R.string.fcm_application_id);

        if (StringUtils.isEmpty(googleApiKey) || StringUtils.isEmpty(fcmApplicationId)) {
            Log.w(LOG_TAG, "============================================================================================================");
            Log.w(LOG_TAG, "Google API key and FCM application ID are not configured. If you wish to use push notifications, please add ");
            Log.w(LOG_TAG, "values for 'zdGoogleApiKey' and 'zdFcmApplicationId' to your 'gradle.properties'.");
            Log.w(LOG_TAG, "============================================================================================================");
        } else {
            FirebaseApp.initializeApp(this, new FirebaseOptions.Builder()
                    .setApiKey(googleApiKey)
                    .setApplicationId(fcmApplicationId)
                    .build());
        }
    }

}
