package com.zappkit.zappid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.lemeor.main_menu.player.FrequencyUIActivity;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import android.view.View;
import android.widget.TextView;
import java.util.Locale;

public class Frequency extends Activity {
    DbHelper database;
    int freq_db_id;
    String id;
    SharedPreferences settings;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.settings = SharedPreferenceHelper.getSharedPreferences(this);
        adjustLanguage();
        setContentView(R.layout.frequency_activity_layout);

        database = new DbHelper(this);
    }

    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        this.freq_db_id = intent.getIntExtra("db_of_freq", 1);
        String value = intent.getStringExtra("id_value");
        this.id = value;
        String freq = "";
        TextView hz;
        if (this.freq_db_id == 1) {
            Cursor frequency = this.database.getFrequency(value, this.freq_db_id);
            frequency.moveToFirst();
            hz = (TextView) findViewById(R.id.freq_hz);
            freq = frequency.getString(frequency.getColumnIndex("frequency"));
            hz.setText(freq + " Hz");
            ((TextView) findViewById(R.id.freq_playcount)).setText(frequency.getString(frequency.getColumnIndex("playcount")));
            frequency.close();
        } else if (this.freq_db_id == 2) {
            Cursor myfrequency = this.database.getFrequency(value, this.freq_db_id);
            myfrequency.moveToFirst();
            hz = (TextView) findViewById(R.id.freq_hz);
            freq = myfrequency.getString(myfrequency.getColumnIndex("frequency"));
            hz.setText(freq + " Hz");
            ((TextView) findViewById(R.id.freq_playcount)).setText(myfrequency.getString(myfrequency.getColumnIndex("playcount")));
            myfrequency.close();
        }
        setTitle(getResources().getString(R.string.title_activity_frequency) + ": " + freq + " Hz");
    }

    public void playFrequencyButton(View view) {
        this.database.addToPlaylist(this.id, this.freq_db_id, getResources().getString(R.string.title_activity_frequency), -1, 1);
        startActivity(new Intent(this, FrequencyUIActivity.class));
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
