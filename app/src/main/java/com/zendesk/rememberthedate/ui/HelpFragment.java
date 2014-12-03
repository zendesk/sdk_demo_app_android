package com.zendesk.rememberthedate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zendesk.rememberthedate.R;
import com.zendesk.sdk.feedback.impl.BaseZendeskFeedbackConfiguration;
import com.zendesk.sdk.feedback.ui.ContactZendeskActivity;
import com.zendesk.sdk.rating.ui.RateMyAppDialog;
import com.zendesk.sdk.requests.RequestActivity;
import com.zendesk.sdk.support.SupportActivity;

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

        rootView.findViewById(R.id.fragment_main_btn_knowledge_base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SupportActivity.class);
                startActivity(intent);
            }
        });

        rootView.findViewById(R.id.fragment_main_btn_contact_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactZendeskActivity.class);
                startActivity(intent);
            }
        });

        rootView.findViewById(R.id.fragment_main_btn_my_tickets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RequestActivity.class);
                startActivity(intent);
            }
        });

        rootView.findViewById(R.id.fragment_main_btn_rate_the_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new RateMyAppDialog.Builder(getActivity())
                        .withAndroidStoreRatingButton()
                        .withSendFeedbackButton(new BaseZendeskFeedbackConfiguration() {
                            @Override
                            public String getRequestSubject() {
                                return "Remember the date feedback";
                            }
                        })
                        .withDontRemindMeAgainButton()
                        .build().showAlways(getActivity());

            }
        });

        return rootView;
    }
}