package com.zendesk.rememberthedate;

import android.app.Application;
import android.util.Log;

import com.zendesk.logger.Logger;
import com.zopim.android.sdk.api.ZopimChat;

public class Global extends Application {

    private final static String LOG_TAG = Global.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        if("replace_me_chat_account_id".equals(getString(R.string.zopim_account_id))){
            Log.w(LOG_TAG, "==============================================================================================================");
            Log.w(LOG_TAG, "Zopim chat is not connected to an account, if you wish to try chat please add your Zopim accountId to 'zd.xml'");
            Log.w(LOG_TAG, "==============================================================================================================");
        }

        ZopimChat.init(getString(R.string.zopim_account_id)).build();
    }
}
