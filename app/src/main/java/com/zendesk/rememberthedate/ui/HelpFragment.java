package com.zendesk.rememberthedate.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.UserProfile;
import com.zendesk.rememberthedate.storage.UserProfileStorage;
import com.zendesk.sdk.feedback.ui.ContactZendeskActivity;
import com.zendesk.sdk.requests.RequestActivity;
import com.zendesk.sdk.support.SupportActivity;
import com.zendesk.util.StringUtils;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.prechat.PreChatForm;
import com.zopim.android.sdk.prechat.ZopimChatActivity;


/**
 * A placeholder fragment containing a simple view.
 */
public class HelpFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static HelpFragment newInstance(int sectionNumber) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public HelpFragment() {
        // Intentionally empty
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final Context ctx = getActivity().getApplicationContext();

        rootView.findViewById(R.id.fragment_main_btn_knowledge_base).setOnClickListener(new AuthOnClickWrapper(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SupportActivity.Builder().show(getActivity());
            }
        }, ctx));

        rootView.findViewById(R.id.fragment_main_btn_contact_us).setOnClickListener(new AuthOnClickWrapper(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactZendeskActivity.class);
                startActivity(intent);
            }
        }, ctx));

        rootView.findViewById(R.id.fragment_main_btn_my_tickets).setOnClickListener(new AuthOnClickWrapper(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RequestActivity.class);
                startActivity(intent);
            }
        }, ctx));

        rootView.findViewById(R.id.fragment_main_btn_chat).setOnClickListener(new AuthOnClickWrapper(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                PreChatForm build = new PreChatForm.Builder()
                        .name(PreChatForm.Field.REQUIRED)
                        .email(PreChatForm.Field.REQUIRED)
                        .phoneNumber(PreChatForm.Field.OPTIONAL)
                        .message(PreChatForm.Field.OPTIONAL)
                        .build();

                ZopimChat.SessionConfig department = new ZopimChat.SessionConfig()
                        .preChatForm(build)
                        .department("The date");

                ZopimChatActivity.startActivity(getActivity(), department);
            }
        }, ctx));

        return rootView;
    }

    class AuthOnClickWrapper implements View.OnClickListener {

        private View.OnClickListener mOnClickListener;
        private UserProfileStorage mUserProfileStorage;
        private Context mContext;

        public AuthOnClickWrapper(View.OnClickListener onClickListener, Context context){
            this.mOnClickListener = onClickListener;
            this.mUserProfileStorage = new UserProfileStorage(context);
            this.mContext = context;
        }

        @Override
        public void onClick(View v) {
            final UserProfile profile = mUserProfileStorage.getProfile();

            if(StringUtils.hasLength(profile.getEmail())){
                mOnClickListener.onClick(v);
            }else{
               showDialog();
            }
        }

        private void showDialog(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.dialog_auth_title)
                    .setPositiveButton(R.string.dialog_auth_positive_btn, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startActivity(new Intent(mContext, CreateProfileActivity.class));
                        }
                    })
                    .setNegativeButton(R.string.dialog_auth_negative_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Intentionally empty
                        }
                    });
            builder.create().show();
        }
    }
}
