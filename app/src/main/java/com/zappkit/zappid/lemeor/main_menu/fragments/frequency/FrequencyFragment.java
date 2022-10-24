package com.zappkit.zappid.lemeor.main_menu.fragments.frequency;

import android.database.Cursor;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.FrequencyListAdapter;
import com.zappkit.zappid.FrequencyListModel;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.main_menu.MainActivity;
import com.zappkit.zappid.lemeor.tools.Utilities;

import android.text.Editable;
import android.text.TextWatcher;

import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.zappkit.zappid.lemeor.base.BaseFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class FrequencyFragment extends BaseFragment {
    private DbHelper mDbHelper;
    private ListView list;
    private EditText etSearch;

    static class SortFrequency implements Comparator<FrequencyListModel> {
        SortFrequency() { }

        public int compare(FrequencyListModel lhs, FrequencyListModel rhs) {
            return Double.compare(lhs.getFrequency(), rhs.getFrequency());
        }
    }

    class FrequencyClickListener implements AdapterView.OnItemClickListener {
        FrequencyClickListener() { }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Utilities.addFrequencyToPlayer(mContext, MainActivity.sFrequenciesList.get(position).getIdString(), MainActivity.sFrequenciesList.get(position).getDatabaseId());
        }
    }

    @Override
    protected int initLayout() { return R.layout.fragment_frequencies; }

    @Override
    protected void initComponents() {
        mDbHelper = new DbHelper(getContext());

        list = mView.findViewById(R.id.frequency_list);
        etSearch = mView.findViewById(R.id.menu_search);
    }

    @Override
    protected void addListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = etSearch.getText().toString().toLowerCase(Locale.getDefault());
                searchFrequency(text);
            }
        });
    }

    public void onResume() {
        super.onResume();
        MainActivity.sFrequenciesList.clear();
        Cursor frequencies = this.mDbHelper.getFrequencies();

        if (frequencies.getCount() != 0) {
            frequencies.moveToFirst();
            do {
                FrequencyListModel freqModel = new FrequencyListModel();
                freqModel.setId(frequencies.getInt(frequencies.getColumnIndex("_id")));
                freqModel.setFrequency(frequencies.getDouble(frequencies.getColumnIndex("frequency")));
                freqModel.setDatabaseId(1);
                MainActivity.sFrequenciesList.add(freqModel);
            } while (frequencies.moveToNext());
        }
        frequencies.close();
        Cursor myFrequencies = this.mDbHelper.getMyFrequencies();
        if (myFrequencies.getCount() != 0) {
            myFrequencies.moveToFirst();
            do {
                FrequencyListModel freqModel = new FrequencyListModel();
                freqModel.setId(myFrequencies.getInt(myFrequencies.getColumnIndex("_id")));
                freqModel.setFrequency(myFrequencies.getDouble(myFrequencies.getColumnIndex("frequency")));
                freqModel.setDatabaseId(2);
                MainActivity.sFrequenciesList.add(freqModel);
            } while (myFrequencies.moveToNext());
        }
        myFrequencies.close();
        Collections.sort(MainActivity.sFrequenciesList, new SortFrequency());
        MainActivity.sFrequencyAdapter = new FrequencyListAdapter(getActivity(), MainActivity.sFrequenciesList);

        if (list != null) { list.setAdapter(MainActivity.sFrequencyAdapter); }
        if (list != null) { list.setOnItemClickListener(new FrequencyClickListener()); }
    }

    public void searchFrequency(String s) {
        s = s.toLowerCase(Locale.getDefault());
        mDbHelper = new DbHelper(getContext());
        Cursor frequencies = mDbHelper.getFrequencies_search(s);
        MainActivity.sFrequenciesList.clear();

        if (frequencies.getCount() != 0) {
            frequencies.moveToFirst();
            do {
                FrequencyListModel freqModel = new FrequencyListModel();
                freqModel.setId(frequencies.getInt(frequencies.getColumnIndex("_id")));
                freqModel.setFrequency(frequencies.getDouble(frequencies.getColumnIndex("frequency")));
                freqModel.setDatabaseId(1);
                MainActivity.sFrequenciesList.add(freqModel);
            } while (frequencies.moveToNext());
        }

        frequencies.close();
        Collections.sort(MainActivity.sFrequenciesList, new SortFrequency());
        MainActivity.sFrequencyAdapter = new FrequencyListAdapter(getActivity(), MainActivity.sFrequenciesList);

        if (list != null) { list.setAdapter(MainActivity.sFrequencyAdapter); }
        if (list != null) { list.setOnItemClickListener(new FrequencyClickListener()); }
    }
}
