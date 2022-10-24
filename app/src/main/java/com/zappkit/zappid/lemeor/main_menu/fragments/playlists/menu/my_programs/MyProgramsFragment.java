package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_programs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.MySequenceListAdapter;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;
import com.zappkit.zappid.lemeor.tools.Utilities;

import java.util.ArrayList;

public class MyProgramsFragment extends BaseFragment {

    private DbHelper mDbHelper;
    private BroadcastReceiver editReceiver;
    private BroadcastReceiver removeReceiver;
    private Button btnAdd;

    @Override
    protected int initLayout() { return R.layout.fragment_my_program; }

    @Override
    protected void initComponents() {
        setTitleToActivity(getString(R.string.mysequences));
        btnAdd = mView.findViewById(R.id.mySequences_btn);
        mDbHelper = new DbHelper(mContext);
        ListView mainMySeqList = mView.findViewById(R.id.sequence_list_myzapp);
        final ArrayList<SequenceListModel> sequences = new ArrayList<>();
        final MySequenceListAdapter adapter = new MySequenceListAdapter((Activity) mContext, sequences);
        mainMySeqList.setAdapter(adapter);
        Cursor mySequences = mDbHelper.getMySequences();
        if (mySequences.getCount() != 0) {
            mView.findViewById(R.id.mySeqNoContent).setVisibility(View.GONE);
            mySequences.moveToFirst();
            do {
                SequenceListModel tempModel = new SequenceListModel();
                tempModel.setDbId(2);
                tempModel.setId(mySequences.getInt(mySequences.getColumnIndex("_id")));
                tempModel.setSequenceTitle(mySequences.getString(mySequences.getColumnIndex("name")));
                tempModel.setFrequecyList(mySequences.getString(mySequences.getColumnIndex("list")));
                tempModel.setNotes(mySequences.getString(mySequences.getColumnIndex("description")));
                sequences.add(tempModel);
            } while (mySequences.moveToNext());
            adapter.notifyDataSetChanged();
        }
        mySequences.close();

        mainMySeqList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Utilities.addProgramToPlayer(mContext, sequences.get(position).getIdString(), sequences.get(position).getDatabaseId());
            }
        });

        removeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int position = intent.getIntExtra("position", -1);
                if (position != -1) {
                    MyProgramsFragment.this.mDbHelper.deleteMySequence(sequences.get(position).getId());
                    sequences.remove(position);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this.removeReceiver, new IntentFilter("removeSeq"));
        editReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int pos = intent.getIntExtra("position", -1);
                if (pos != -1) {
                    if (getParentFragment() != null) { ((BaseGroupFragment) getParentFragment()).addFragment(MyProgramCreateFragment.newInstance(sequences.get(pos))); }
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this.editReceiver, new IntentFilter("editSequence"));
    }

    @Override
    protected void addListener() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentFragment() != null) { ((BaseGroupFragment) getParentFragment()).addFragment(MyProgramCreateFragment.newInstance(null)); }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(removeReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(editReceiver);
        setTitleToActivity(getString(R.string.title_section3));
    }
}
