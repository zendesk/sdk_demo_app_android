package com.zendesk.rememberthedate.push;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class PushBroadcastReceiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), PushIntentService.class.getName());
        intent.setComponent(comp);
        startWakefulService(context, intent);
        setResultCode(Activity.RESULT_OK);
    }
    
}
