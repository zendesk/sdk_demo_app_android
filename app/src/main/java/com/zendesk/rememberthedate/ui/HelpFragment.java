package com.zendesk.rememberthedate.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zendesk.rememberthedate.BuildConfig;
import com.zendesk.rememberthedate.R;
import com.zendesk.util.FileUtils;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.prechat.PreChatForm;
import com.zopim.android.sdk.prechat.ZopimChatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import zendesk.answerbot.AnswerBotActivity;
import zendesk.commonui.UiConfig;
import zendesk.core.Zendesk;
import zendesk.support.CustomField;
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

    public static HelpFragment newInstance() {
        return new HelpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        final Context context = getContext();
        view.findViewById(R.id.fragment_main_btn_knowledge_base).setOnClickListener(new LoggedInClickListener(v -> openHelpCenter(context)));
        view.findViewById(R.id.fragment_main_btn_contact_us).setOnClickListener(new LoggedInClickListener(v -> openRequest(context)));
        view.findViewById(R.id.fragment_main_btn_my_tickets).setOnClickListener(new LoggedInClickListener(v -> openRequestList(context)));
        view.findViewById(R.id.fragment_main_btn_chat).setOnClickListener(new LoggedInClickListener(v -> openChat(context)));
        view.findViewById(R.id.fragment_main_btn_answer_bot).setOnClickListener(new LoggedInClickListener(v -> openAnswerBot(context)));
        return view;
    }

    private void openHelpCenter(Context context) {
        UiConfig config = RequestActivity.builder()
                .withTicketForm(TICKET_FORM_ID, getCustomFields())
                .config();

        HelpCenterActivity.builder()
                .show(context, config);
    }

    private void openRequestList(Context context) {
        UiConfig config = RequestActivity.builder()
                .withTicketForm(TICKET_FORM_ID, getCustomFields())
                .config();

        RequestListActivity.builder()
                .show(context, config);
    }

    private void openRequest(Context context) {
        RequestActivity.builder()
                .withTicketForm(TICKET_FORM_ID, getCustomFields())
                .show(context);
    }

    private void openChat(Context context) {
        PreChatForm build = new PreChatForm.Builder()
                .name(PreChatForm.Field.REQUIRED)
                .email(PreChatForm.Field.REQUIRED)
                .phoneNumber(PreChatForm.Field.OPTIONAL)
                .message(PreChatForm.Field.OPTIONAL)
                .build();

        ZopimChat.SessionConfig department = new ZopimChat.SessionConfig()
                .preChatForm(build)
                .department("The date");

        ZopimChatActivity.startActivity(context, department);
    }

    private void openAnswerBot(Context context) {
        AnswerBotActivity.builder().show(context);
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
