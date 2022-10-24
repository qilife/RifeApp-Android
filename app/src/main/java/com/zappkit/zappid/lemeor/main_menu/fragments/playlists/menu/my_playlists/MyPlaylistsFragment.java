package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_playlists;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zappkit.zappid.PlayListListAdapter;
import com.zappkit.zappid.PlayListListModel;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;
import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.lemeor.tools.Utilities;

import java.util.ArrayList;

public class MyPlaylistsFragment extends BaseFragment {
    private Button mBtnAddPlaylist;
    private DbHelper mDbHelper;
    private BroadcastReceiver editReceiver;

    public static MyPlaylistsFragment newInstance() {
        return new MyPlaylistsFragment();
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_my_playlist;
    }

    @Override
    protected void initComponents() {
        setTitleToActivity(getString(R.string.tv_my_playlist));
        mBtnAddPlaylist = mView.findViewById(R.id.btnCreateNewPlaylist);
        mDbHelper = new DbHelper(mContext);
        final ArrayList<PlayListListModel> playlists = new ArrayList<>();
        ListView listView = mView.findViewById(R.id.myplaylistmain);
        final PlayListListAdapter adapter = new PlayListListAdapter((Activity) mContext, playlists);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Utilities.addPlaylistToPlayer(mContext, playlists.get(position));
            }
        });
        Cursor cursor = this.mDbHelper.getMyPlaylists();
        if (cursor.getCount() != 0) {
            mView.findViewById(R.id.myPlaylistNoContentList).setVisibility(View.GONE);
            cursor.moveToFirst();
            do {
                PlayListListModel tempPlaylistModel = new PlayListListModel();
                tempPlaylistModel.setName(cursor.getString(cursor.getColumnIndex("name")));
                tempPlaylistModel.setNotes(cursor.getString(cursor.getColumnIndex("description")));
                tempPlaylistModel.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                tempPlaylistModel.setList(cursor.getString(cursor.getColumnIndex("list")));
                playlists.add(tempPlaylistModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
        BroadcastReceiver removeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int position = intent.getIntExtra("position", -1);
                if (position != -1 && position < playlists.size()) {
                    mDbHelper.deleteMyPlayList(playlists.get(position).getId());
                    playlists.remove(position);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(removeReceiver, new IntentFilter("removePlaylist"));
        this.editReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int pos = intent.getIntExtra("position", -1);
                PlayListListModel playListListModel = playlists.get(pos);
                if (pos != -1) {
                    ArrayList<SequenceListModel> arrayList = new ArrayList<>();
                    for (String s : playlists.get(pos).getArrayList()) {
                        String[] tempIds = s.split(",");
                        Cursor sequences = mDbHelper.getSequence(tempIds[1], Integer.parseInt(tempIds[0]));
                        if (sequences.getCount() != 0) {
                            sequences.moveToFirst();
                            SequenceListModel tempModel = new SequenceListModel();
                            tempModel.setSequenceTitle(sequences.getString(sequences.getColumnIndex("name")));
                            tempModel.setNotes("");
                            tempModel.setId(sequences.getInt(sequences.getColumnIndex("_id")));
                            tempModel.setDbId(1);
                            arrayList.add(tempModel);
                        }
                        sequences.close();
                    }
                    playListListModel.setSequenceListModels(arrayList);

                    if (getParentFragment() != null) {
                        ((BaseGroupFragment)getParentFragment()).addFragment(MyPlaylistAddFragment.newInstance(playListListModel));
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(editReceiver, new IntentFilter("editPlaylist"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(editReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(editReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setTitleToActivity(getString(R.string.title_section3));
    }

    @Override
    protected void addListener() {
        mBtnAddPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentFragment() != null) {
                    ((BaseGroupFragment)getParentFragment()).addFragment(MyPlaylistAddFragment.newInstance(null));
                }
            }
        });
    }

}
