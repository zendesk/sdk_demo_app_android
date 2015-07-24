package com.zendesk.rememberthedate.ui;


import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.zendesk.logger.Logger;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.UserProfile;
import com.zendesk.rememberthedate.storage.UserProfileStorage;
import com.zendesk.sdk.model.network.JwtIdentity;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.util.StringUtils;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.model.VisitorInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CreateProfileActivity extends ActionBarActivity {

    private UserProfileStorage mUserProfileStorage;

    private Bitmap currentBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mUserProfileStorage = new UserProfileStorage(this);
        currentBitmap = mUserProfileStorage.getProfile().getAvatar();
    }

    @Override
    protected void onResume() {
        super.onResume();

        UserProfile userProfile = mUserProfileStorage.getProfile();

        ImageButton button = (ImageButton) this.findViewById(R.id.imageButton);
        EditText nameText = (EditText) this.findViewById(R.id.nameText);
        EditText emailText = (EditText) this.findViewById(R.id.emailText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageIntent();
            }
        });

        if (currentBitmap != null) {
            button.setImageBitmap(currentBitmap);
        }

        nameText.setText(userProfile.getName());
        emailText.setText(userProfile.getEmail());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1001) {
            Bitmap bitmap = null;

            if(data.getData() != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if(data.getExtras() != null && data.getExtras().get("data") instanceof Bitmap){
                bitmap = (Bitmap) data.getExtras().get("data");

            }

            if(bitmap != null){
                currentBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false);;
                ((ImageButton) this.findViewById(R.id.imageButton)).setImageBitmap(currentBitmap);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {

            EditText nameText = (EditText) this.findViewById(R.id.nameText);
            EditText emailText = (EditText) this.findViewById(R.id.emailText);

            String email = emailText.getText().toString();

            if (StringUtils.hasLength(email)) {
                mUserProfileStorage.storeUserProfile(
                        nameText.getText().toString(),
                        email,
                        currentBitmap
                );

                final UserProfile profile = mUserProfileStorage.getProfile();
                if (StringUtils.hasLength(profile.getEmail())) {
                    Logger.i("Identity", "Setting identity");
                    ZendeskConfig.INSTANCE.setIdentity(new JwtIdentity(profile.getEmail()));

                    // Init Zopim Visitor info
                    final VisitorInfo.Builder build = new VisitorInfo.Builder()
                            .email(profile.getEmail());

                    if (StringUtils.hasLength(profile.getName())) {
                        build.name(profile.getName());
                    }

                    ZopimChat.setVisitorInfo(build.build());
                }


                finish();

            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.fragment_profile_invalid_email), Toast.LENGTH_LONG).show();

            }


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openImageIntent() {

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

        startActivityForResult(chooserIntent, 1001);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
            // Intentionally empty
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_create_profile, container, false);
        }
    }
}
