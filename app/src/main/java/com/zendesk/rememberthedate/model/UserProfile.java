package com.zendesk.rememberthedate.model;

import android.net.Uri;

/**
 * Data model for a user profile
 */
public class UserProfile {

    private final String name;
    private final String email;
    private final Uri uri;

    public UserProfile(String name, String email, Uri uri) {
        this.name = name;
        this.email = email;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Uri getUri() {
        return uri;
    }
}
