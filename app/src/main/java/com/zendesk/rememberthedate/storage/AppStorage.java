package com.zendesk.rememberthedate.storage;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zendesk.rememberthedate.Constants;
import com.zendesk.rememberthedate.model.DateModel;
import com.zendesk.rememberthedate.model.UserProfile;
import com.zendesk.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppStorage {

    private static final String REMEMBER_THE_DATE_STORE = "rtd_dates";

    // Profile keys
    private static final String NAME_KEY = "name";
    private static final String EMAIL_KEY = "email";
    private static final String IMAGE_DATA_KEY = "image_data";

    // Dates
    private static final String DATES = "dates";

    private final Gson gson;
    private final SharedPreferences storage;

    public AppStorage(Context context) {
        this.storage = context.getSharedPreferences(REMEMBER_THE_DATE_STORE, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void storeUserProfile(UserProfile user) {
        String avatarUri = null;

        if (user.getUri() != null) {
            avatarUri = user.getUri().toString();
        }

        storage.edit()
                .putString(NAME_KEY, user.getName())
                .putString(EMAIL_KEY, user.getEmail())
                .putString(IMAGE_DATA_KEY, avatarUri)
                .apply();
    }

    public UserProfile getUserProfile(){
        final String name = storage.getString(NAME_KEY, "");
        final String email = storage.getString(EMAIL_KEY, "");
        final String avatarUri = storage.getString(IMAGE_DATA_KEY, "");

        Uri uri = null;
        if (StringUtils.hasLength(avatarUri)) {
            uri = Uri.parse(avatarUri);
        }

        return new UserProfile(name, email, uri);
    }

    public void storeMapData(Map<String, DateModel> inputMap){

        String jsonString = gson.toJson(inputMap);

        storage.edit()
                .putString(DATES, jsonString)
                .apply();
    }

    public Map<String, DateModel> loadMapData(){
        final String jsonString = storage.getString(DATES, null);
        if (jsonString != null) {
            try{
                Type dateModelType = new TypeToken<HashMap<String, DateModel>>(){}.getType();
                return gson.fromJson(jsonString, dateModelType);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return Collections.emptyMap();
    }
}
