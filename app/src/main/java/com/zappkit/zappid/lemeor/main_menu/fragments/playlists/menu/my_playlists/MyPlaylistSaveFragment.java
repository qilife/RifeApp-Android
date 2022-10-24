package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_playlists;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.PlayListListModel;
import com.zappkit.zappid.PlayListSequenceListAdapter;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;

import java.util.ArrayList;

public class MyPlaylistSaveFragment extends BaseFragment {
    private DbHelper mDbHelper;
    private PlayListListModel mPlayListListModel;
    private Button mBtnAddPrograms, mBtnCancel, mBtnSavePlaylist;
    private PlayListSequenceListAdapter mAdapter;
    private ArrayList<SequenceListModel> mSequenceList;
    private BroadcastReceiver mBroadcastReceiverAddProgram;
    private BroadcastReceiver removeSeqReceiver;

    public static MyPlaylistSaveFragment newInstance(PlayListListModel playListListModel) {
        MyPlaylistSaveFragment fragment = new MyPlaylistSaveFragment();
        fragment.mPlayListListModel = playListListModel;
        return fragment;
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_my_playlist_save_new;
    }

    @Override
    protected void initComponents() {
        mDbHelper = new DbHelper(mContext);
        mBtnAddPrograms = mView.findViewById(R.id.addmorecontentBtn);
        mBtnCancel = mView.findViewById(R.id.btnCancel);
        mBtnSavePlaylist = mView.findViewById(R.id.myPlayListAddBtn);
        ListView listPlaylists = mView.findViewById(R.id.myPlaylistSeqList);

        mSequenceList = new ArrayList<>();
        if (mPlayListListModel.getSequenceListModels() != null) {
            mSequenceList.addAll(mPlayListListModel.getSequenceListModels());
        }
        mAdapter = new PlayListSequenceListAdapter((Activity) mContext, mSequenceList);
        listPlaylists.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        if (this.mSequenceList.size() != 0) {
            mView.findViewById(R.id.myPlaylistNoAddedSeqText).setVisibility(View.GONE);
        } else {
            mView.findViewById(R.id.myPlaylistNoAddedSeqText).setVisibility(View.VISIBLE);
        }

        if (this.mPlayListListModel.getId() != -1) {
            ((Button) mView.findViewById(R.id.myPlayListAddBtn)).setText(getResources().getString(R.string.myPlaylistEditBtn));
        }

        mBroadcastReceiverAddProgram = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("Program")) {
                    SequenceListModel program = (SequenceListModel) intent.getSerializableExtra("Program");
                    if (program != null) {
                        mSequenceList.add(program);
                        mAdapter.notifyDataSetChanged();
                        if (mSequenceList.size() != 0) {
                            mView.findViewById(R.id.myPlaylistNoAddedSeqText).setVisibility(View.GONE);
                        } else {
                            mView.findViewById(R.id.myPlaylistNoAddedSeqText).setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        };
        mContext.registerReceiver(mBroadcastReceiverAddProgram, new IntentFilter("addProgramToPlaylist"));
        removeSeqReceiver = new UpdateUIReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this.removeSeqReceiver, new IntentFilter("removeSeqFromPlaylist"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBroadcastReceiverAddProgram != null) {
            mContext.unregisterReceiver(mBroadcastReceiverAddProgram);
        }

        if (removeSeqReceiver != null){
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this.removeSeqReceiver);
        }
    }

    @Override
    protected void addListener() {
        mBtnAddPrograms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentFragment() != null) {
                    ((BaseGroupFragment) getParentFragment()).addFragmentNotReloadContent(MyPlaylistAddProgramFragment.newInstance());
                }
            }
        });
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackToPreFragment();
            }
        });
        mBtnSavePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPlaylistToMyPlaylists();
            }
        });
    }

    public void addPlaylistToMyPlaylists() {
        if (this.mSequenceList.size() != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            boolean firstrun = true;
            for (SequenceListModel loopModel : this.mSequenceList) {
                if (firstrun) {
                    stringBuilder.append(loopModel.getDatabaseId());
                    stringBuilder.append(",");
                    stringBuilder.append(loopModel.getIdString());
                    firstrun = false;
                } else {
                    stringBuilder.append("-");
                    stringBuilder.append(loopModel.getDatabaseId());
                    stringBuilder.append(",");
                    stringBuilder.append(loopModel.getIdString());
                }
            }
            String list = stringBuilder.toString();
            if (this.mPlayListListModel.getId() == -1) {
                mDbHelper.insertMyPlaylist(mPlayListListModel.getName(), mPlayListListModel.getNotes(), list);
            } else {
                mDbHelper.updateMyPlaylist(mPlayListListModel.getId(), mPlayListListModel.getName(), mPlayListListModel.getNotes(), list);
            }
            onBackToPreFragment();
            onBackToPreFragment();
            return;
        }
        Toast.makeText(mContext, getResources().getString(R.string.myPlaylistNoSeqAdded), Toast.LENGTH_SHORT).show();
    }

    class UpdateUIReceiver extends BroadcastReceiver {
        UpdateUIReceiver() { }

        public void onReceive(Context context, Intent intent) {
            mSequenceList.remove(intent.getIntExtra("position", 0));
            mAdapter.notifyDataSetChanged();
            if (mSequenceList.size() != 0) {
                mView.findViewById(R.id.myPlaylistNoAddedSeqText).setVisibility(View.GONE);
            } else {
                mView.findViewById(R.id.myPlaylistNoAddedSeqText).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitleToActivity(getString(R.string.tv_my_playlist));
    }
}
