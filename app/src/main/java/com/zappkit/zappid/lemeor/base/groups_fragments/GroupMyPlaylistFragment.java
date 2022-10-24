package com.zappkit.zappid.lemeor.base.groups_fragments;

import com.zappkit.zappid.lemeor.base.BaseGroupFragment;
import com.zappkit.zappid.lemeor.main_menu.fragments.playlists.PlaylistFragment;

public class GroupMyPlaylistFragment extends BaseGroupFragment {
    @Override
    public void showRootFragment() {
        replaceFragment(new PlaylistFragment());
    }
}
