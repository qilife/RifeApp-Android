package com.zappkit.zappid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;

import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.lemeor.main_menu.fragments.programs.ProgramsAdapter;
import com.zappkit.zappid.lemeor.main_menu.player.FrequencyUIActivity;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class PlaylistActivity extends Activity {
    private static String currentlang;
    ProgramsAdapter SequenceAdapter;
    String[] Sequences;
    ArrayList<SequenceListModel> SequencesList;
    DbHelper database;
    private String locale;
    SharedPreferences settings;

    class C01831 implements OnItemClickListener {
        C01831() {
        }
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent seq = new Intent(PlaylistActivity.this.getBaseContext(), Sequence.class);
            seq.putExtra("position", ((SequenceListModel) PlaylistActivity.this.SequencesList.get(position)).getIdString());
            seq.putExtra("seq_db", ((SequenceListModel) PlaylistActivity.this.SequencesList.get(position)).getDatabaseId());
            PlaylistActivity.this.startActivity(seq);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.settings = SharedPreferenceHelper.getSharedPreferences(this);
        currentlang = this.settings.getString("language", "en");
        adjustLanguage();
        setContentView(R.layout.activity_playlist);
        this.database = new DbHelper(this);
        this.SequencesList = new ArrayList();
        this.SequenceAdapter = new ProgramsAdapter(this, this.SequencesList);
        TextView notes = (TextView) findViewById(R.id.playlist_notes);
        ListView list = (ListView) findViewById(R.id.playlist_sequences_list);
        Cursor playlist = this.database.getMyPlaylist(getIntent().getIntExtra("playlist_id", 1));
        playlist.moveToFirst();
        String notesTxt = playlist.getString(playlist.getColumnIndex("description"));
        if (!notesTxt.equals("")) {
            notes.setText(notesTxt);
        }
        String nameTxt = playlist.getString(playlist.getColumnIndex("name"));
        if (!nameTxt.equals("")) {
            setTitle(nameTxt);
        }
        this.Sequences = playlist.getString(playlist.getColumnIndex("list")).split("-");
        playlist.close();
        for (String seq : this.Sequences) {
            SequenceListModel tempSeqModel = new SequenceListModel();
            String[] tempIds = seq.split(",");
            Cursor tempSeqCursor = this.database.getSequence(tempIds[1], Integer.parseInt(tempIds[0]));
            tempSeqCursor.moveToFirst();
            tempSeqModel.setDbId(Integer.parseInt(tempIds[0]));
            tempSeqModel.setId(Integer.parseInt(tempIds[1]));
            tempSeqModel.setSequenceTitle(tempSeqCursor.getString(tempSeqCursor.getColumnIndex("name" + this.locale)));
            tempSeqModel.setNotes("");
            this.SequencesList.add(tempSeqModel);
            tempSeqCursor.close();
        }
        list.setAdapter(this.SequenceAdapter);
        list.setOnItemClickListener(new C01831());
    }

    public void playPlaylistButton(View view) {
        final ProgressDialog progDailog = ProgressDialog.show(this, getResources().getString(R.string.loadingDialogTitle), getResources().getString(R.string.loadingDialogMessage), true);
        new Thread() {
            public void run() {
                Iterator it = PlaylistActivity.this.SequencesList.iterator();
                while (it.hasNext()) {
                    SequenceListModel playModel = (SequenceListModel) it.next();
                    Cursor tempCursor = PlaylistActivity.this.database.getSequence(playModel.getIdString(), playModel.getDatabaseId());
                    tempCursor.moveToFirst();
                    for (String id : tempCursor.getString(tempCursor.getColumnIndex("list")).split("-")) {
                        String[] tempIds = id.split(",");
                        if (tempIds.length > 1) {
                            PlaylistActivity.this.database.addToPlaylist(tempIds[1], Integer.parseInt(tempIds[0]), playModel.getSequenceTitle(), playModel.getId(), playModel.getDatabaseId());
                        }
                    }
                }
                PlaylistActivity.this.startActivity(new Intent(PlaylistActivity.this.getApplicationContext(), FrequencyUIActivity.class));
                progDailog.dismiss();
            }
        }.start();
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
