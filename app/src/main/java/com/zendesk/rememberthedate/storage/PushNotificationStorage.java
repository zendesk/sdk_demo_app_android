package com.zendesk.rememberthedate.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PushNotificationStorage {

    private SharedPreferences mStorage;
    private Context mContext;

    private static final String MY_DATES_STORE = "MyDates_push_storage";
    private static final String IDENTIFIER = "identifier";
    private static final String APP_VERSION = "app_version";
    
    public static final String IDENTIFIER_FALLBACK = "#noidentifier#";
    public static final int APP_VERSION_FALLBACK = Integer.MIN_VALUE;
    
    public PushNotificationStorage(Context context) {
        mStorage = context.getSharedPreferences(MY_DATES_STORE, Context.MODE_PRIVATE);
        mContext = context;
    }
    
    
    public void storePushIdentifier(String identity){
        mStorage.edit()
                .putString(IDENTIFIER, identity)
                .putInt(APP_VERSION, getAppVersion(mContext))
                .apply();
    }
    
    public String getPushIdentifier(){
        return mStorage.getString(IDENTIFIER, IDENTIFIER_FALLBACK);
    }

    public void clear(){
        mStorage.edit().clear().commit();
    }
    
    public boolean hasPushIdentifier(){
        return !IDENTIFIER_FALLBACK.equals(getPushIdentifier());
    }
    
    public boolean isAppUpdated(){
        return mStorage.getInt(APP_VERSION, APP_VERSION_FALLBACK) != getAppVersion(mContext);
    }

    private int getAppVersion(Context context) {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    
}
