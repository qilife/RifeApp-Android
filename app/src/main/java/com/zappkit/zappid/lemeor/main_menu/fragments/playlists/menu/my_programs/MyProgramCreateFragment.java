package com.zappkit.zappid.lemeor.main_menu.fragments.playlists.menu.my_programs;

import android.app.Activity;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.zappkit.zappid.lemeor.database.DbHelper;
import com.zappkit.zappid.lemeor.main_menu.MainActivity;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.main_menu.fragments.programs.ProgramsAdapter;
import com.zappkit.zappid.lemeor.models.SequenceListModel;
import com.zappkit.zappid.lemeor.base.BaseFragment;
import com.zappkit.zappid.lemeor.base.BaseGroupFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MyProgramCreateFragment extends BaseFragment {
    private SequenceListModel mSequenceListModel;
    private EditText seqNotes, SeqName;
    private ListView listView;
    private ProgramsAdapter Seq_adapt;
    private DbHelper database;
    private Button mBtnCancel, mBtnNext;
    public ArrayList<SequenceListModel> mSequenceList = new ArrayList<>();

    public static MyProgramCreateFragment newInstance(SequenceListModel sequenceListModel) {
        MyProgramCreateFragment fragment = new MyProgramCreateFragment();
        fragment.mSequenceListModel = sequenceListModel;
        return fragment;
    }

    class UpdateList implements AdapterView.OnItemClickListener {
        UpdateList() { }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            SeqName.setText(mSequenceList.get(position).getSequenceTitle());
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    protected int initLayout() { return R.layout.fragment_add_new_my_program; }

    @Override
    protected void initComponents() {
        setTitleToActivity(getString(R.string.mysequences));
        if (mSequenceListModel == null) { mSequenceListModel = new SequenceListModel(); }
        mBtnCancel = mView.findViewById(R.id.add_frequency_cancel);
        mBtnNext = mView.findViewById(R.id.add_frequency_addbtn);
        database = new DbHelper(mContext);
        seqNotes = mView.findViewById(R.id.editnewseqnotes);
        SeqName = mView.findViewById(R.id.editnewseqname);
        listView = mView.findViewById(R.id.list_view);
        listView.setVisibility(View.GONE);
        Seq_adapt = new ProgramsAdapter((Activity) mContext, mSequenceList);
        if (listView != null) { listView.setAdapter(Seq_adapt); }
        SeqName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = SeqName.getText().toString().toLowerCase(Locale.getDefault());
                if (text.length() != 0) {
                    searchForSequence_(text);
                } else {
                    listView.setVisibility(View.GONE);
                }
            }
        });

        if (mSequenceListModel.getId() != -1) {
            SeqName.setText(mSequenceListModel.getSequenceTitle());
            seqNotes.setText(mSequenceListModel.getNotes());
        }
    }

    public void searchForSequence_(String s) {
        ArrayList<SequenceListModel> temp_Seq = new ArrayList<>();
        s = s.toLowerCase(Locale.getDefault());
        mSequenceList.clear();
        Cursor Sequences = database.getSequences();
        if (Sequences.getCount() != 0) {
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
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
                mSequenceList.add(seqModel);
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
                    mSequenceList.add(seqModel);
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
        Collections.sort(mSequenceList, new SortList());
        if (!temp_Seq.isEmpty()) {
            Collections.sort(temp_Seq, new SortList());
            mSequenceList.addAll(temp_Seq);
        }
        Seq_adapt.notifyDataSetChanged();
        listView.setOnItemClickListener(new UpdateList());
    }

    @Override
    protected void addListener() {
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackToPreFragment();
            }
        });
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (SeqName.getText().toString().equals("")) {
                    Toast.makeText(mContext, getResources().getString(R.string.mySequencesMissingName), Toast.LENGTH_LONG).show();
                    return;
                }
                SeqName.clearFocus();
                seqNotes.clearFocus();
                mSequenceListModel.setSequenceTitle(SeqName.getText().toString());
                mSequenceListModel.setNotes(seqNotes.getText().toString());
                if (getParentFragment() != null) {
                    ((BaseGroupFragment) getParentFragment()).addFragmentNotReloadContent(MyProgramSaveFragment.newInstance(mSequenceListModel));
                }
            }
        });
    }

    static class SortList implements Comparator<SequenceListModel> {
        SortList() { }

        public int compare(SequenceListModel lhs, SequenceListModel rhs) {
            int res = String.CASE_INSENSITIVE_ORDER.compare(lhs.getSequenceTitle(), rhs.getSequenceTitle());
            return res != 0 ? res : lhs.getSequenceTitle().compareTo(rhs.getSequenceTitle());
        }
    }
}
