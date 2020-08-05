package com.zendesk.rememberthedate.ui;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.zendesk.rememberthedate.Global;
import com.zendesk.rememberthedate.R;
import com.zendesk.rememberthedate.model.UserProfile;
import com.zendesk.rememberthedate.push.PushUtils;
import com.zendesk.rememberthedate.storage.AppStorage;
import com.zendesk.util.StringUtils;

import zendesk.chat.Chat;
import zendesk.chat.VisitorInfo;


public class MainActivity extends AppCompatActivity {

    private static final int POS_DATE_LIST = 0;
    private static final int POS_PROFILE = 1;
    public static final int POS_HELP = 2;

    public static final String EXTRA_VIEWPAGER_POSITION = "extra_viewpager_pos";

    private AppStorage storage;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setSupportActionBar(toolbar);

        storage = Global.getStorage(getApplicationContext());
        initialiseChatSdk();

        final SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.addOnPageChangeListener(new PageChangeListener(this, fab));
        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


        final int viewPagerPos = getIntent().getIntExtra(EXTRA_VIEWPAGER_POSITION, POS_DATE_LIST);
        viewPager.setCurrentItem(viewPagerPos);
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabs);
        fab = findViewById(R.id.action_bar_add);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PushUtils.checkPlayServices(this);
    }

    private void initialiseChatSdk() {
        final UserProfile profile = storage.getUserProfile();
        if (StringUtils.hasLength(profile.getEmail())) {
            // Init Zopim Visitor info
            final VisitorInfo.Builder build = VisitorInfo.builder()
                    .withEmail(profile.getEmail());

            if (StringUtils.hasLength(profile.getName())) {
                build.withName(profile.getName());
            }

            Chat.INSTANCE.providers().profileProvider().setVisitorInfo(build.build(), null);
        }
    }

    private static class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case POS_DATE_LIST: {
                    return DateFragment.newInstance();
                }
                case POS_PROFILE: {
                    return ProfileFragment.newInstance();
                }
                case POS_HELP: {
                    return HelpFragment.newInstance();
                }
            }

            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case POS_DATE_LIST: {
                    return DateFragment.FRAGMENT_TITLE;
                }
                case POS_PROFILE: {
                    return ProfileFragment.FRAGMENT_TITLE;
                }
                case POS_HELP: {
                    return HelpFragment.FRAGMENT_TITLE;
                }
            }
            return null;
        }
    }

    private static class PageChangeListener implements ViewPager.OnPageChangeListener {

        private final Activity context;
        private final FloatingActionButton fab;

        private PageChangeListener(Activity context, FloatingActionButton fab) {
            this.context = context;
            this.fab = fab;
            configureFabForDateList();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Intentionally empty
        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {

                case POS_DATE_LIST:
                    configureFabForDateList();
                    break;

                default:
                    fab.hide();
                    break;

            }
        }

        private void configureFabForDateList() {
            fab.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_add_light));
            fab.setOnClickListener(view -> EditDateActivity.start(context));
            fab.show();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Intentionally empty
        }
    }
}
