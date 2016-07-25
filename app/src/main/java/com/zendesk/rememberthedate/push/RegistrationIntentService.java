package com.zendesk.rememberthedate.push;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.zendesk.logger.Logger;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.storage.PushNotificationStorage;
import com.zendesk.sdk.model.access.AuthenticationType;
import com.zendesk.sdk.model.push.PushRegistrationResponse;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

public class RegistrationIntentService extends IntentService {

    private final static String LOG_TAG = RegistrationIntentService.class.getSimpleName();


    public RegistrationIntentService() {
        super(RegistrationIntentService.class.getSimpleName());
    }


    public static void start(Context context){
        context.startService(new Intent(context, RegistrationIntentService.class));
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final PushNotificationStorage mPushStorage = new PushNotificationStorage(RegistrationIntentService.this);

        GcmUtil.getInstanceId(this, new ZendeskCallback<String>() {
            @Override
            public void onSuccess(final String s) {

                final boolean hasPushIdentifier = mPushStorage.hasPushIdentifier();

                if (hasPushIdentifier) {
                    ZendeskConfig.INSTANCE.disablePush(mPushStorage.getPushIdentifier(), new ZendeskCallback<Void>() {
                        @Override
                        public void onSuccess(final Void response) {
                            Logger.d(LOG_TAG, "Successfully unregistered");
                            enablePush(mPushStorage);
                        }

                        @Override
                        public void onError(final ErrorResponse errorResponse) {
                            Logger.d(LOG_TAG, "Error during unregister");
                        }
                    });
                } else {
                    enablePush(mPushStorage);
                }
            }

            @Override
            public void onError(final ErrorResponse errorResponse) {
                Logger.d(LOG_TAG, "Error getInstance: " + errorResponse.getReason());
            }
        });
    }

    void enablePush(final PushNotificationStorage pushNotificationStorage){
        GcmUtil.getInstanceId(this, new ZendeskCallback<String>() {
            @Override
            public void onSuccess(final String result) {

                final AuthenticationType authentication = ZendeskConfig.INSTANCE.getMobileSettings().getAuthenticationType();

                if (authentication != null) {
                    ZendeskConfig.INSTANCE.enablePushWithIdentifier(result, new ZendeskCallback<PushRegistrationResponse>() {
                        @Override
                        public void onSuccess(PushRegistrationResponse result) {
                            Logger.d(LOG_TAG, "Successfully sent push token to zendesk:  " + result.getIdentifier());
                            pushNotificationStorage.storePushIdentifier(result.getIdentifier());
                        }

                        @Override
                        public void onError(ErrorResponse error) {
                            pushNotificationStorage.clear();
                            Logger.e(LOG_TAG, "Failed during enabling push notifications: " + error.getReason());
                        }
                    });
                }
            }

            @Override
            public void onError(final ErrorResponse errorResponse) {
                Toast.makeText(RegistrationIntentService.this.getApplicationContext(), getResources().getString(R.string.push_error_obtain_push_id), Toast.LENGTH_LONG).show();
            }
        });
    }
}
