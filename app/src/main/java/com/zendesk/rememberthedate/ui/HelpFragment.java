package com.zendesk.rememberthedate.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.zendesk.rememberthedate.BuildConfig;
import com.zendesk.rememberthedate.R;
import com.zendesk.util.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import zendesk.answerbot.AnswerBotEngine;
import zendesk.chat.ChatConfiguration;
import zendesk.chat.ChatEngine;
import zendesk.chat.PreChatFormFieldStatus;
import zendesk.configurations.Configuration;
import zendesk.core.Zendesk;
import zendesk.messaging.MessagingActivity;
import zendesk.support.CustomField;
import zendesk.support.SupportEngine;
import zendesk.support.guide.HelpCenterActivity;
import zendesk.support.request.RequestActivity;
import zendesk.support.requestlist.RequestListActivity;


/**
 * A placeholder fragment containing a simple view.
 */
public class HelpFragment extends Fragment {

    public static final String FRAGMENT_TITLE = "Help";

    private static final long TICKET_FORM_ID = 62599L;
    private static final long TICKET_FIELD_APP_VERSION = 24328555L;
    private static final long TICKET_FIELD_DEVICE_FREE_SPACE = 24274009L;

    private Button helpCenter, contactUs, requestList, chat;

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        helpCenter = view.findViewById(R.id.fragment_main_btn_knowledge_base);
        contactUs = view.findViewById(R.id.fragment_main_btn_contact_us);
        requestList = view.findViewById(R.id.fragment_main_btn_my_tickets);
        chat = view.findViewById(R.id.fragment_main_btn_chat);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        final Context context = getActivity();
        if (context != null) {
            helpCenter.setOnClickListener(new LoggedInClickListener(v -> openHelpCenter(context)));
            contactUs.setOnClickListener(new LoggedInClickListener(v -> openMessaging(context)));
            requestList.setOnClickListener(new LoggedInClickListener(v -> openRequestList(context)));
            chat.setOnClickListener(new LoggedInClickListener(v -> openChat(context)));
        }
    }

    private void openHelpCenter(Context context) {
        Configuration config = RequestActivity.builder()
                .withTicketForm(TICKET_FORM_ID, getCustomFields())
                .config();

        HelpCenterActivity.builder()
                .show(context, config);
    }

    private void openRequestList(Context context) {
        Configuration config = RequestActivity.builder()
                .withTicketForm(TICKET_FORM_ID, getCustomFields())
                .config();

        RequestListActivity.builder()
                .show(context, config);
    }

    private void openMessaging(Context context) {
        MessagingActivity.builder()
                .withEngines(AnswerBotEngine.engine(), SupportEngine.engine(), ChatEngine.engine())
                .show(context);
    }

    private void openChat(Context context) {
        ChatConfiguration chatConfiguration = ChatConfiguration.builder()
                .withNameFieldStatus(PreChatFormFieldStatus.REQUIRED)
                .withEmailFieldStatus(PreChatFormFieldStatus.REQUIRED)
                .withPhoneFieldStatus(PreChatFormFieldStatus.OPTIONAL)
                .build();

        MessagingActivity.builder()
                .withEngines(ChatEngine.engine())
                .show(context, chatConfiguration);
    }

    private List<CustomField> getCustomFields() {
        final String appVersion = String.format(
                Locale.US,
                "version_%s",
                BuildConfig.VERSION_NAME
        );

        final StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        final long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        final String freeSpace = FileUtils.humanReadableFileSize(bytesAvailable);


        final List<CustomField> customFields = new ArrayList<>();
        customFields.add(new CustomField(TICKET_FIELD_APP_VERSION, appVersion));
        customFields.add(new CustomField(TICKET_FIELD_DEVICE_FREE_SPACE, freeSpace));

        return customFields;
    }

    private static class LoggedInClickListener implements View.OnClickListener {

        final private View.OnClickListener onClickListener;

        LoggedInClickListener(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        @Override
        public void onClick(View view) {
            if (Zendesk.INSTANCE.isInitialized() && Zendesk.INSTANCE.getIdentity() != null) {
                onClickListener.onClick(view);
            } else {
                showDialog(view.getContext());
            }
        }

        private void showDialog(Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.dialog_auth_title)
                    .setPositiveButton(R.string.dialog_auth_positive_btn, (dialog, id) -> CreateProfileActivity.start(context))
                    .setNegativeButton(R.string.dialog_auth_negative_btn, null);
            builder.create().show();
        }
    }
}
