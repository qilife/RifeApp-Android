package com.zappkit.zappid.lemeor.base.groups_fragments;

import com.zappkit.zappid.lemeor.main_menu.fragments.frequency.FrequencyFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;

public class GroupFrequenciesFragment extends BaseGroupFragment {
    @Override
    public void showRootFragment() {
        replaceFragment(new FrequencyFragment());
    }
}
