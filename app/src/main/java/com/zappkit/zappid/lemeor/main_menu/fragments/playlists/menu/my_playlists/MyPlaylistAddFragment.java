package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_playlists;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zappkit.zappid.PlayListListModel;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;

public class MyPlaylistAddFragment extends BaseFragment {
    private Button mBtnCancel, mBtnAdd;
    private EditText etNote, etName;
    private InputMethodManager imm;
    private PlayListListModel mPlayListListModel;

    public static MyPlaylistAddFragment newInstance(PlayListListModel playListListModel) {
        MyPlaylistAddFragment fragment = new MyPlaylistAddFragment();
        fragment.mPlayListListModel = playListListModel;
        return fragment;
    }

    @Override
    protected int initLayout() { return R.layout.fragment_my_playlist_add_new; }

    @Override
    protected void initComponents() {
        setTitleToActivity(getString(R.string.tv_my_playlist));
        if (mPlayListListModel == null) {
            mPlayListListModel = new PlayListListModel();
        }
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mBtnCancel = mView.findViewById(R.id.btnCancel);
        mBtnAdd = mView.findViewById(R.id.btnAdd);
        etName = mView.findViewById(R.id.editnewplaylistname);
        etNote = mView.findViewById(R.id.editnewplaylistnotes);

        if (mPlayListListModel.getId() != -1) {
            etName.setText(mPlayListListModel.getName());
            etNote.setText(mPlayListListModel.getNotes());
        }
    }

    @Override
    protected void addListener() {
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackToPreFragment();
            }
        });
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String playlistName = etName.getText().toString();
                String playlistNote = etNote.getText().toString();
                mPlayListListModel.setName(playlistName);
                mPlayListListModel.setNotes(playlistNote);
                if (playlistName.equals("")) {
                    Toast.makeText(mContext, getResources().getString(R.string.myPlaylistMissingName), Toast.LENGTH_SHORT).show();
                    return;
                }
                etName.clearFocus();
                etNote.clearFocus();
                imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
                if (getParentFragment() != null) {
                    ((BaseGroupFragment) getParentFragment()).addFragmentNotReloadContent(MyPlaylistSaveFragment.newInstance(mPlayListListModel));
                }
            }
        });
    }
}
