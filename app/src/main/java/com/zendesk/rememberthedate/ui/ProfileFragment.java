package com.zendesk.rememberthedate.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
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
        CardView cardView = view.findViewById(R.id.card_view);

        cardView.setOnClickListener(v->{
            CreateProfileActivity.start(getContext());
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        AppStorage storage = Global.getStorage(getContext());
        UserProfile userProfile = storage.getUserProfile();
//        Log.i("Image", userProfile.getUri().toString());

        if (userProfile.getUri() != null) {
            ImageUtils.loadProfilePicture(getContext(), userProfile.getUri(), imageView);
        }

        userName.setText(userProfile.getName());
        email.setText(userProfile.getEmail());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            userName.setTextColor(getResources().getColor(R.color.primaryTextColor, getActivity().getTheme()));
            email.setTextColor(getResources().getColor(R.color.primaryTextColor, getActivity().getTheme()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

