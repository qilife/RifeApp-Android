package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_programs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.FrequencyListModel;
import com.zappkit.zappid.MySequenceFrequencyListAdapter;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;

import java.util.ArrayList;
import java.util.Collections;

public class MyProgramSaveFragment extends BaseFragment {
    private SequenceListModel mSequenceListModel;
    private ArrayList<FrequencyListModel> freqAdded = new ArrayList<>();
    private ArrayList<String> newSequence = new ArrayList<>();
    private MySequenceFrequencyListAdapter mFreqListAdapter;
    private Button btnCancel, btnSave, btnAddFrequencies;
    private BroadcastReceiver frequencyReceiver;
    private DbHelper mDbHelper;
    private BroadcastReceiver removeFreqReceiver;

    public static MyProgramSaveFragment newInstance(SequenceListModel sequenceListModel) {
        MyProgramSaveFragment fragment = new MyProgramSaveFragment();
        fragment.mSequenceListModel = sequenceListModel;
        return fragment;
    }

    class UpdateReceiver extends BroadcastReceiver {
        UpdateReceiver() { }

        public void onReceive(Context context, Intent intent) {
            int pos = intent.getIntExtra("position", 0);
            if (freqAdded.size() > 0) { freqAdded.remove(pos); }
            if (newSequence.size() > 0) { newSequence.remove(pos); }
            mFreqListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected int initLayout() { return R.layout.fragment_save_new_my_program; }

    @Override
    protected void initComponents() {
        if (mSequenceListModel == null) {
            mSequenceListModel = new SequenceListModel();
        }
        this.mDbHelper = new DbHelper(mContext);
        if (mSequenceListModel.getId() != -1) {
            ((TextView) mView.findViewById(R.id.mySequenceAddSequence)).setText(getResources().getString(R.string.myPlaylistEditBtn));
        }
        btnAddFrequencies = mView.findViewById(R.id.mySeqAddFreqBtn);
        btnCancel = mView.findViewById(R.id.backToNameAndNotes);
        btnSave = mView.findViewById(R.id.mySequenceAddSequence);
        ListView freqAddedList = mView.findViewById(R.id.mySequenceFreqList);
        freqAdded.clear();
        this.mFreqListAdapter = new MySequenceFrequencyListAdapter((Activity) mContext, this.freqAdded);
        freqAddedList.setAdapter(this.mFreqListAdapter);

        if (mSequenceListModel.getId() != -1) { Collections.addAll(newSequence, mSequenceListModel.getFrequencyListArray()); }

        try {
            if (this.newSequence.size() != 0) {
                for (String s : this.newSequence) {
                    String[] ids = s.split(",");
                    FrequencyListModel tempModel = new FrequencyListModel();
                    tempModel.setDatabaseId(Integer.parseInt(ids[0]));
                    tempModel.setId(Integer.parseInt(ids[1]));
                    tempModel.setFrequency(Double.parseDouble(this.mDbHelper.getFrequencyString(ids[1], Integer.parseInt(ids[0]))));
                    this.freqAdded.add(tempModel);
                }
                this.mFreqListAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        frequencyReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ArrayList<String> listFreq = intent.getStringArrayListExtra("Frequency");
                if (listFreq != null) {
                    try {
                        for (String s : listFreq) {
                            String[] ids = s.split(",");
                            FrequencyListModel tempModel = new FrequencyListModel();
                            tempModel.setDatabaseId(Integer.parseInt(ids[0]));
                            tempModel.setId(Integer.parseInt(ids[1]));
                            tempModel.setFrequency(Double.parseDouble(mDbHelper.getFrequencyString(ids[1], Integer.parseInt(ids[0]))));
                            freqAdded.add(tempModel);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (listFreq != null) { newSequence.addAll(listFreq); }
                mFreqListAdapter.notifyDataSetChanged();
            }
        };
        mContext.registerReceiver(frequencyReceiver, new IntentFilter("add_frequency_to_program"));
        removeFreqReceiver = new UpdateReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this.removeFreqReceiver, new IntentFilter("removeFreqFromSequence"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mContext.unregisterReceiver(frequencyReceiver);
        } catch (IllegalArgumentException ignored) { }
        try {
            mContext.unregisterReceiver(removeFreqReceiver);
        } catch (IllegalArgumentException ignored) { }
    }

    @Override
    protected void addListener() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackToPreFragment();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSequenceToMySequences();
            }
        });
        btnAddFrequencies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentFragment() != null) {
                    ((BaseGroupFragment) getParentFragment()).addFragmentNotReloadContent(new MyProgramAddFrequencyFragment());
                }
            }
        });
    }

    public void addSequenceToMySequences() {
        if (this.newSequence.size() != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            boolean isFirstRun = true;
            for (String tempFreq : this.newSequence) {
                if (isFirstRun) {
                    stringBuilder.append(tempFreq);
                    isFirstRun = false;
                } else {
                    stringBuilder.append("-");
                    stringBuilder.append(tempFreq);
                }
            }
            String finalList = stringBuilder.toString();
            if (mSequenceListModel.getId() == -1) {
                mDbHelper.insertMySequence(mSequenceListModel.getSequenceTitle(), mSequenceListModel.getNotes(), finalList);
            } else {
                mDbHelper.updateMySequence(mSequenceListModel.getId(), mSequenceListModel.getSequenceTitle(), mSequenceListModel.getNotes(), finalList);
            }

            onBackToPreFragment();
            onBackToPreFragment();
            return;
        }
        Toast.makeText(mContext, getResources().getString(R.string.mySequencesMissingFreq), Toast.LENGTH_SHORT).show();
    }
}
