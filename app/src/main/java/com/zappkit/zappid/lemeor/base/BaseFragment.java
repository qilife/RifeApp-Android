package com.zappkit.zappid.lemeor.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zappkit.zappid.lemeor.main_menu.MainActivity;

public abstract class BaseFragment extends Fragment {
    protected View mView;
    protected int mViewId;
    protected Context mContext;

    protected abstract int initLayout();

    protected abstract void initComponents();

    protected abstract void addListener();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = initLayout();
        if (layoutId != 0) { mViewId = layoutId; }
        mView = LayoutInflater.from(getActivity()).inflate(mViewId, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponents();
        addListener();
    }

    public void setTitleToActivity(String title){
        if (mContext instanceof MainActivity) { ((MainActivity) mContext).setTitle(title); }
    }

    public void onBackToPreFragment(){
        if (getParentFragment() != null){ ((BaseGroupFragment)getParentFragment()).onBackPressed(); }
    }
}
