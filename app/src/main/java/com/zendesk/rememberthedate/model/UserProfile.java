package com.zendesk.rememberthedate.model;

import android.graphics.Bitmap;

/**
 * Data model for a user profile
 */
public class UserProfile {

    private String mName;

    private String mEmail;

    private Bitmap mAvatar;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public void setAvatar(Bitmap avatar) {
        this.mAvatar = avatar;
    }

    public Bitmap getAvatar() {
        return mAvatar;
    }
}
