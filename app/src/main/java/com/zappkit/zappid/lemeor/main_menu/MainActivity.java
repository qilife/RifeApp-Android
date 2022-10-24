package com.zappkit.zappid.lemeor.main_menu;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.zappkit.zappid.FrequencyListAdapter;
import com.zappkit.zappid.FrequencyListModel;
import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;

import android.view.View;
import android.view.View.OnClickListener;

import com.zappkit.zappid.lemeor.base.BaseGroupFragment;
import com.zappkit.zappid.lemeor.base.groups_fragments.GroupFrequenciesFragment;
import com.zappkit.zappid.lemeor.base.groups_fragments.GroupMyPlaylistFragment;
import com.zappkit.zappid.lemeor.base.groups_fragments.GroupProgramsFragment;
import com.zappkit.zappid.lemeor.tools.Constants;
import com.zappkit.zappid.views.CustomDialogMessageConfirm;

import java.util.ArrayList;
import java.util.Locale;

import static com.zappkit.zappid.lemeor.MainMenuActivity.IN_APP;

@SuppressLint({"NewApi"})
public class MainActivity extends BaseActivity implements ActionBar.TabListener {
    @SuppressLint("StaticFieldLeak")
    public static FrequencyListAdapter sFrequencyAdapter;
    private String mCurrentLang;
    public static ArrayList<FrequencyListModel> sFrequenciesList = new ArrayList<>();
    public static String sLocale;

    public Context mContext;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences sharedPreferences;
    private View mTabProgram, mTabFrequencies, mTabRifeApp, mCurrentTab;

    private BroadcastReceiver mBroadcastReceiverPurchase = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { updateView(); }
    };

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) { }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) { }

    public void sHowDialog() {
        CustomDialogMessageConfirm messageConfirm = new CustomDialogMessageConfirm(MainActivity.this);
        messageConfirm.show();
        messageConfirm.setTextButtonLeft(getString(R.string.txt_later));
        messageConfirm.setTextButtonRight(getString(R.string.txt_agree));
        messageConfirm.setTextTitle(getString(R.string.txt_title_disclaimer));
        messageConfirm.setTextContent(getString(R.string.txt_content_disclaimer));

        messageConfirm.setOnClickConfirmListener(new CustomDialogMessageConfirm.OnClickConfirmListener() {
            @Override
            public void onClick(String value) {
                SharedPreferences.Editor prefEditor = sharedPreferences.edit();
                prefEditor.putBoolean("show", true);
                prefEditor.apply();
            }
        });

    }

    @Override
    protected int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initComponents() {
        registerReceiver(mBroadcastReceiverPurchase, new IntentFilter(Constants.BROADCAST_ACTION_PURCHASED));
        sharedPreferences = getSharedPreferences("PREF", Context.MODE_PRIVATE);
        mSharedPreferences = SharedPreferenceHelper.getSharedPreferences(this);
        mCurrentLang = mSharedPreferences.getString("language", "en");
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        if (!sharedPreferences.getBoolean("show", false)) { sHowDialog(); }
        mContext = this;
        mTabProgram = findViewById(R.id.tab_program);
        mTabFrequencies = findViewById(R.id.tab_frequencies);
        mTabRifeApp = findViewById(R.id.tab_rifeapp);

        setTitle(getString(R.string.title_section1));
        mCurrentTab = mTabProgram;
        mCurrentTab.setSelected(true);

        initRightMenu();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int typePage = getIntent().getIntExtra(IN_APP, -1);
        adjustLanguage();
        updateView();
        if (typePage == 2) {
            mTabFrequencies.performClick();
        } else if (typePage == 3) {
            mTabRifeApp.performClick();
        } else {
            mTabProgram.performClick();
        }
    }

    protected void onResume() {
        super.onResume();
        if (!mCurrentLang.equals(this.mSharedPreferences.getString("language", "en"))) {
            refreshMainActivity();
        }
        updateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(mBroadcastReceiverPurchase); }
        catch (IllegalArgumentException ignored) { }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private GroupProgramsFragment mGroupProgramsFragment;
        private GroupFrequenciesFragment mGroupFrequenciesFragment;
        private GroupMyPlaylistFragment mGroupMyRifeAppFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mGroupProgramsFragment = new GroupProgramsFragment();
            mGroupFrequenciesFragment = new GroupFrequenciesFragment();
            mGroupMyRifeAppFragment = new GroupMyPlaylistFragment();
        }

        @NonNull
        public Fragment getItem(int position) {
            if (position == 0) { return mGroupProgramsFragment; }
            if (position == 1) { return mGroupFrequenciesFragment; }
            return mGroupMyRifeAppFragment;
        }

        public int getCount() {
            return 3;
        }

        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return MainActivity.this.getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return MainActivity.this.getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return MainActivity.this.getString(R.string.title_section3).toUpperCase(l);
                default:
                    return null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            BaseGroupFragment fragment = (BaseGroupFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());
            if (!fragment.onBackPressed()) { finish(); }
        }
    }

    public void addListener() {
        showNavLeft(R.drawable.ic_back, new OnClickListener() {
            @Override
            public void onClick(View view) { onBackPressed(); }
        });

        showNavRight(R.drawable.ic_menu, new OnClickListener() {
            @Override
            public void onClick(View view) { toggleMenuRight(); }
        });

        mTabProgram.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setTitle(getString(R.string.title_section1));
                if (mCurrentTab != null) { mCurrentTab.setSelected(false); }
                mCurrentTab = mTabProgram;
                mCurrentTab.setSelected(true);
                mViewPager.setCurrentItem(0);
            }
        });

        mTabFrequencies.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setTitle(getString(R.string.title_section2));
                if (mCurrentTab != null) { mCurrentTab.setSelected(false); }
                mCurrentTab = mTabFrequencies;
                mCurrentTab.setSelected(true);
                mViewPager.setCurrentItem(1);
            }
        });
    }

    private void updateView() {
        if (SharedPreferenceHelper.getInstance(this).getBool(Constants.KEY_PURCHASED)) {
            mTabRifeApp.setEnabled(true);
            mTabRifeApp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    setTitle(getString(R.string.title_section3));
                    if (mCurrentTab != null) { mCurrentTab.setSelected(false); }
                    mCurrentTab = mTabRifeApp;
                    mCurrentTab.setSelected(true);
                    mViewPager.setCurrentItem(2);
                }
            });
        } else {
            mTabRifeApp.setEnabled(true);
            mTabRifeApp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) { showSubscriptionAppDialog();
                }
            });
        }
    }

    private void adjustLanguage() {
        Locale locale = new Locale(this.mSharedPreferences.getString("language", "en"));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        sLocale = "";
        if (!mCurrentLang.equals("en")) { sLocale = "_" + mCurrentLang; }
    }

    private void refreshMainActivity() {
        finish();
        startActivity(getIntent());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mSectionsPagerAdapter.notifyDataSetChanged();
    }
}
