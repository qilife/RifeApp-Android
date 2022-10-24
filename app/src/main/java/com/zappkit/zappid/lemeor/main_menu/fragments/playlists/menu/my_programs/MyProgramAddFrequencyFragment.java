package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_programs;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.FrequencyListAdapter;
import com.zappkit.zappid.FrequencyListModel;
import com.zappkit.zappid.lemeor.main_menu.MainActivity;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.base.BaseActivity;
import com.zappkit.zappid.lemeor.base.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MyProgramAddFrequencyFragment extends BaseFragment {
    private ListView mListView;
    private ArrayList<FrequencyListModel> mFrequencyList;
    private DbHelper mDbHelper;
    private ArrayList<String> mListFreq = new ArrayList<>();

    @Override
    protected int initLayout() { return R.layout.fragment_add_frequency_to_sequence; }

    @Override
    protected void initComponents() {
        mDbHelper = new DbHelper(mContext);

        Button btnAddFrequencies = mView.findViewById(R.id.btn_head_mp);
        mListView = mView.findViewById(R.id.FrequencyToAddList);
        mFrequencyList = new ArrayList<>();
        FrequencyListAdapter adapter = new FrequencyListAdapter((Activity) mContext, mFrequencyList);
        mListView.setAdapter(adapter);
        Cursor frequencies = mDbHelper.getFrequencies();
        frequencies.moveToFirst();
        do {
            FrequencyListModel freqModel = new FrequencyListModel();
            freqModel.setId(frequencies.getInt(frequencies.getColumnIndex("_id")));
            freqModel.setFrequency(frequencies.getDouble(frequencies.getColumnIndex("frequency")));
            freqModel.setDatabaseId(1);
            mFrequencyList.add(freqModel);
        } while (frequencies.moveToNext());
        frequencies.close();
        Cursor myFrequencies = this.mDbHelper.getMyFrequencies();
        if (myFrequencies.getCount() == 0 || myFrequencies.getCount() == 0) {
            myFrequencies.close();
            Collections.sort(mFrequencyList, new SortFrequency());
            adapter.notifyDataSetChanged();
        } else {
            myFrequencies.moveToFirst();
            do {
                FrequencyListModel freqModel = new FrequencyListModel();
                freqModel.setId(myFrequencies.getInt(myFrequencies.getColumnIndex("_id")));
                freqModel.setFrequency(myFrequencies.getDouble(myFrequencies.getColumnIndex("frequency")));
                freqModel.setDatabaseId(2);
                mFrequencyList.add(freqModel);
            } while (myFrequencies.moveToNext());
            myFrequencies.close();
        }
        Collections.sort(mFrequencyList, new SortFrequency());
        adapter.notifyDataSetChanged();

        adapter.setIOnClickItemListener(new FrequencyListAdapter.IOnClickItemListener() {
            @Override
            public void onClick(int position) {
                String newSequence = mFrequencyList.get(position).getDatabaseId() + "," + mFrequencyList.get(position).getId();
                mListFreq.add(newSequence);
            }
        });

        btnAddFrequencies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("add_frequency_to_program");
                intent.putStringArrayListExtra("Frequency", mListFreq);
                mContext.sendBroadcast(intent);
                onBackToPreFragment();
            }
        });

        final EditText etSearch = mView.findViewById(R.id.menu_search);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = etSearch.getText().toString().toLowerCase(Locale.getDefault());
                searchForSequence(text);
            }
        });
    }

    @Override
    protected void addListener() { }

    public void searchForSequence(String s) {
        FrequencyListAdapter adapter = new FrequencyListAdapter((Activity) mContext, mFrequencyList);
        mListView.setAdapter(adapter);
        mFrequencyList.clear();
        Cursor frequencies = this.mDbHelper.getFrequencies_search(s);
        if (frequencies.getCount() > 0) {
            frequencies.moveToFirst();
            do {
                String name = frequencies.getString(frequencies.getColumnIndex("frequency"));
                name = name.toLowerCase(Locale.getDefault());
                if (name.contains(s)) {
                    FrequencyListModel freqModel = new FrequencyListModel();
                    freqModel.setId(frequencies.getInt(frequencies.getColumnIndex("_id")));
                    freqModel.setFrequency(frequencies.getDouble(frequencies.getColumnIndex("frequency")));
                    freqModel.setDatabaseId(1);
                    mFrequencyList.add(freqModel);
                }
            } while (frequencies.moveToNext());
        }
        frequencies.close();
        Cursor myFrequencies = this.mDbHelper.getMyFrequencies();
        if (myFrequencies.getCount() == 0 || frequencies.getCount() == 0) {
            myFrequencies.close();
            Collections.sort(mFrequencyList, new SortFrequency());
            adapter.notifyDataSetChanged();
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    String newSequence = mFrequencyList.get(position).getDatabaseId() + "," + mFrequencyList.get(position).getId();
                    Intent intent = new Intent("add_frequency_to_program");
                    intent.putExtra("Frequency", newSequence);
                    mContext.sendBroadcast(intent);
                    onBackToPreFragment();
                }
            });
        } else {
            myFrequencies.moveToFirst();
            if (myFrequencies.getCount() > 0) {
                do {
                    String name = myFrequencies.getString(myFrequencies.getColumnIndex("frequency" + MainActivity.sLocale));
                    name = name.toLowerCase(Locale.getDefault());
                    if (name.contains(s)) {
                        FrequencyListModel freqModel = new FrequencyListModel();
                        freqModel.setId(myFrequencies.getInt(myFrequencies.getColumnIndex("_id")));
                        freqModel.setFrequency(myFrequencies.getDouble(myFrequencies.getColumnIndex("frequency")));
                        freqModel.setDatabaseId(2);
                        mFrequencyList.add(freqModel);
                    }
                } while (myFrequencies.moveToNext());
            }
            myFrequencies.close();
        }
        Collections.sort(mFrequencyList, new SortFrequency());
        adapter.notifyDataSetChanged();
    }

    static class SortFrequency implements Comparator<FrequencyListModel> {
        SortFrequency() { }

        public int compare(FrequencyListModel lhs, FrequencyListModel rhs) {
            return Double.compare(lhs.getFrequency(), rhs.getFrequency());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((BaseActivity) mContext).hideKeyBoard();
    }
}
