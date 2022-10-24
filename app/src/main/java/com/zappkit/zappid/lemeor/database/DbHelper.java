package com.zappkit.zappid.lemeor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "frequency.db";
    private static final int DATABASE_VERSION = 12;
    public static final String FH_CURRENT_PLAYLIST = "Current_playlist";
    private static final String FH_CURRENT_PLAYLIST_CREATE = "CREATE TABLE Current_playlist(_id INTEGER PRIMARY KEY  AUTOINCREMENT,frequency_id TEXT NOT NULL,frequency_db_id INTEGER,state TEXT NOT NULL,time_lapsed INTEGER,sequence TEXT NOT NULL,sequence_id INTEGER,sequence_db_id INTEGER,loop INTEGER,play_status INTEGER DEFAULT 0);";
    private static final String FH_LIST_TABLE_CREATE = "CREATE TABLE Frequency_lists(_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,list TEXT NOT NULL,description TEXT NOT NULL,name_is TEXT NOT NULL,description_is TEXT NOT NULL);";
    private static final String FH_PERSONAL_SEQUENCES_CREATE = "CREATE TABLE Personal_Sequences(_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,list TEXT NOT NULL,description TEXT NOT NULL );";
    public static final String FH_PERSONAL_SEQUENCES_NAME = "Personal_Sequences";
    private static final String FH_TABLE_CREATE = "CREATE TABLE Frequencies(_id INTEGER PRIMARY KEY AUTOINCREMENT,frequency TEXT NOT NULL,playcount INTEGER,volume INTEGER,added_by_user TEXT NOT NULL);";
    public static final String FH_TABLE_NAME = "Frequencies";
    private static final String LoopTrigger = "INSERT INTO User_triggers values(2,'LoopOptions',0)";
    private static final String MYZAPP_FREQUENCIES_CREATE = "CREATE TABLE Myzapp_frequencies(_id INTEGER PRIMARY KEY AUTOINCREMENT,frequency TEXT NOT NULL,playcount INTEGER );";
    public static final String MYZAPP_FREQUENCIES_NAME = "Myzapp_frequencies";
    private static final String MYZAPP_PLAYLISTS_CREATE = "CREATE TABLE Myzapp_playlists(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, list TEXT NOT NULL, description TEXT NOT NULL);";
    public static final String MYZAPP_PLAYLISTS_NAME = "Myzapp_playlists";
    private static final String TermsTrigger = "INSERT INTO User_triggers values(1,'ReadTerms',0)";
    private static final String ZAPP_TRIGGERS_CREATE = "CREATE TABLE User_triggers(_id INTEGER PRIMARY KEY  AUTOINCREMENT,trigger_name TEXT NOT NULL,triggered INTEGER);";
    private Context mContext;
    private SQLiteDatabase reader;
    private SQLiteDatabase writer;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        reader = getReadableDatabase();
        writer = getWritableDatabase();
    }

    public static String getDbHelperName() {
        return DATABASE_NAME;
    }

    public void onCreate(final SQLiteDatabase db) {
        try {
            db.execSQL(FH_TABLE_CREATE);
            db.execSQL(FH_LIST_TABLE_CREATE);
            db.execSQL(ZAPP_TRIGGERS_CREATE);
            db.execSQL(FH_CURRENT_PLAYLIST_CREATE);
            db.execSQL(FH_PERSONAL_SEQUENCES_CREATE);
            db.execSQL(MYZAPP_FREQUENCIES_CREATE);
            db.execSQL(MYZAPP_PLAYLISTS_CREATE);
            db.execSQL(TermsTrigger);
            db.execSQL(LoopTrigger);
            try {
                populateDB(db);
            } catch (IOException e1) {
                Toast.makeText(mContext, "Exception populateDB: " + e1.getMessage(), Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Exception onCreate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion < 2) {
            onCreate(db);
        } else if (newVersion == 2) {
            db.execSQL("DELETE FROM Frequency_lists WHERE list = ''");
        } else if (newVersion == 3) {
            db.execSQL("DELETE FROM Frequency_lists WHERE list = ''");
        }
    }

    public void updateMySequence(int id, String name, String notes, String list) {
        ContentValues args = new ContentValues();
        String filter = "_id='" + id + "'";
        args.put("name", name);
        args.put("description", notes);
        args.put("list", list);
        this.writer.update(FH_PERSONAL_SEQUENCES_NAME, args, filter, null);
    }

    public void updateMyPlaylist(int id, String name, String notes, String list) {
        ContentValues args = new ContentValues();
        String filter = "_id='" + id + "'";
        args.put("name", name);
        args.put("description", notes);
        args.put("list", list);
        this.writer.update(MYZAPP_PLAYLISTS_NAME, args, filter, null);
    }

    public void deleteMyFrequency(int id) {
        Iterator<Integer> i$;
        String search = "2," + id;
        Cursor sequences = this.reader.rawQuery("SELECT * FROM Personal_Sequences WHERE list LIKE '%" + search + "%'", null);
        ArrayList<String> newLists = new ArrayList<>();
        ArrayList<Integer> changeIds = new ArrayList<>();
        int changecount = 0;
        ArrayList<Integer> deleteIds = new ArrayList<>();
        if (sequences.getCount() != 0) {
            sequences.moveToFirst();
            do {
                int seqId = sequences.getInt(sequences.getColumnIndex("_id"));
                String[] tempListItems = sequences.getString(sequences.getColumnIndex("list")).split("-");
                StringBuilder newSeqList = new StringBuilder();
                for (String tempItem : tempListItems) {
                    if (!tempItem.equals(search)) {
                        if (newSeqList.length() != 0) {
                            newSeqList.append("-");
                        }
                        newSeqList.append(tempItem);
                    }
                }
                if (newSeqList.length() == 0) {
                    deleteIds.add(seqId);
                } else {
                    newLists.add(newSeqList.toString());
                    changeIds.add(seqId);
                    changecount++;
                }
            } while (sequences.moveToNext());
        }
        sequences.close();
        if (deleteIds.size() != 0) {
            i$ = deleteIds.iterator();
            while (i$.hasNext()) {
                deleteMySequence(i$.next());
            }
        }
        if (changecount != 0) {
            for (int i = 0; i < changecount; i++) {
                ContentValues args = new ContentValues();
                String filter = "_id='" + changeIds.get(i) + "'";
                args.put("list", newLists.get(i));
                this.writer.update(FH_PERSONAL_SEQUENCES_NAME, args, filter, null);
            }
        }
        this.writer.delete(MYZAPP_FREQUENCIES_NAME, "_id='" + id + "'", null);
    }

    public void deleteMySequence(int id) {
        Iterator<Integer> i$;
        String search = "2," + id;
        Cursor playlists = this.reader.rawQuery("SELECT * FROM Myzapp_playlists WHERE list LIKE '%" + search + "%'", null);
        ArrayList<String> newLists = new ArrayList<>();
        ArrayList<Integer> changeIds = new ArrayList<>();
        int changecount = 0;
        ArrayList<Integer> deleteIds = new ArrayList<>();
        if (playlists.getCount() != 0) {
            playlists.moveToFirst();
            do {
                int playlistId = playlists.getInt(playlists.getColumnIndex("_id"));
                String[] tempListItems = playlists.getString(playlists.getColumnIndex("list")).split("-");
                StringBuilder newPlayList = new StringBuilder();
                for (String tempItem : tempListItems) {
                    if (!tempItem.equals(search)) {
                        if (newPlayList.length() != 0) {
                            newPlayList.append("-");
                        }
                        newPlayList.append(tempItem);
                    }
                }
                if (newPlayList.length() == 0) {
                    deleteIds.add(playlistId);
                } else {
                    newLists.add(newPlayList.toString());
                    changeIds.add(playlistId);
                    changecount++;
                }
            } while (playlists.moveToNext());
        }
        playlists.close();
        if (deleteIds.size() != 0) {
            i$ = deleteIds.iterator();
            while (i$.hasNext()) {
                deleteMyPlayList(i$.next());
            }
        }
        if (changecount != 0) {
            for (int i = 0; i < changecount; i++) {
                ContentValues args = new ContentValues();
                String filter = "_id='" + changeIds.get(i) + "'";
                args.put("list", newLists.get(i));
                this.writer.update(MYZAPP_PLAYLISTS_NAME, args, filter, null);
            }
        }
        this.writer.delete(FH_PERSONAL_SEQUENCES_NAME, "_id='" + id + "'", null);
    }

    public void deleteMyPlayList(int id) {
        this.writer.delete(MYZAPP_PLAYLISTS_NAME, "_id='" + id + "'", null);
    }

    public void insertMyPlaylist(String name, String notes, String list) {
        ContentValues args = new ContentValues();
        args.put("name", name);
        args.put("description", notes);
        args.put("list", list);
        this.writer.insert(MYZAPP_PLAYLISTS_NAME, null, args);
    }

    public Cursor getMyPlaylists() {
        return this.reader.rawQuery("SELECT * FROM Myzapp_playlists", null);
    }

    public Cursor getMyPlaylist(int id) {
        return this.reader.rawQuery("SELECT * FROM Myzapp_playlists WHERE _id=" + id, null);
    }

    public void insertMySequence(String name, String notes, String list) {
        ContentValues args = new ContentValues();
        args.put("name", name);
        args.put("description", notes);
        args.put("list", list);
        this.writer.insert(FH_PERSONAL_SEQUENCES_NAME, null, args);
    }

    public Cursor getMySequences() {
        return this.reader.rawQuery("SELECT * FROM Personal_Sequences", null);
    }

    public void insertMyFrequency(String frequency) {
        ContentValues arg = new ContentValues();
        arg.put("frequency", frequency);
        arg.put("playcount", 0);
        this.writer.insert(MYZAPP_FREQUENCIES_NAME, null, arg);
    }

    public Cursor getMyFrequencies() {
        return this.reader.rawQuery("SELECT * FROM Myzapp_frequencies", null);
    }

    public Cursor getMyFrequencies_isavailbe(String freq) {
        return this.reader.rawQuery("SELECT * FROM Myzapp_frequencies where frequency = '" + freq + "'", null);
    }


    public Cursor getFrequencies() {
        return this.reader.rawQuery("SELECT * FROM Frequencies", null);
    }

    public Cursor getFrequencies_search(String s) {
        return this.reader.rawQuery("SELECT * FROM Frequencies where frequency LIKE '%" + s + "%'", null);
    }


    public Cursor getFrequency(String id, int freq_db_id) {
        String query;
        if (freq_db_id == 1) {
            query = "SELECT * FROM Frequencies WHERE _id='" + id + "'";
        } else {
            query = "SELECT * FROM Myzapp_frequencies WHERE _id='" + id + "'";
        }
        return this.reader.rawQuery(query, null);
    }

    public String getFrequencyString(String id, int freq_db_id) {
        String query;
        if (freq_db_id == 1) {
            query = "SELECT * FROM Frequencies WHERE _id='" + id + "'";
        } else {
            query = "SELECT * FROM Myzapp_frequencies WHERE _id='" + id + "'";
        }
        Cursor frequency = this.reader.rawQuery(query, null);
        String outputValue = "0";
        if (frequency.getCount() > 0) {
            frequency.moveToFirst();
            outputValue = frequency.getString(frequency.getColumnIndex("frequency"));
            outputValue = outputValue.replace("146713.583 W1 G0 A20 O0", "146713.583").replace("70D50", "7050");
        }
        frequency.close();
        return outputValue;
    }

    public Cursor getSequences() {
        return this.reader.rawQuery("SELECT * FROM Frequency_lists", null);
    }

    public Cursor getSequence(String id, int dbId) {
        String query;
        if (dbId == 1) {
            query = "SELECT * FROM Frequency_lists WHERE _id='" + id + "'";
        } else if (dbId == 2) {
            query = "SELECT * FROM Personal_Sequences WHERE _id='" + id + "'";
        } else {
            query = "SELECT * FROM Frequency_lists WHERE _id='" + id + "'";
        }
        return this.reader.rawQuery(query, null);
    }

    public void frequencyPlayed(String id) {
        Cursor data = this.reader.rawQuery("SELECT * FROM Frequencies WHERE _id='" + id + "'", null);
        data.moveToFirst();
        String filter = "_id='" + id + "'";
        ContentValues args = new ContentValues();
        args.put("playcount", data.getInt(data.getColumnIndex("playcount")) + 1);
        data.close();
        this.writer.update(FH_TABLE_NAME, args, filter, null);
    }

    public void addToPlaylist(String id, int freq_db_id, String seq, int seq_id, int seq_db_id) {
        ContentValues args = new ContentValues();
        args.put("frequency_id", id);
        args.put("state", "play");
        args.put("sequence", seq);
        args.put("sequence_id", seq_id);
        args.put("loop", 0);
        args.put("frequency_db_id", freq_db_id);
        args.put("sequence_db_id", seq_db_id);
        this.writer.insert(FH_CURRENT_PLAYLIST, null, args);
    }

    public void empty_playlist() {
        this.writer.delete(FH_CURRENT_PLAYLIST, null, null);
    }

    public Cursor getPlayList() {
        return this.reader.rawQuery("SELECT * FROM Current_playlist", null);
    }

    public int getPlaylistCount() {
        Cursor playitem = this.reader.rawQuery("SELECT COUNT(*) from Current_playlist", null);
        playitem.moveToFirst();
        int count = playitem.getInt(0);
        playitem.close();
        return count;
    }

    public int getPlaylistCountPlaying() {
        Cursor playitem = this.reader.rawQuery("SELECT COUNT(*) from Current_playlist WHERE play_status = 0", null);
        playitem.moveToFirst();
        int count = playitem.getInt(0);
        playitem.close();
        return count;
    }

    public int getPlaylistCountPlayed() {
        Cursor playitem = this.reader.rawQuery("SELECT COUNT(*) from Current_playlist WHERE play_status = 1", null);
        playitem.moveToFirst();
        int count = playitem.getInt(0);
        playitem.close();
        return count;
    }

    public void updatePlaylistToPlaying() {
        ContentValues args = new ContentValues();
        args.put("play_status", 0);
        this.writer.update(FH_CURRENT_PLAYLIST, args, null, null);
    }

    public void updatePlaylistToPlayingFromId(int id, double d) {
        ContentValues args = new ContentValues();
        args.put("play_status", 0);
        args.put("state", "pause");
        args.put("time_lapsed", (int) d);
        this.writer.update(FH_CURRENT_PLAYLIST, args, "_id" + " > " + (id - 1), null);

        args = new ContentValues();
        args.put("play_status", 1);
        args.put("state", "play");
        this.writer.update(FH_CURRENT_PLAYLIST, args, "_id" + " < " + id, null);
    }

    public void updatePlaylistToPlayed() {
        ContentValues args = new ContentValues();
        args.put("play_status", 1);
        this.writer.update(FH_CURRENT_PLAYLIST, args, null, null);
    }

    public String getNextFreqOnPlayList() {
        String output = "done";
        try {
            Cursor next = this.reader.rawQuery("SELECT * FROM Current_playlist WHERE play_status = 0", null);
            if (next.moveToFirst()) {
                next.moveToFirst();
                output = next.getString(next.getColumnIndex("frequency_id"));
            } else {
                output = "done!";
                this.writer.delete(FH_CURRENT_PLAYLIST, null, null);
            }
            next.close();
        } catch (SQLiteException ignored){}
        return output;
    }

    public void loopFrequency() {
        Cursor item = this.reader.rawQuery("SELECT * FROM Current_playlist LIMIT 1", null);
        item.moveToFirst();
        String tempFreqId = item.getString(item.getColumnIndex("frequency_id"));
        String tempSequence = item.getString(item.getColumnIndex("sequence"));
        int tempLoop = item.getInt(item.getColumnIndex("loop"));
        int tempSeqId = item.getInt(item.getColumnIndex("sequence_id"));
        int tempFreqDbId = item.getInt(item.getColumnIndex("frequency_db_id"));
        int tempSeqDbId = item.getInt(item.getColumnIndex("sequence_db_id"));
        item.close();
        ContentValues args = new ContentValues();
        args.put("frequency_id", tempFreqId);
        args.put("frequency_db_id", tempFreqDbId);
        args.put("state", "play");
        args.put("sequence", tempSequence);
        args.put("sequence_id", tempSeqId);
        args.put("sequence_db_id", tempSeqDbId);
        args.put("loop", tempLoop);
        this.writer.insert(FH_CURRENT_PLAYLIST, null, args);
    }

    public boolean shouldLoop() {
        Cursor item = this.reader.rawQuery("SELECT * FROM Current_playlist LIMIT 1", null);
        item.moveToFirst();
        if (item.getInt(item.getColumnIndex("loop")) == 1) {
            item.close();
            return true;
        }
        item.close();
        return false;
    }

    public void DeletePayedItemFromPlayList() {
        String idColumn = null;
        Cursor next = this.reader.rawQuery("SELECT * FROM Current_playlist WHERE play_status = 0", null);
        if (next.moveToFirst()) {
            next.moveToFirst();
            idColumn = next.getString(next.getColumnIndex("_id"));
        }
        next.close();
        if (idColumn != null) {
            ContentValues args = new ContentValues();
            String filter = "_id='" + idColumn + "'";
            args.put("play_status", 1);
            this.writer.update(FH_CURRENT_PLAYLIST, args, filter, null);
        }
    }

    public void ChangeFirstItemState(String newState, double d) {
        Cursor next = this.reader.rawQuery("SELECT * FROM Current_playlist WHERE play_status = 0", null);
        if (next.moveToFirst()) {
            ContentValues args = new ContentValues();
            args.put("state", newState);
            if (newState.equals("pause")) {
                args.put("time_lapsed", (int) d);
            }
            String filter = "_id='" + next.getString(next.getColumnIndex("_id")) + "'";
            next.close();
            this.writer.update(FH_CURRENT_PLAYLIST, args, filter, null);
        }
    }

    public void ResetAllItemState() {
        ContentValues args = new ContentValues();
        args.put("state", "play");
        args.put("time_lapsed", 0);
        args.put("play_status", 0);
        this.writer.update(FH_CURRENT_PLAYLIST, args, null, null);
    }

    public Cursor getPlayListItemsCursor() {
        return this.reader.rawQuery("SELECT * FROM Current_playlist WHERE play_status = 0", null);
    }

    public void populateDB(SQLiteDatabase db) throws IOException {
        byte[] buffer = new byte[900000];
        this.mContext.getAssets().open("List_EN_IS_Final.txt").read(buffer);
        ArrayList<String> rows = new ArrayList(Arrays.asList(new String(buffer).split("[\\r\\n]+")));
        rows.remove(rows.size() - 1);
        ArrayList<String> frequenciesForDB = new ArrayList<>();
        StringBuilder tempFreqValues = new StringBuilder();
        for (String row : rows) {
            int i;
            String[] line = row.split(";");
            if (line.length == 5) {
                String[] indFreq = line[4].split("/");
                for (String s : indFreq) {
                    double freg = 0;
                    try {
                        freg = Double.parseDouble(s);
                    } catch (NumberFormatException ignored) { }

                    if (freg < 20000) {
                        if (frequenciesForDB.contains(s)) {
                            for (i = 0; i < frequenciesForDB.size(); i++) {
                                if (frequenciesForDB.get(i).equals(s)) {
                                    if (tempFreqValues.toString().equals("")) {
                                        tempFreqValues = new StringBuilder("1," + i);
                                    } else {
                                        tempFreqValues.append("-1,").append(i);
                                    }
                                }
                            }
                        } else {
                            frequenciesForDB.add(s);
                            System.out.println("indFreq: " + s);
                            if (tempFreqValues.toString().equals("")) {
                                tempFreqValues = new StringBuilder("1," + (frequenciesForDB.size() - 1));
                            } else {
                                tempFreqValues.append("-1,").append((frequenciesForDB.size() - 1));
                            }
                        }
                    }

                }
                if (!line[0].equals("1") && tempFreqValues.length() > 0) {
                    System.out.println("tempFreqValues: " + tempFreqValues);
                    db.execSQL("INSERT INTO Frequency_lists (name, list, description, name_is, description_is) values('" + line[0] + "', '" + tempFreqValues + "', '" + line[1] + "', '" + line[2] + "', '" + line[3] + "');");
                    tempFreqValues = new StringBuilder();
                }
            } else {
                System.out.println(line[0] + ", lengd: " + line.length);
            }
        }
        int size = frequenciesForDB.size();
        for (int i = 0; i < size; i++) {
            db.execSQL("INSERT INTO Frequencies values(" + i + ", '" + frequenciesForDB.get(i) + "', 0, 100, 'false');");
        }
    }

    public void clear() {
        this.writer.execSQL("DELETE FROM Current_playlist");
        this.writer.execSQL("DELETE FROM Frequencies");
        this.writer.execSQL("DELETE FROM Frequency_lists");
        this.writer.execSQL("DELETE FROM Myzapp_frequencies");
        this.writer.execSQL("DELETE FROM Myzapp_playlists");
        this.writer.execSQL("DELETE FROM User_triggers");
    }
}
