package com.zendesk.rememberthedate.push;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.zendesk.logger.Logger;


public class ZendeskInstanceIdListenerService extends InstanceIDListenerService {

    private final static String LOG_TAG = ZendeskInstanceIdListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        Logger.d(LOG_TAG, "onTokenRefresh received");
        startService(new Intent(this, RegistrationIntentService.class));
    }

}
