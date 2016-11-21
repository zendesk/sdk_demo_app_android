package com.zendesk.rememberthedate.push;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.zendesk.rememberthedate.R;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ErrorResponseAdapter;
import com.zendesk.service.ZendeskCallback;
import com.zendesk.util.StringUtils;

import java.io.IOException;

public class GcmUtil {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Check if play services are installed and use able.
     *
     * @param activity An activity
     * @return True if the device support push notifications, false if not.
     */
    public static boolean checkPlayServices(Activity activity) {
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (errorCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(errorCode)) {
                apiAvailability.getErrorDialog(activity, errorCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(activity, R.string.push_error_device_not_compatible, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }


    static void getInstanceId(final Context context, final ZendeskCallback<String> callback) {
        final InstanceID instanceID = InstanceID.getInstance(context);

        new AsyncTask<Void, Void, Pair<String, ErrorResponse>>() {
            @Override
            protected Pair<String, ErrorResponse> doInBackground(final Void... params) {

                String identifier = null;
                ErrorResponse errorResponse = null;

                try {
                    identifier = instanceID.getToken(context.getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                } catch (IOException e) {
                    errorResponse = new ErrorResponseAdapter(e.getLocalizedMessage());
                }

                return new Pair<>(identifier, errorResponse);
            }

            @Override
            protected void onPostExecute(final Pair<String, ErrorResponse> result) {
                if (callback != null) {
                    if (StringUtils.hasLength(result.first)) {
                        callback.onSuccess(result.first);
                    } else {
                        callback.onError(result.second);
                    }
                }
            }

        }.execute();
    }
}
