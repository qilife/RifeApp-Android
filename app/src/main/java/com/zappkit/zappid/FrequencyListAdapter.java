package com.zappkit.zappid;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FrequencyListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    private Activity activity;
    private ArrayList data;
    FrequencyListModel tempValues = null;
    private IOnClickItemListener mListener;

    public FrequencyListAdapter(Activity a, ArrayList d) {
        this.activity = a;
        this.data = d;
        inflater = (LayoutInflater) this.activity.getSystemService("layout_inflater");
    }

    public void setIOnClickItemListener(IOnClickItemListener listener) {
        this.mListener = listener;
    }

    public int getCount() {
        return this.data.size();
    }

    public Object getItem(int position) {
        return Integer.valueOf(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        View vi = convertView;
        this.tempValues = null;
        this.tempValues = (FrequencyListModel) this.data.get(position);
        // vi = inflater.inflate(R.layout.list_item, null);
        vi = inflater.inflate(R.layout.list_item_cp, null);

        final TextView tvLabel = vi.findViewById(R.id.label);
        tvLabel.setText(this.tempValues.getFrequencyString() + " Hz");

        if (mListener != null) {
            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvLabel.setTextColor(parent.getContext().getResources().getColor(R.color.color_text_disable));
                    mListener.onClick(position);
                }
            });
        }
        return vi;
    }

    public interface IOnClickItemListener {
        void onClick(int position);
    }
}
