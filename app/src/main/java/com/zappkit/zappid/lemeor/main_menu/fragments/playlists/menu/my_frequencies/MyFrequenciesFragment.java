package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_frequencies;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.FrequencyListModel;
import com.zappkit.zappid.MyFrequencyListAdapter;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;
import com.zappkit.zappid.lemeor.tools.Utilities;

import java.util.ArrayList;

public class MyFrequenciesFragment extends BaseFragment {
    private MyFrequencyListAdapter mAdapter;
    private ArrayList<FrequencyListModel> mFrequencyList = new ArrayList<>();
    private DbHelper mDbHelper;
    private BroadcastReceiver removeReceiver;
    private Button btnAdd;

    class ItemClicker implements AdapterView.OnItemClickListener {
        ItemClicker() { }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Utilities.addFrequencyToPlayer(mContext, mFrequencyList.get(position).getIdString(), mFrequencyList.get(position).getDatabaseId());
        }
    }

    class UpdateListReceiver extends BroadcastReceiver {
        UpdateListReceiver() { }

        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra("position", -1);
            if (position != -1) {
                MyFrequenciesFragment.this.mDbHelper.deleteMyFrequency(mFrequencyList.get(position).getId());
                mFrequencyList.remove(position);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    protected int initLayout() { return R.layout.fragment_my_frequencies_cp; }

    @Override
    protected void initComponents() {
        mDbHelper = new DbHelper(mContext);
        btnAdd = mView.findViewById(R.id.btn_add_new);
        ListView freq_list = mView.findViewById(R.id.frequency_list_myzapp);
        TextView noContent = mView.findViewById(R.id.noContent);
        Cursor myFrequencies = this.mDbHelper.getMyFrequencies();
        if (myFrequencies.getCount() != 0) {
            mFrequencyList.clear();
            myFrequencies.moveToFirst();
            do {
                FrequencyListModel tempModel = new FrequencyListModel();
                tempModel.setFrequency(Double.parseDouble(myFrequencies.getString(myFrequencies.getColumnIndex("frequency"))));
                tempModel.setDatabaseId(2);
                tempModel.setId(myFrequencies.getInt(myFrequencies.getColumnIndex("_id")));
                mFrequencyList.add(tempModel);
            } while (myFrequencies.moveToNext());
            myFrequencies.close();
            noContent.setText("");
            mAdapter = new MyFrequencyListAdapter((Activity) mContext, mFrequencyList);
            freq_list.setAdapter(mAdapter);
            freq_list.setOnItemClickListener(new ItemClicker());
            this.removeReceiver = new UpdateListReceiver();
            LocalBroadcastManager.getInstance(mContext).registerReceiver(this.removeReceiver, new IntentFilter("removeFreq"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this.removeReceiver);
        setTitleToActivity(getString(R.string.title_section3));
    }

    @Override
    protected void addListener() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentFragment() != null) {
                    ((BaseGroupFragment) getParentFragment()).addFragment(new MyFrequenciesAddFragment());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitleToActivity(getString(R.string.tv_my_frequencies));
    }
}
