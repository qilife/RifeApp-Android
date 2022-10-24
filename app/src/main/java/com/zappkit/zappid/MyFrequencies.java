package com.zappkit.zappid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;

import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.lemeor.main_menu.MainActivity;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MyFrequencies extends Activity {
    static MyFrequencyListAdapter Freq_adapt;
    public static ArrayList<FrequencyListModel> freq = new ArrayList();
    Button AddFreqButton;
    DbHelper database;
    ListView freq_list;
    InputMethodManager imm;
    Cursor myfreq;
    TextView noContent;
    BroadcastReceiver removeReceiver;
    SharedPreferences settings;

    class C01471 implements OnItemClickListener {
        C01471() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent frequency = new Intent(MyFrequencies.this.getBaseContext(), Frequency.class);
            frequency.putExtra("id_value", ((FrequencyListModel) MyFrequencies.freq.get(position)).getIdString());
            frequency.putExtra("db_of_freq", ((FrequencyListModel) MyFrequencies.freq.get(position)).getDatabaseId());
            MyFrequencies.this.startActivity(frequency);
        }
    }

    class C01482 extends BroadcastReceiver {
        C01482() {
        }

        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra("position", -1);
            if (position != -1) {
                MyFrequencies.this.database.deleteMyFrequency(((FrequencyListModel) MyFrequencies.freq.get(position)).getId());
                MyFrequencies.freq.remove(position);
                MyFrequencies.Freq_adapt.notifyDataSetChanged();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.settings = SharedPreferenceHelper.getSharedPreferences(this);
        adjustLanguage();
        setContentView(R.layout.my_frequencies);

        database = new DbHelper(this);

        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.AddFreqButton = (Button) findViewById(R.id.btn_add_new);
        this.freq_list = (ListView) findViewById(R.id.frequency_list_myzapp);
        this.noContent = (TextView) findViewById(R.id.noContent);
        this.myfreq = this.database.getMyFrequencies();
        if (this.myfreq.getCount() != 0) {
            freq.clear();
            this.myfreq.moveToFirst();
            do {
                FrequencyListModel tempModel = new FrequencyListModel();
                tempModel.setFrequency(Double.parseDouble(this.myfreq.getString(this.myfreq.getColumnIndex("frequency"))));
                tempModel.setDatabaseId(2);
                tempModel.setId(this.myfreq.getInt(this.myfreq.getColumnIndex("_id")));
                freq.add(tempModel);
            } while (this.myfreq.moveToNext());
            this.myfreq.close();
            this.noContent.setText("");
            Freq_adapt = new MyFrequencyListAdapter(this, freq);
            this.freq_list.setAdapter(Freq_adapt);
            this.freq_list.setOnItemClickListener(new C01471());
            this.removeReceiver = new C01482();
            LocalBroadcastManager.getInstance(this).registerReceiver(this.removeReceiver, new IntentFilter("removeFreq"));
        }
    }

    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.removeReceiver);
    }
    ListView lv;
    EditText texteditor;
    public void add_btn_clicked(View view) {
        setContentView(R.layout.addnewfrequency);
        texteditor = (EditText) findViewById(R.id.editnewfreq);
        texteditor.requestFocus();
        lv = findViewById(R.id.frequency_list);
        searchForfrequency("");
        texteditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int inputType = texteditor.getInputType();
                String text = texteditor.getText().toString().toLowerCase(Locale.getDefault());
                searchForfrequency(s.toString());

            }
        });


        this.imm.showSoftInput(texteditor, 1);
    }

    public void cancel_frequency_add(View view) {
        refreshActivity();
    }
    public void searchForfrequency(String s) {
        ArrayList<SequenceListModel> temp_Seq = new ArrayList();
        s = s.toLowerCase(Locale.getDefault());

        this.database = new DbHelper(this);
        Cursor frequencies = this.database.getFrequencies_search(s);
        MainActivity.sFrequenciesList.clear();
        Log.d("FRESH",frequencies.getCount()+"");
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
        Collections.sort(MainActivity.sFrequenciesList, new C01401());
        MainActivity.sFrequencyAdapter = new FrequencyListAdapter(this, MainActivity.sFrequenciesList);
        if (lv != null) {
            lv.setAdapter(MainActivity.sFrequencyAdapter);
        }
        lv.setOnItemClickListener(new C01412());

    }
    class C01412 implements AdapterView.OnItemClickListener {
        C01412() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            texteditor.setText(MainActivity.sFrequenciesList.get(position).getFrequency()+"");
        }
    }


    class C01401 implements Comparator<FrequencyListModel> {
        C01401() {
        }

        public int compare(FrequencyListModel lhs, FrequencyListModel rhs) {
            if (lhs.getFrequency() > rhs.getFrequency()) {
                return 1;
            }
            if (rhs.getFrequency() > lhs.getFrequency()) {
                return -1;
            }
            return 0;
        }
    }

    public void add_frequency_clicked(View view) {
        EditText newFreqEdit = (EditText) findViewById(R.id.editnewfreq);
        String freq = newFreqEdit.getText().toString();
        newFreqEdit.clearFocus();
        this.imm.hideSoftInputFromWindow(newFreqEdit.getWindowToken(), 0);
        if (freq.equals("")) {
            Toast.makeText(this, getResources().getString(R.string.myFrequenciesNotypedFreq), Toast.LENGTH_SHORT).show();
        } else if (freq.indexOf(".") == -1 || freq.length() - (freq.indexOf(".") + 1) <= 3) {
            float freqFloat = Float.parseFloat(freq);
            if (((double) freqFloat) < 0.5d || freqFloat > 23000.0f) {
                Toast.makeText(this, getResources().getString(R.string.myFrequenciesOutOfRange), Toast.LENGTH_SHORT).show();
                return;
            }
            Cursor freqMain = this.database.getFrequencies();
            boolean existsAlready = false;
            if(freqMain.moveToFirst()) {
                while (!freqMain.getString(freqMain.getColumnIndex("frequency")).equals(freq)) {
                    if (!freqMain.moveToNext()) {
                        break;
                    }
                }
                freqMain.close();
            }
            Cursor myFreq = this.database.getMyFrequencies_isavailbe(freq);
            if (myFreq.getCount() != 0) {
                existsAlready = true;
            }
            myFreq.close();
            if (existsAlready) {
                Toast.makeText(this, getResources().getString(R.string.myFrequenciesFrequencyExistsMy), Toast.LENGTH_SHORT).show();
                return;
            }
            this.database.insertMyFrequency(freq);
            Toast.makeText(this, getResources().getString(R.string.myFrequecniesAddedFreq) + " " + freq + "Hz", Toast.LENGTH_SHORT).show();
            refreshActivity();
        } else {
            Toast.makeText(this, getResources().getString(R.string.myFrequenciesDecimalOverload), Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshActivity() {
        finish();
        startActivity(getIntent());
    }

    private void adjustLanguage() {
        Locale locale = new Locale(this.settings.getString("language", "en"));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_main);
    }
}
