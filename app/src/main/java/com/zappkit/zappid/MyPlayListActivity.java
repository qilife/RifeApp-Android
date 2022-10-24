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
import com.zappkit.zappid.lemeor.main_menu.fragments.programs.ProgramsAdapter;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.Iterator;
import java.util.Locale;

public class MyPlayListActivity extends Activity {
    private static String currentlang;
    private static String locale1;
    DbHelper database;
    int editPlayId;
    BroadcastReceiver editReceiver;
    InputMethodManager imm;
    String newPlayListname;
    String newPlayListnotes;
    ArrayList<SequenceListModel> newPlayListseq;
    BroadcastReceiver removeReceiver;
    BroadcastReceiver removeSeqReceiver;
    PlayListSequenceListAdapter seqListAdapter;
    SharedPreferences settings;

    ProgramsAdapter adapter;
    EditText editText;
    ListView sequenceList;
    ArrayList<SequenceListModel> sequences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.settings = SharedPreferenceHelper.getSharedPreferences(this);
        currentlang = this.settings.getString("language", "en");
        adjustLanguage();
        setContentView(R.layout.playlistactivity_layout);
        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.database = new DbHelper(this);
        this.newPlayListname = "";
        this.newPlayListnotes = "";
        this.newPlayListseq = new ArrayList();
        this.editPlayId = -1;
        final ArrayList<PlayListListModel> playlists = new ArrayList();
        ListView mainlist = (ListView) findViewById(R.id.myplaylistmain);
        final PlayListListAdapter adapter = new PlayListListAdapter(this, playlists);
        mainlist.setAdapter(adapter);
        mainlist.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MyPlayListActivity.this.getApplicationContext(), PlaylistActivity.class);
                intent.putExtra("playlist_id", ((PlayListListModel) playlists.get(position)).getId());
                MyPlayListActivity.this.startActivity(intent);
            }
        });
        Cursor playlistcursor = this.database.getMyPlaylists();
        if (playlistcursor.getCount() != 0) {
            ((TextView) findViewById(R.id.myPlaylistNoContentList)).setVisibility(View.GONE);
            playlistcursor.moveToFirst();
            do {
                PlayListListModel tempPlaylistModel = new PlayListListModel();
                tempPlaylistModel.setName(playlistcursor.getString(playlistcursor.getColumnIndex("name")));
                tempPlaylistModel.setNotes(playlistcursor.getString(playlistcursor.getColumnIndex("description")));
                tempPlaylistModel.setId(playlistcursor.getInt(playlistcursor.getColumnIndex("_id")));
                tempPlaylistModel.setList(playlistcursor.getString(playlistcursor.getColumnIndex("list")));
                playlists.add(tempPlaylistModel);
            } while (playlistcursor.moveToNext());
        }
        playlistcursor.close();
        adapter.notifyDataSetChanged();
        this.removeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int position = intent.getIntExtra("position", -1);
                if (position != -1) {
                    MyPlayListActivity.this.database.deleteMyPlayList(((PlayListListModel) playlists.get(position)).getId());
                    playlists.remove(position);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(this.removeReceiver, new IntentFilter("removePlaylist"));
        this.editReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int pos = intent.getIntExtra("position", -1);
                if (pos != -1) {
                    MyPlayListActivity.this.newPlayListname = ((PlayListListModel) playlists.get(pos)).getName();
                    MyPlayListActivity.this.newPlayListnotes = ((PlayListListModel) playlists.get(pos)).getNotes();
                    Iterator i$ = ((PlayListListModel) playlists.get(pos)).getArrayList().iterator();
                    while (i$.hasNext()) {
                        String[] tempIds = ((String) i$.next()).split(",");
                        Cursor sequences = MyPlayListActivity.this.database.getSequence(tempIds[1], Integer.parseInt(tempIds[0]));
                        if (sequences.getCount() != 0) {
                            sequences.moveToFirst();
                            SequenceListModel tempModel = new SequenceListModel();
                            tempModel.setSequenceTitle(sequences.getString(sequences.getColumnIndex("name")));
                            tempModel.setNotes("");
                            tempModel.setId(sequences.getInt(sequences.getColumnIndex("_id")));
                            tempModel.setDbId(1);
                            MyPlayListActivity.this.newPlayListseq.add(tempModel);
                        }
                        sequences.close();
                    }
                    MyPlayListActivity.this.editPlayId = ((PlayListListModel) playlists.get(pos)).getId();
                    MyPlayListActivity.this.createNewPlaylist(MyPlayListActivity.this.getCurrentFocus());
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(this.editReceiver, new IntentFilter("editPlaylist"));
        this.removeSeqReceiver = new C01554();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.removeSeqReceiver, new IntentFilter("removeSeqFromPlaylist"));
    }

    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.removeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.editReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.removeSeqReceiver);
    }

    public void createNewPlaylist(View view) {
        setContentView(R.layout.myplaylist_nameandnotes);
        EditText newnotes = (EditText) findViewById(R.id.editnewplaylistnotes);
        ((EditText) findViewById(R.id.editnewplaylistname)).setText(this.newPlayListname);
        newnotes.setText(this.newPlayListnotes);
    }

    public void cancel_playlist_add(View view) {
        this.newPlayListname = "";
        this.newPlayListnotes = "";
        this.newPlayListseq.clear();
        refresh();
    }

    public void add_nameandnotes_clicked(View view) {
        EditText newname = (EditText) findViewById(R.id.editnewplaylistname);
        this.newPlayListname = newname.getText().toString();
        EditText newnotes = (EditText) findViewById(R.id.editnewplaylistnotes);
        this.newPlayListnotes = newnotes.getText().toString();
        if (this.newPlayListname.equals("")) {
            Toast.makeText(this, getResources().getString(R.string.myPlaylistMissingName), Toast.LENGTH_SHORT).show();
            return;
        }
        newname.clearFocus();
        newnotes.clearFocus();
        this.imm.hideSoftInputFromWindow(newname.getWindowToken(), 0);
        setContentChooseSequences(view);
    }

    public void setContentChooseSequences(View view) {
        setContentView(R.layout.myplaylist_seqlist);
        ListView chosenseq = (ListView) findViewById(R.id.myPlaylistSeqList);
        this.seqListAdapter = new PlayListSequenceListAdapter(this, this.newPlayListseq);
        chosenseq.setAdapter(this.seqListAdapter);
        this.seqListAdapter.notifyDataSetChanged();
        if (this.newPlayListseq.size() != 0) {
            ((TextView) findViewById(R.id.myPlaylistNoAddedSeqText)).setVisibility(View.GONE);
        }
        if (this.editPlayId != -1) {
            ((Button) findViewById(R.id.myPlayListAddBtn)).setText(getResources().getString(R.string.myPlaylistEditBtn));
        }
    }

    public void add_SequenceLayoutOpen(View view) {
        setContentView(R.layout.add_sequence_to_playlist_layout);
        sequenceList = (ListView) findViewById(R.id.SequenceToAddList);
        editText = findViewById(R.id.menu_search);
        sequences = new ArrayList();
        adapter = new ProgramsAdapter(this, sequences);
        sequenceList.setAdapter(adapter);
        method("");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int inputType = editText.getInputType();
                String text = editText.getText().toString().toLowerCase(Locale.getDefault());
                method(s.toString());

            }
        });



    }

    private void method(String s) {
        Cursor Sequences = this.database.getSequences();
        sequences.clear();
        if (Sequences.getCount() > 0) {
            Sequences.moveToFirst();
            do {
                String name = Sequences.getString(Sequences.getColumnIndex("name"));
                String notes = Sequences.getString(Sequences.getColumnIndex("description")).toLowerCase(Locale.getDefault());
                String OriginalName = name;
                name = name.toLowerCase(Locale.getDefault());
                if (name.contains(s)) {
                    SequenceListModel seqModel = new SequenceListModel();
                    seqModel.setSequenceTitle(Sequences.getString(Sequences.getColumnIndex("name" + locale1)));
                    seqModel.setNotes("");
                    seqModel.setId(Sequences.getInt(Sequences.getColumnIndex("_id")));
                    seqModel.setDbId(1);
                    sequences.add(seqModel);
                }
            } while (Sequences.moveToNext());
        }
        Sequences.close();
        Cursor mySequences = this.database.getMySequences();
        if (mySequences.getCount() != 0) {
            mySequences.moveToFirst();
            do {
                String name = mySequences.getString(mySequences.getColumnIndex("name"));
                String notes = mySequences.getString(mySequences.getColumnIndex("description")).toLowerCase(Locale.getDefault());
                name = name.toLowerCase(Locale.getDefault());
                if (name.contains(s)) {
                    SequenceListModel seqModel = new SequenceListModel();
                    seqModel.setSequenceTitle(mySequences.getString(mySequences.getColumnIndex("name")));
                    seqModel.setNotes("");
                    seqModel.setId(mySequences.getInt(mySequences.getColumnIndex("_id")));
                    seqModel.setDbId(2);
                    sequences.add(seqModel);
                }
            } while (mySequences.moveToNext());
        }
        mySequences.close();
        Collections.sort(sequences, new C01565());
        adapter = new ProgramsAdapter(this, sequences);
        sequenceList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        sequenceList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MyPlayListActivity.this.newPlayListseq.add(sequences.get(position));
                MyPlayListActivity.this.setContentChooseSequences(view);
            }
        });
    }

    public void backToNameAndNotes(View view) {
        createNewPlaylist(view);
    }

    public void addPlaylistToMyPlaylists(View view) {
        if (this.newPlayListseq.size() != 0) {
            String list = "";
            StringBuilder stringBuilder = new StringBuilder();
            boolean firstrun = true;
            Iterator i$ = this.newPlayListseq.iterator();
            while (i$.hasNext()) {
                SequenceListModel loopModel = (SequenceListModel) i$.next();
                if (firstrun) {
                    stringBuilder.append(loopModel.getDatabaseId());
                    stringBuilder.append(",");
                    stringBuilder.append(loopModel.getIdString());
                    firstrun = false;
                } else {
                    stringBuilder.append("-");
                    stringBuilder.append(loopModel.getDatabaseId());
                    stringBuilder.append(",");
                    stringBuilder.append(loopModel.getIdString());
                }
            }
            list = stringBuilder.toString();
            if (this.editPlayId == -1) {
                this.database.insertMyPlaylist(this.newPlayListname, this.newPlayListnotes, list);
            } else {
                this.database.updateMyPlaylist(this.editPlayId, this.newPlayListname, this.newPlayListnotes, list);
            }
            refresh();
            return;
        }
        Toast.makeText(this, getResources().getString(R.string.myPlaylistNoSeqAdded), Toast.LENGTH_SHORT).show();
    }

    public void backToSequencesAdded(View view) {
        setContentChooseSequences(view);
    }

    private void refresh() {
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
        locale1 = "";
        if (!currentlang.equals("en")) {
            locale1 = "_" + currentlang;
        }
    }

    class C01554 extends BroadcastReceiver {
        C01554() {
        }

        public void onReceive(Context context, Intent intent) {
            MyPlayListActivity.this.newPlayListseq.remove(intent.getIntExtra("position", 0));
            MyPlayListActivity.this.seqListAdapter.notifyDataSetChanged();
        }
    }

    class C01565 implements Comparator<SequenceListModel> {
        C01565() {
        }

        public int compare(SequenceListModel lhs, SequenceListModel rhs) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getSequenceTitle(), rhs.getSequenceTitle());
            return res != 0 ? res : lhs.getSequenceTitle().compareTo(rhs.getSequenceTitle());
        }
    }
}
