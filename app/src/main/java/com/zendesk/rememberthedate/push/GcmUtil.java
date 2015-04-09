package com.zendesk.rememberthedate.push;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.zendesk.rememberthedate.R;
import com.zendesk.sdk.model.network.ErrorResponse;
import com.zendesk.sdk.network.impl.ZendeskCallback;
import com.zendesk.sdk.util.StringUtils;

import java.io.IOException;

public class GcmUtil {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Check if play services are installed and use able.
     *
     * @param activity  An activity
     * @return          True if the device support push notifications, false if not.
     */
    public static boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(activity, R.string.push_error_device_not_compatible, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    /**
     * Helper method to receive a GCM registration id.
     *
     * @param activity  An activity
     * @param callback  Callback that will deliver a push registration id or {@link com.zendesk.sdk.model.network.ErrorResponse}
     */
    public static void asyncRegister(final Activity activity, final ZendeskCallback<String> callback) {
        new AsyncTask<Void, Void, Result>() {
            @Override
            protected Result doInBackground(Void... params) {
                final Result result = new Result();
                try {

                    final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(activity.getApplicationContext());
                    final String id = gcm.register(activity.getResources().getString(R.string.push_sender_id));
                    result.identifier = id;

                } catch (final IOException ex) {
                    ex.printStackTrace();
                    result.errorResponse = new ErrorResponse() {
                        @Override
                        public boolean isNetworkError() {
                            return false;
                        }

                        @Override
                        public String getReason() {
                            return ex.getMessage();
                        }

                        @Override
                        public int getStatus() {
                            return -1;
                        }
                    };
                }
                return result;
            }

            @Override
            protected void onPostExecute(Result result) {
                if(callback != null){
                    if(StringUtils.hasLength(result.identifier)){
                        callback.onSuccess(result.identifier);
                    }else{
                        callback.onError(result.errorResponse);
                    }
                }
            }
        }.execute();
    }
    
    static class Result{
        String identifier;
        ErrorResponse errorResponse;
    }

}
