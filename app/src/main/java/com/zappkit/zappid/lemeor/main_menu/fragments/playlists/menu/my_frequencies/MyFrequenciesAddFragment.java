package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_frequencies;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.FrequencyListAdapter;
import com.zappkit.zappid.FrequencyListModel;
import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.main_menu.MainActivity;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MyFrequenciesAddFragment extends BaseFragment implements View.OnClickListener {
    private ListView mListView;
    private EditText mEditText;
    private InputMethodManager imm;
    private DbHelper mDbHelper;
    private Button mBtnAdd, mBtnCancel;

    static class SortList implements Comparator<FrequencyListModel> {
        SortList() { }

        public int compare(FrequencyListModel lhs, FrequencyListModel rhs) {
            return Double.compare(lhs.getFrequency(), rhs.getFrequency());
        }
    }

    @Override
    protected int initLayout() { return R.layout.fragment_add_new_frequency_cp; }

    @Override
    protected void initComponents() {
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mDbHelper = new DbHelper(mContext);

        mBtnAdd = mView.findViewById(R.id.add_frequency_addbtn);
        mBtnCancel = mView.findViewById(R.id.add_frequency_cancel);
        mEditText = mView.findViewById(R.id.editnewfreq);
        mEditText.requestFocus();
        mListView = mView.findViewById(R.id.frequency_list);
        searchFrequencies("");
        imm.showSoftInput(mEditText, 1);
    }

    @Override
    protected void addListener() {
        mBtnAdd.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mEditText.getText().toString().toLowerCase(Locale.getDefault());
                searchFrequencies(text);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_frequency_addbtn:
                add_frequency_clicked();
                break;
            case R.id.add_frequency_cancel:
                if (getParentFragment() != null) { ((BaseGroupFragment) getParentFragment()).onBackPressed(); }
                break;
        }
    }

    public void searchFrequencies(String s) {
        s = s.toLowerCase(Locale.getDefault());
        Cursor frequencies = this.mDbHelper.getFrequencies_search(s);
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
        Collections.sort(MainActivity.sFrequenciesList, new SortList());
        MainActivity.sFrequencyAdapter = new FrequencyListAdapter((Activity) mContext, MainActivity.sFrequenciesList);
        if (mListView != null) {
            mListView.setAdapter(MainActivity.sFrequencyAdapter);
        }

        if (mListView != null) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mEditText.setText(String.valueOf(MainActivity.sFrequenciesList.get(i).getFrequency()));
                }
            });
        }
    }

    public void add_frequency_clicked() {
        String freq = mEditText.getText().toString();
        mEditText.clearFocus();
        this.imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        if (freq.equals("")) {
            Toast.makeText(mContext, getResources().getString(R.string.myFrequenciesNotypedFreq), Toast.LENGTH_SHORT).show();
        } else if (!freq.contains(".") || freq.length() - (freq.indexOf(".") + 1) <= 3) {
            float freqFloat = Float.parseFloat(freq);
            if (((double) freqFloat) < 0.5d || freqFloat > 23000.0f) {
                Toast.makeText(mContext, getResources().getString(R.string.myFrequenciesOutOfRange), Toast.LENGTH_SHORT).show();
                return;
            }
            Cursor freqMain = this.mDbHelper.getFrequencies();
            boolean existsAlready = false;
            freqMain.moveToFirst();
            while (!freqMain.getString(freqMain.getColumnIndex("frequency")).equals(freq)) {
                if (!freqMain.moveToNext()) { break; }
            }
            freqMain.close();

            Cursor myFreq = this.mDbHelper.getMyFrequencies_isavailbe(freq);
            if (myFreq.getCount() != 0) { existsAlready = true; }
            myFreq.close();
            if (existsAlready) {
                Toast.makeText(mContext, getResources().getString(R.string.myFrequenciesFrequencyExistsMy), Toast.LENGTH_SHORT).show();
                return;
            }

            mDbHelper.insertMyFrequency(freq);
            Toast.makeText(mContext, getResources().getString(R.string.myFrequecniesAddedFreq) + " " + freq + "Hz", Toast.LENGTH_SHORT).show();

            if (getParentFragment() != null) { ((BaseGroupFragment) getParentFragment()).onBackPressed(); }

        } else {
            Toast.makeText(mContext, getResources().getString(R.string.myFrequenciesDecimalOverload), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitleToActivity(getString(R.string.tv_my_frequencies));
    }
}
