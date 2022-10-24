package com.zappkit.zappid.lemeor.main_menu.fragments.programs;

import android.app.Activity;
import android.database.Cursor;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.main_menu.MainActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.tools.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class ProgramsFragment extends BaseFragment {

    private ArrayList<SequenceListModel> mProgramsList = new ArrayList<>();

    private EditText etSearch;
    private DbHelper mDbHelper;

    private ProgramsAdapter mProgramsAdapter;

    static class SortProgramTitle implements Comparator<SequenceListModel> {
        SortProgramTitle() { }

        public int compare(SequenceListModel lhs, SequenceListModel rhs) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getSequenceTitle(), rhs.getSequenceTitle());
            return res != 0 ? res : lhs.getSequenceTitle().compareTo(rhs.getSequenceTitle());
        }
    }

    class ProgramClickListener implements AdapterView.OnItemClickListener {
        ProgramClickListener() { }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Utilities.addProgramToPlayer(mContext, mProgramsList.get(position).getIdString(), mProgramsList.get(position).getDatabaseId());
        }
    }

    @Override
    protected int initLayout() { return R.layout.fragment_program; }

    @Override
    protected void initComponents() {
        etSearch = mView.findViewById(R.id.menu_search);

        mDbHelper = new DbHelper(getActivity());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = etSearch.getText().toString().toLowerCase(Locale.getDefault());
                searchProgram(text);
            }
        });

        ListView sequenceList = mView.findViewById(R.id.sequence_listview);
        DbHelper db = new DbHelper(getContext());
        mProgramsList.clear();
        Cursor Sequences = db.getSequences();
        if (Sequences.getCount() != 0) {
            Sequences.moveToFirst();
            do {
                SequenceListModel seqModel = new SequenceListModel();
                seqModel.setSequenceTitle(Sequences.getString(Sequences.getColumnIndex("name" + MainActivity.sLocale)));
                seqModel.setNotes("");
                seqModel.setId(Sequences.getInt(Sequences.getColumnIndex("_id")));
                seqModel.setDbId(1);
                mProgramsList.add(seqModel);
            } while (Sequences.moveToNext());
        }
        Sequences.close();
        Cursor mySequences = db.getMySequences();
        if (mySequences.getCount() != 0) {
            mySequences.moveToFirst();
            do {
                SequenceListModel seqModel = new SequenceListModel();
                seqModel.setSequenceTitle(mySequences.getString(mySequences.getColumnIndex("name")));
                seqModel.setNotes("");
                seqModel.setId(mySequences.getInt(mySequences.getColumnIndex("_id")));
                seqModel.setDbId(2);
                mProgramsList.add(seqModel);
            } while (mySequences.moveToNext());
        }
        mySequences.close();
        Collections.sort(mProgramsList, new SortProgramTitle());

        Activity activity = getActivity();
        if (activity != null) { mProgramsAdapter = new ProgramsAdapter(activity, mProgramsList); }
        if (sequenceList != null) { sequenceList.setAdapter(mProgramsAdapter); }
        if (sequenceList != null) { sequenceList.setOnItemClickListener(new ProgramClickListener()); }
    }

    @Override
    protected void addListener() { }

    public void searchProgram(String s) {
        ArrayList<SequenceListModel> temp_Seq = new ArrayList<>();
        s = s.toLowerCase(Locale.getDefault());
        mProgramsList.clear();
        Cursor Sequences = mDbHelper.getSequences();
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
                mProgramsList.add(seqModel);
            } else if (notes.contains(s)) {
                seqModel.setSequenceTitle(OriginalName);
                seqModel.setNotes("See Notes");
                seqModel.setId(Sequences.getInt(Sequences.getColumnIndex("_id")));
                seqModel.setDbId(1);
                temp_Seq.add(seqModel);
            }
        } while (Sequences.moveToNext());
        Sequences.close();
        Cursor mySequences = mDbHelper.getMySequences();
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
                    mProgramsList.add(seqModel);
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
        Collections.sort(mProgramsList, new SortProgramTitle());
        if (!temp_Seq.isEmpty()) {
            Collections.sort(temp_Seq, new SortProgramTitle());
            mProgramsList.addAll(temp_Seq);
        }
        mProgramsAdapter.notifyDataSetChanged();
    }
}