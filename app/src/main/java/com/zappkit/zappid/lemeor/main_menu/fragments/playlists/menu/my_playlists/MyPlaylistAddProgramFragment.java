package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_playlists;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import com.zappkit.zappid.lemeor.tools.SharedPreferenceHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.main_menu.fragments.programs.ProgramsAdapter;
import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.base.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MyPlaylistAddProgramFragment extends BaseFragment {
    private DbHelper database;
    private String mLocale;
    private Button mBtnBack;
    private ListView sequenceList;
    private EditText mEditText;
    private ArrayList<SequenceListModel> sequencesList;
    private ProgramsAdapter mProgramsAdapter;

    public static MyPlaylistAddProgramFragment newInstance() { return new MyPlaylistAddProgramFragment(); }

    @Override
    protected int initLayout() {
        return R.layout.fragment_my_playlist_add_program;
    }

    @Override
    protected void initComponents() {
        setTitleToActivity(getString(R.string.tv_program));
        SharedPreferences sharedPreferences = SharedPreferenceHelper.getSharedPreferences(mContext);
        String currentLang = sharedPreferences.getString("language", "en");
        mLocale = "";
        if (!currentLang.equals("en")) { mLocale = "_" + currentLang; }

        database = new DbHelper(mContext);
        mBtnBack = mView.findViewById(R.id.btnBack);

        sequenceList = mView.findViewById(R.id.SequenceToAddList);
        mEditText = mView.findViewById(R.id.menu_search);
        sequencesList = new ArrayList<>();
        mProgramsAdapter = new ProgramsAdapter((Activity) mContext, sequencesList);
        sequenceList.setAdapter(mProgramsAdapter);
        method("");
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mEditText.getText().toString().toLowerCase(Locale.getDefault());
                method(text);
            }
        });
    }

    private void method(String s) {
        Cursor Sequences = this.database.getSequences();
        sequencesList.clear();
        if (Sequences.getCount() > 0) {
            Sequences.moveToFirst();
            do {
                String name = Sequences.getString(Sequences.getColumnIndex("name"));
                name = name.toLowerCase(Locale.getDefault());
                if (name.contains(s)) {
                    SequenceListModel seqModel = new SequenceListModel();
                    seqModel.setSequenceTitle(Sequences.getString(Sequences.getColumnIndex("name" + mLocale)));
                    seqModel.setNotes("");
                    seqModel.setId(Sequences.getInt(Sequences.getColumnIndex("_id")));
                    seqModel.setDbId(1);
                    sequencesList.add(seqModel);
                }
            } while (Sequences.moveToNext());
        }
        Sequences.close();
        Cursor mySequences = this.database.getMySequences();
        if (mySequences.getCount() != 0) {
            mySequences.moveToFirst();
            do {
                String name = mySequences.getString(mySequences.getColumnIndex("name"));
                name = name.toLowerCase(Locale.getDefault());
                if (name.contains(s)) {
                    SequenceListModel seqModel = new SequenceListModel();
                    seqModel.setSequenceTitle(mySequences.getString(mySequences.getColumnIndex("name")));
                    seqModel.setNotes("");
                    seqModel.setId(mySequences.getInt(mySequences.getColumnIndex("_id")));
                    seqModel.setDbId(2);
                    sequencesList.add(seqModel);
                }
            } while (mySequences.moveToNext());
        }
        mySequences.close();
        Collections.sort(sequencesList, new SortOrder());
        mProgramsAdapter = new ProgramsAdapter((Activity) mContext, sequencesList);
        sequenceList.setAdapter(mProgramsAdapter);
        mProgramsAdapter.notifyDataSetChanged();
        sequenceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent("addProgramToPlaylist");
                intent.putExtra("Program", sequencesList.get(position));
                mContext.sendBroadcast(intent);
                onBackToPreFragment();
            }
        });
    }

    @Override
    protected void addListener() {
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackToPreFragment();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setTitleToActivity(getString(R.string.tv_my_playlist));
    }

    static class SortOrder implements Comparator<SequenceListModel> {
        SortOrder() { }

        public int compare(SequenceListModel lhs, SequenceListModel rhs) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getSequenceTitle(), rhs.getSequenceTitle());
            return res != 0 ? res : lhs.getSequenceTitle().compareTo(rhs.getSequenceTitle());
        }
    }
}
