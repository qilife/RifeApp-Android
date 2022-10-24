package com.zappkit.zappid;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private Context mContext;
    private List<MenuModel> mMenus;
    private IOnClickItemListener mIOnClickItemListener;

    public MenuAdapter(Context mContext, List<MenuModel> mMenus) {
        this.mContext = mContext;
        this.mMenus = mMenus;
    }

    public void setIOnClickItemListener(IOnClickItemListener listener) {
        mIOnClickItemListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuModel menuModel = mMenus.get(position);
        holder.mTvNameMenu.setText(menuModel.getmNameMenu());
    }

    @Override
    public int getItemCount() {
        return mMenus.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvNameMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvNameMenu = itemView.findViewById(R.id.tv_name_menu);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mIOnClickItemListener != null) {
                        mIOnClickItemListener.onItemClick(mMenus.get(getLayoutPosition()).getType());
                    }
                }
            });
        }
    }

    public interface IOnClickItemListener {
        void onItemClick(MenuModel.MENU_TYPE menuType);
    }
}
