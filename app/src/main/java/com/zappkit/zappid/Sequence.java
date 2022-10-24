package com.zappkit.zappid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.lemeor.main_menu.MainActivity;
import com.zappkit.zappid.lemeor.main_menu.player.FrequencyUIActivity;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class Sequence extends Activity {
    private static String currentlang;
    public String Seq_name;
    DbHelper database;
    private ArrayList<FrequencyListModel> freq = new ArrayList();
    public String[] freq_id;
    private String locale;
    private int seq_db_id;
    private int seq_id;
    SharedPreferences settings;

    class C01861 implements OnItemClickListener {
        C01861() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent frequency = new Intent(Sequence.this.getBaseContext(), Frequency.class);
            frequency.putExtra("id_value", ((FrequencyListModel) Sequence.this.freq.get(position)).getIdString());
            Sequence.this.startActivity(frequency);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.settings = SharedPreferenceHelper.getSharedPreferences(this);
        currentlang = this.settings.getString("language", "en");
        adjustLanguage();
        setContentView(R.layout.activity_sequence);

        database = new DbHelper(this);

        Intent intent = getIntent();
        String pos = intent.getStringExtra("position");
        this.seq_db_id = intent.getIntExtra("seq_db", 1);
        this.seq_id = Integer.parseInt(pos);
        Cursor values = this.database.getSequence(Integer.toString(this.seq_id), this.seq_db_id);
        values.moveToFirst();
        this.Seq_name = values.getString(values.getColumnIndex("name" + this.locale));
        setTitle(this.Seq_name + " " + getResources().getString(R.string.title_activity_sequence));
        String notes = values.getString(values.getColumnIndex("description" + this.locale));
        TextView notesView = (TextView) findViewById(R.id.Sequence_notes);
        if (notes.equals("")) {
            notesView.setText(getResources().getString(R.string.none));
        } else {
            notesView.setText(notes);
        }
        ListView list = (ListView) findViewById(R.id.Sequence_frequencies);
        this.freq_id = SequenceFrequencies(values.getString(values.getColumnIndex("list")));
        for (String temp : this.freq_id) {
            String[] ids = temp.split(",");
            if(ids.length > 1) {
                Cursor frequency = this.database.getFrequency(ids[1], Integer.parseInt(ids[0]));
                frequency.moveToFirst();
                FrequencyListModel tempModel = new FrequencyListModel();
                tempModel.setDatabaseId(Integer.parseInt(ids[0]));
                tempModel.setId(Integer.parseInt(ids[1]));
                try {
                    tempModel.setFrequency(Double.parseDouble(frequency.getString(frequency.getColumnIndex("frequency" + MainActivity.sLocale))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.freq.add(tempModel);
                frequency.close();
            }
        }
        list.setAdapter(new FrequencyListAdapter(this, this.freq));
        values.close();
        list.setOnItemClickListener(new C01861());
    }

    public String[] SequenceFrequencies(String unaltered) {
        return unaltered.split("-");
    }

    public void playSequenceButton(View view) {
        for (String i : this.freq_id) {
            String[] ids = i.split(",");
            this.database.addToPlaylist(ids[1], Integer.parseInt(ids[0]), this.Seq_name, this.seq_id, this.seq_db_id);
        }
        startActivity(new Intent(this, FrequencyUIActivity.class));
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void adjustLanguage() {
        Locale locale = new Locale(this.settings.getString("language", "en"));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_main);
        this.locale = "";
        if (!currentlang.equals("en")) {
            this.locale = "_" + currentlang;
        }
    }
}
