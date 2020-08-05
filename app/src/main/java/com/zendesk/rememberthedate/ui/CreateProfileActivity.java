package com.zendesk.rememberthedate.ui;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.UserProfile;
import com.zendesk.rememberthedate.push.PushUtils;
import com.zendesk.rememberthedate.storage.AppStorage;
import com.zendesk.util.StringUtils;

import java.util.List;

import zendesk.belvedere.Belvedere;
import zendesk.belvedere.Callback;
import zendesk.belvedere.MediaResult;
import zendesk.chat.Chat;
import zendesk.chat.Providers;
import zendesk.chat.VisitorInfo;
import zendesk.core.JwtIdentity;
import zendesk.core.Zendesk;

import static com.zendesk.rememberthedate.Global.getStorage;


public class CreateProfileActivity extends AppCompatActivity {


    private AppStorage storage;
    private Uri uri;
    private ImageView imageView;
    private EditText emailText, nameText;

    private boolean isProfileSettable;

    static void start(Context context) {
        context.startActivity(new Intent(context, CreateProfileActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bindViews();
        storage = getStorage(getApplicationContext());
        uri = storage.getUserProfile().getUri();
    }

    private void bindViews() {
        imageView = findViewById(R.id.imageButton);
        imageView.setOnLongClickListener(v -> {
            if (imageView.getDrawable() != null) {
            }
            return true;
        });
        nameText = findViewById(R.id.nameText);

        emailText = findViewById(R.id.emailText);
        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    isProfileSettable = false;
                    invalidateOptionsMenu();
                } else {
                    isProfileSettable = true;
                    invalidateOptionsMenu();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showStoredProfile();
    }

    private void showStoredProfile() {
        UserProfile userProfile = storage.getUserProfile();

        imageView.setOnClickListener(v -> Belvedere.from(getApplicationContext())
                .document()
                .contentType("image/*").allowMultiple(false)
                .open(CreateProfileActivity.this));

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
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        MenuItem saveItem = menu.findItem(R.id.action_edit);
        saveItem.setIcon(getResources().getDrawable(R.drawable.ic_save));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_edit);
        if (isProfileSettable) {
            item.setIcon(getResources().getDrawable(R.drawable.ic_save));
            item.setEnabled(true);
        } else {
            item.setIcon(getResources().getDrawable(R.drawable.ic_save_diasbled));
            item.setEnabled(false);

            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.fragment_profile_invalid_email),
                    Toast.LENGTH_LONG)
                    .show();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_edit:
                final String email = emailText.getText().toString();
                final String name = nameText.getText().toString();

                if (isProfileSettable) {
                    final UserProfile user = new UserProfile(name, email, uri);
                    storage.storeUserProfile(user);
                    updateIdentityInSdks(user);
                    finish();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateIdentityInSdks(UserProfile user) {

        // Update identity in Zendesk Support SDK
        Zendesk.INSTANCE.setIdentity(new JwtIdentity(user.getEmail()));

        // Register for push
        PushUtils.registerWithZendesk();

        // Init Chat SDK with an identity
        final VisitorInfo.Builder build = VisitorInfo.builder()
                .withEmail(user.getEmail());

        if (StringUtils.hasLength(user.getName())) {
            build.withName(user.getName());
        }

        Providers providers = Chat.INSTANCE.providers();
        if (providers != null) {
            providers.profileProvider().setVisitorInfo(build.build(), null);
        }
    }
}
