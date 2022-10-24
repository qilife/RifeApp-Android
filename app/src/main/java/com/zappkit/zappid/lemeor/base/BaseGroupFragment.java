package com.zappkit.zappid.lemeor.base;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zappkit.zappid.R;

/**
 * Created by Admin on 3/22/2017.
 */

public abstract class BaseGroupFragment extends Fragment {
    public static final int MAIN_FRAME_ID = R.id.frm_main;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    public void addFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
                .replace(MAIN_FRAME_ID, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void addFragmentNotReloadContent(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.trans_right_to_left_in, R.anim.trans_right_to_left_out,
                        R.anim.trans_left_to_right_in, R.anim.trans_left_to_right_out)
                .add(MAIN_FRAME_ID, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void replaceFragment(Fragment fragment) {
        while (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
        }
        getChildFragmentManager().beginTransaction()
                .replace(MAIN_FRAME_ID, fragment)
                .commit();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showRootFragment();
    }

    public abstract void showRootFragment();

    public boolean onBackPressed() {
        if (isAdded() && getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getChildFragmentManager().findFragmentById(MAIN_FRAME_ID);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
