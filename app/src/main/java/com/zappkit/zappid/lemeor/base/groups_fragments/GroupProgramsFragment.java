package com.zappkit.zappid.lemeor.base.groups_fragments;

import com.zappkit.zappid.lemeor.main_menu.fragments.programs.ProgramsFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;

public class GroupProgramsFragment extends BaseGroupFragment {
    @Override
    public void showRootFragment() {
        replaceFragment(new ProgramsFragment());
    }
}
