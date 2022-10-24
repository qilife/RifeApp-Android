package com.zappkit.zappid.lemeor.main_menu.fragments.playlists;

import android.view.View;

import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;
import com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_frequencies.MyFrequenciesFragment;
import com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_playlists.MyPlaylistsFragment;
import com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_programs.MyProgramsFragment;

public class PlaylistFragment extends BaseFragment {

    class MyFrequencies implements View.OnClickListener {
        MyFrequencies() { }

        public void onClick(View v) {
            if (getParentFragment() != null) {
                ((BaseGroupFragment) getParentFragment()).addFragment(new MyFrequenciesFragment());
            }
        }
    }

    class MyPrograms implements View.OnClickListener {
        MyPrograms() { }

        public void onClick(View v) {
            if (getParentFragment() != null) {
                ((BaseGroupFragment) getParentFragment()).addFragment(new MyProgramsFragment());
            }
        }
    }

    class MyPlaylists implements View.OnClickListener {
        MyPlaylists() { }

        public void onClick(View v) {
            if (getParentFragment() != null) {
                ((BaseGroupFragment) getParentFragment()).addFragment(MyPlaylistsFragment.newInstance());
            }
        }
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_my_rife;
    }

    @Override
    protected void initComponents() {
        mView.findViewById(R.id.imv_my_frequency).setOnClickListener(new MyFrequencies());
        mView.findViewById(R.id.imv_my_program).setOnClickListener(new MyPrograms());
        mView.findViewById(R.id.imv_my_playlist).setOnClickListener(new MyPlaylists());
    }

    @Override
    protected void addListener() { }
}