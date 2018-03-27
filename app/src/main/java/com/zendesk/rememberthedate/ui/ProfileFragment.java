package com.zendesk.rememberthedate.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zendesk.rememberthedate.Global;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.UserProfile;
import com.zendesk.rememberthedate.storage.AppStorage;

public class ProfileFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Profile";

    private ImageView imageView;
    private TextView userName;
    private TextView email;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageView = view.findViewById(R.id.imageView);
        userName = view.findViewById(R.id.userName);
        email = view.findViewById(R.id.emailView);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        AppStorage storage = Global.getStorage(getContext());
        UserProfile userProfile = storage.getUserProfile();

        if (userProfile.getUri() != null) {
            ImageUtils.loadProfilePicture(getContext(), userProfile.getUri(), imageView);
        }

        userName.setText(userProfile.getName());
        email.setText(userProfile.getEmail());
    }
}

