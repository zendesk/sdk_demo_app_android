package com.zendesk.rememberthedate.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.zendesk.rememberthedate.model.UserProfile;
import com.zendesk.util.StringUtils;

import java.io.ByteArrayOutputStream;

/**
 * Storage for user profile
 */
public class UserProfileStorage {

    private SharedPreferences mStorage;

    private static final String MY_DATES_STORE = "MyDates";
    private static final String IMAGE_DATA_KEY = "image_data";
    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";

    public UserProfileStorage(Context context) {
        mStorage = context.getSharedPreferences(MY_DATES_STORE, Context.MODE_PRIVATE);
    }


    public void storeUserProfile(String name, String email, Bitmap avatar) {

        String base64Avatar = null;

        if (avatar != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            avatar.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();

            base64Avatar = Base64.encodeToString(b, Base64.DEFAULT);
        }

        mStorage.edit()
                .putString(NAME_KEY, name)
                .putString(EMAIL_KEY, email)
                .putString(IMAGE_DATA_KEY, base64Avatar)
                .apply();

    }

    public UserProfile getProfile() {

        UserProfile userProfile = new UserProfile();

        userProfile.setName(mStorage.getString(NAME_KEY, ""));
        userProfile.setEmail(mStorage.getString(EMAIL_KEY, ""));

        String base64Avatar = mStorage.getString(IMAGE_DATA_KEY, "");

        if (StringUtils.hasLength(base64Avatar)) {
            byte[] b = Base64.decode(base64Avatar, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
            userProfile.setAvatar(bitmap);
        }

        return userProfile;
    }
}
