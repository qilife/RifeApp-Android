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
import com.zappkit.zappid.lemeor.main_menu.fragments.programs.ProgramsAdapter;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

public class MySequences extends Activity {
    public static ArrayList<SequenceListModel> Seq = new ArrayList();
    DbHelper database;
    BroadcastReceiver editReceiver;
    ArrayList<FrequencyListModel> freqAdded;
    MySequenceFrequencyListAdapter freqListAdapter;
    InputMethodManager imm;
    BroadcastReceiver removeFreqReceiver;
    BroadcastReceiver removeReceiver;
    SharedPreferences settings;
    ProgramsAdapter Seq_adapt;
    ListView mainMySeqList;
    ListView frequencyList;
    ArrayList<FrequencyListModel> freq;
    ListView lv;
    EditText seqNotes, SeqName;
    private int editId;
    private String newSeqName;
    private String newSeqNotes;
    private ArrayList<String> newSequence;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.settings = SharedPreferenceHelper.getSharedPreferences(this);
        adjustLanguage();
        setContentView(R.layout.my_sequences);

        this.freqAdded = new ArrayList();

        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.database = new DbHelper(this);
        mainMySeqList = (ListView) findViewById(R.id.sequence_list_myzapp);
        final ArrayList<SequenceListModel> sequences = new ArrayList();
        final MySequenceListAdapter adapter = new MySequenceListAdapter(this, sequences);
        mainMySeqList.setAdapter(adapter);
        Cursor mySequences = this.database.getMySequences();
        if (mySequences.getCount() != 0) {
            ((TextView) findViewById(R.id.mySeqNoContent)).setVisibility(View.GONE);
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

        mainMySeqList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent seq = new Intent(MySequences.this.getBaseContext(), Sequence.class);
                seq.putExtra("position", ((SequenceListModel) sequences.get(position)).getIdString());
                seq.putExtra("seq_db", ((SequenceListModel) sequences.get(position)).getDatabaseId());
                MySequences.this.startActivity(seq);
            }
        });

        this.newSeqName = "";
        this.newSeqNotes = "";
        this.newSequence = new ArrayList();
        this.editId = -1;
        this.removeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int position = intent.getIntExtra("position", -1);
                if (position != -1) {
                    MySequences.this.database.deleteMySequence(((SequenceListModel) sequences.get(position)).getId());
                    sequences.remove(position);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(this.removeReceiver, new IntentFilter("removeSeq"));
        this.editReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int pos = intent.getIntExtra("position", -1);
                if (pos != -1) {
                    MySequences.this.newSeqName = ((SequenceListModel) sequences.get(pos)).getSequenceTitle();
                    MySequences.this.newSeqNotes = ((SequenceListModel) sequences.get(pos)).getNotes();
                    for (String freqId : ((SequenceListModel) sequences.get(pos)).getFrequencyListArray()) {
                        MySequences.this.newSequence.add(freqId);
                    }
                    MySequences.this.editId = ((SequenceListModel) sequences.get(pos)).getId();
                    MySequences.this.add_btn_clicked(MySequences.this.getCurrentFocus());
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(this.editReceiver, new IntentFilter("editSequence"));
        this.removeFreqReceiver = new C01664();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.removeFreqReceiver, new IntentFilter("removeFreqFromSequence"));
    }

    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.removeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.editReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.removeFreqReceiver);
    }

    public void add_btn_clicked(View view) {
        setContentView(R.layout.add_sequence_nameandnotes);
        seqNotes = (EditText) findViewById(R.id.editnewseqnotes);
        SeqName = (EditText) findViewById(R.id.editnewseqname);
        lv = findViewById(R.id.list_view);
        lv.setVisibility(View.GONE);
        Seq_adapt = new ProgramsAdapter(this, Seq);
        if (lv != null) {
            lv.setAdapter(Seq_adapt);
        }
        SeqName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int inputType = SeqName.getInputType();
                String text = SeqName.getText().toString().toLowerCase(Locale.getDefault());
                if (text.length() != 0) {
                    searchForSequence_(text);
                } else {
                    lv.setVisibility(View.GONE);
                }
            }
        });
        SeqName.setText(this.newSeqName);
        seqNotes.setText(this.newSeqNotes);

    }

    public void searchForSequence_(String s) {
        ArrayList<SequenceListModel> temp_Seq = new ArrayList();
        s = s.toLowerCase(Locale.getDefault());
        Seq.clear();
        Cursor Sequences = database.getSequences();
        if (Sequences.getCount() != 0) {
            lv.setVisibility(View.VISIBLE);
        } else {
            lv.setVisibility(View.GONE);
        }
        Sequences.moveToFirst();
        do {
            String name = Sequences.getString(Sequences.getColumnIndex("name" + MainActivity.sLocale));
            String notes = Sequences.getString(Sequences.getColumnIndex("description")).toLowerCase(Locale.getDefault());
            String OriginalName = name;
            name = name.toLowerCase(Locale.getDefault());
            SequenceListModel seqModel = new SequenceListModel();
            if (name.contains(s)) {
                seqModel.setSequenceTitle(OriginalName);
                seqModel.setNotes("");
                seqModel.setId(Sequences.getInt(Sequences.getColumnIndex("_id")));
                seqModel.setDbId(1);
                Seq.add(seqModel);
            } else if (notes.contains(s)) {
                seqModel.setSequenceTitle(OriginalName);
                seqModel.setNotes("See Notes");
                seqModel.setId(Sequences.getInt(Sequences.getColumnIndex("_id")));
                seqModel.setDbId(1);
                temp_Seq.add(seqModel);
            }
        } while (Sequences.moveToNext());
        Sequences.close();
        Cursor mySequences = database.getMySequences();
        if (mySequences.getCount() != 0) {
            mySequences.moveToFirst();
            do {
                String name = mySequences.getString(mySequences.getColumnIndex("name"));
                String notes = mySequences.getString(mySequences.getColumnIndex("description")).toLowerCase(Locale.getDefault());
                String OriginalName = name;
                name = name.toLowerCase(Locale.getDefault());
                SequenceListModel seqModel = new SequenceListModel();
                if (name.contains(s)) {
                    seqModel.setSequenceTitle(OriginalName);
                    seqModel.setNotes("");
                    seqModel.setId(mySequences.getInt(mySequences.getColumnIndex("_id")));
                    seqModel.setDbId(2);
                    Seq.add(seqModel);
                } else if (notes.contains(s)) {
                    seqModel.setSequenceTitle(OriginalName);
                    seqModel.setNotes("See Notes");
                    seqModel.setId(mySequences.getInt(mySequences.getColumnIndex("_id")));
                    seqModel.setDbId(2);
                    temp_Seq.add(seqModel);
                }
            } while (Sequences.moveToNext());
        }
        mySequences.close();
        Collections.sort(Seq, new C01363());
        if (!temp_Seq.isEmpty()) {
            Collections.sort(temp_Seq, new C01374());
            Iterator i$ = temp_Seq.iterator();
            while (i$.hasNext()) {
                Seq.add((SequenceListModel) i$.next());
            }
        }
        Seq_adapt.notifyDataSetChanged();
        lv.setOnItemClickListener(new C01432());

    }

    public void cancel_sequence_add(View view) {
        this.newSeqName = "";
        this.newSeqNotes = "";
        this.newSequence.clear();
        refresh();
    }

    public void add_nameandnotes_clicked(View view) {
        EditText seqname = (EditText) findViewById(R.id.editnewseqname);
        EditText seqNotes = (EditText) findViewById(R.id.editnewseqnotes);
        this.newSeqName = seqname.getText().toString();
        this.newSeqNotes = seqNotes.getText().toString();
        if (this.newSeqName.equals("")) {
            Toast.makeText(this, getResources().getString(R.string.mySequencesMissingName), Toast.LENGTH_LONG).show();
            return;
        }
        seqname.clearFocus();
        seqNotes.clearFocus();
        this.imm.hideSoftInputFromWindow(seqname.getWindowToken(), 0);
        setSequenceFrequenciesContent();
    }

    public void add_FrequencyLayoutOpen(View view) {
        setContentView(R.layout.add_frequency_to_sequence);
        frequencyList = (ListView) findViewById(R.id.FrequencyToAddList);
        freq = new ArrayList();
        FrequencyListAdapter adapter = new FrequencyListAdapter(this, freq);
        frequencyList.setAdapter(adapter);
        Cursor frequencies = this.database.getFrequencies();
        frequencies.moveToFirst();
        do {
            FrequencyListModel freqModel = new FrequencyListModel();
            freqModel.setId(frequencies.getInt(frequencies.getColumnIndex("_id")));
            freqModel.setFrequency(frequencies.getDouble(frequencies.getColumnIndex("frequency")));
            freqModel.setDatabaseId(1);
            freq.add(freqModel);
        } while (frequencies.moveToNext());
        frequencies.close();
        Cursor myfrequencies = this.database.getMyFrequencies();
        if (myfrequencies.getCount() == 0 || myfrequencies.getCount() == 0) {
            myfrequencies.close();
            Collections.sort(freq, new C01675());
            adapter.notifyDataSetChanged();
            frequencyList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    MySequences.this.newSequence.add(Integer.toString(((FrequencyListModel) freq.get(position)).getDatabaseId()) + "," + Integer.toString(((FrequencyListModel) freq.get(position)).getId()));
                    MySequences.this.setSequenceFrequenciesContent();
                }
            });
        } else {
            myfrequencies.moveToFirst();
            do {
                FrequencyListModel freqModel = new FrequencyListModel();
                freqModel.setId(myfrequencies.getInt(myfrequencies.getColumnIndex("_id")));
                freqModel.setFrequency(myfrequencies.getDouble(myfrequencies.getColumnIndex("frequency")));
                freqModel.setDatabaseId(2);
                freq.add(freqModel);
            } while (myfrequencies.moveToNext());
            myfrequencies.close();
        }
        Collections.sort(freq, new C01675());
        adapter.notifyDataSetChanged();
        frequencyList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MySequences.this.newSequence.add(Integer.toString(((FrequencyListModel) freq.get(position)).getDatabaseId()) + "," + Integer.toString(((FrequencyListModel) freq.get(position)).getId()));
                MySequences.this.setSequenceFrequenciesContent();
            }
        });

        final EditText editsearch = (EditText) findViewById(R.id.menu_search);

        editsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int inputType = editsearch.getInputType();
                String text = editsearch.getText().toString().toLowerCase(Locale.getDefault());
                searchForSequence(text);

            }
        });

    }

    public void backToNameAndNotes(View view) {
        add_btn_clicked(view);
    }

    public void backToFrequenciesAdded(View view) {
        setSequenceFrequenciesContent();
    }

    private void setSequenceFrequenciesContent() {
        setContentView(R.layout.add_sequence_frequencies);
        if (this.editId != -1) {
            ((TextView) findViewById(R.id.mySequenceAddSequence)).setText(getResources().getString(R.string.myPlaylistEditBtn));
        }
        ListView freqAddedList = (ListView) findViewById(R.id.mySequenceFreqList);
        freqAdded.clear();
        this.freqListAdapter = new MySequenceFrequencyListAdapter(this, this.freqAdded);
        freqAddedList.setAdapter(this.freqListAdapter);
        freqAddedList.setOnItemClickListener(new C01697());

        try {
            if (this.newSequence.size() != 0) {
                Iterator i$ = this.newSequence.iterator();
                while (i$.hasNext()) {
                    String[] ids = ((String) i$.next()).split(",");
                    FrequencyListModel tempModel = new FrequencyListModel();
                    tempModel.setDatabaseId(Integer.parseInt(ids[0]));
                    tempModel.setId(Integer.parseInt(ids[1]));
                    tempModel.setFrequency(Double.parseDouble(this.database.getFrequencyString(ids[1], Integer.parseInt(ids[0]))));
                    this.freqAdded.add(tempModel);
                }
                this.freqListAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void addSequenceToMySequences(View view) {
        if (this.newSequence.size() != 0) {
            String finallist = "";
            StringBuilder stringBuilder = new StringBuilder();
            boolean firstrun = true;
            Iterator i$ = this.newSequence.iterator();
            while (i$.hasNext()) {
                String tempFreq = (String) i$.next();
                if (firstrun) {
                    stringBuilder.append(tempFreq);
                    firstrun = false;
                } else {
                    stringBuilder.append("-");
                    stringBuilder.append(tempFreq);
                }
            }
            finallist = stringBuilder.toString();
            System.out.println(finallist);
            if (this.editId == -1) {
                this.database.insertMySequence(this.newSeqName, this.newSeqNotes, finallist);
            } else {
                this.database.updateMySequence(this.editId, this.newSeqName, this.newSeqNotes, finallist);
            }
            refresh();
            return;
        }
        Toast.makeText(this, getResources().getString(R.string.mySequencesMissingFreq), Toast.LENGTH_SHORT).show();
    }

    private void refresh() {
        MySequences.this.newSequence.clear();
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

    public void searchForSequence(String s) {

        FrequencyListAdapter adapter = new FrequencyListAdapter(this, freq);
        frequencyList.setAdapter(adapter);
        freq.clear();
        Cursor frequencies = this.database.getFrequencies_search(s);
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
                    freq.add(freqModel);
                }
            } while (frequencies.moveToNext());
        }
        frequencies.close();
        Cursor myfrequencies = this.database.getMyFrequencies();
        if (myfrequencies.getCount() == 0 || frequencies.getCount() == 0) {
            myfrequencies.close();
            Collections.sort(freq, new C01675());
            adapter.notifyDataSetChanged();
            frequencyList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    MySequences.this.newSequence.add(Integer.toString(((FrequencyListModel) freq.get(position)).getDatabaseId()) + "," + Integer.toString(((FrequencyListModel) freq.get(position)).getId()));
                    MySequences.this.setSequenceFrequenciesContent();
                }
            });
        } else {
            myfrequencies.moveToFirst();
            if (myfrequencies.getCount() > 0) {
                do {
                    String name = myfrequencies.getString(myfrequencies.getColumnIndex("frequency" + MainActivity.sLocale));
                    String OriginalName = name;
                    name = name.toLowerCase(Locale.getDefault());
                    if (name.contains(s)) {
                        FrequencyListModel freqModel = new FrequencyListModel();
                        freqModel.setId(myfrequencies.getInt(myfrequencies.getColumnIndex("_id")));
                        freqModel.setFrequency(myfrequencies.getDouble(myfrequencies.getColumnIndex("frequency")));
                        freqModel.setDatabaseId(2);
                        freq.add(freqModel);
                    }
                } while (myfrequencies.moveToNext());
            }
            myfrequencies.close();
        }
        Collections.sort(freq, new C01675());
        adapter.notifyDataSetChanged();
    }

    class C01664 extends BroadcastReceiver {
        C01664() {
        }

        public void onReceive(Context context, Intent intent) {
            int pos = intent.getIntExtra("position", 0);
            MySequences.this.newSequence.remove(pos);
            MySequences.this.freqAdded.remove(pos);
            MySequences.this.freqListAdapter.notifyDataSetChanged();
        }
    }

    class C01675 implements Comparator<FrequencyListModel> {
        C01675() {
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

    class C01697 implements OnItemClickListener {
        C01697() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {


        }
    }

    class C01432 implements AdapterView.OnItemClickListener {
        C01432() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            SeqName.setText(Seq.get(position).getSequenceTitle());
            lv.setVisibility(View.GONE);
        }
    }

    class C01363 implements Comparator<SequenceListModel> {
        C01363() {
        }

        public int compare(SequenceListModel lhs, SequenceListModel rhs) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getSequenceTitle(), rhs.getSequenceTitle());
            return res != 0 ? res : lhs.getSequenceTitle().compareTo(rhs.getSequenceTitle());
        }
    }

    class C01374 implements Comparator<SequenceListModel> {
        C01374() {
        }

        public int compare(SequenceListModel lhs, SequenceListModel rhs) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getSequenceTitle(), rhs.getSequenceTitle());
            return res != 0 ? res : lhs.getSequenceTitle().compareTo(rhs.getSequenceTitle());
        }
    }
}
