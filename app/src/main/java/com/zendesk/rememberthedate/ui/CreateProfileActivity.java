package com.zendesk.rememberthedate.ui;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.UserProfile;
import com.zendesk.rememberthedate.push.PushUtils;
import com.zendesk.rememberthedate.storage.AppStorage;
import com.zendesk.util.StringUtils;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.model.VisitorInfo;

import java.util.List;

import zendesk.belvedere.Belvedere;
import zendesk.belvedere.Callback;
import zendesk.belvedere.MediaResult;
import zendesk.core.JwtIdentity;
import zendesk.core.Zendesk;

import static com.zendesk.rememberthedate.Global.getStorage;


public class CreateProfileActivity extends AppCompatActivity {

    static void start(Context context) {
        context.startActivity(new Intent(context, CreateProfileActivity.class));
    }

    private AppStorage storage;
    private Uri uri;
    private ImageView imageView;
    private EditText emailText, nameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.imageButton);
        nameText = findViewById(R.id.nameText);
        emailText =findViewById(R.id.emailText);

        storage = getStorage(getApplicationContext());
        uri = storage.getUserProfile().getUri();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showStoredProfile();
    }

    private void showStoredProfile() {
        UserProfile userProfile = storage.getUserProfile();

        imageView.setOnClickListener(v -> {
            Belvedere.from(getApplicationContext())
                    .document()
                    .contentType("image/*").allowMultiple(false)
                    .open(CreateProfileActivity.this);
        });

        if (userProfile.getUri() != null) {
            ImageUtils.loadProfilePicture(getApplicationContext(), userProfile.getUri(), imageView);
        }

        if (!StringUtils.hasLength(nameText.getText().toString())) {
            nameText.setText(userProfile.getName());
        }

        if (!StringUtils.hasLength(emailText.getText().toString())) {
            emailText.setText(userProfile.getEmail());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Belvedere.from(getApplicationContext()).getFilesFromActivityOnResult(requestCode, resultCode, data, new Callback<List<MediaResult>>() {
            @Override
            public void success(List<MediaResult> result) {
                if (result.size() > 0) {
                    uri = result.get(0).getUri();
                    ImageUtils.loadProfilePicture(getApplicationContext(), uri, imageView);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (item.getItemId() == R.id.action_save) {

            final String email = emailText.getText().toString();
            final String name = nameText.getText().toString();

            if (StringUtils.hasLength(email)) {

                final UserProfile user = new UserProfile(name, email, uri);
                storage.storeUserProfile(user);
                updateIdentityInSdks(user);
                finish();

            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.fragment_profile_invalid_email), Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateIdentityInSdks(UserProfile user) {

        // Update identity in Zendesk Support SDK
        Zendesk.INSTANCE.setIdentity(new JwtIdentity(user.getEmail()));

        // Register for push
        PushUtils.registerWithZendesk();

        // Init Chat SDK with an identity
        final VisitorInfo.Builder build = new VisitorInfo.Builder()
                .email(user.getEmail());

        if (StringUtils.hasLength(user.getName())) {
            build.name(user.getName());
        }

        ZopimChat.setVisitorInfo(build.build());
    }
}
