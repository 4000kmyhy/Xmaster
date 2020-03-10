package com.xu.xxplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xu.xxplayer.R;

import java.io.File;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    protected List<String> list;
    protected Context context;
    protected int select = -1;

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public VideoAdapter(Context context) {
        this.context = context;
    }

    public VideoAdapter(Context context, List<String> list) {
        this.list = list;
        this.context = context;
    }

    public void setSelectItem(int position) {
        if (select != position) {
            notifyItemChanged(select);
            notifyItemChanged(position);
            select = position;
        }
    }

    public int getSelect() {
        return select;
    }

    public int getSelectPosition(String url) {
        for (int i = 0; i < list.size(); i++) {
            if (TextUtils.equals(url, list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public VideoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.player_item_video, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideoAdapter.MyViewHolder holder, final int position) {
        File file = new File(list.get(position));
        holder.item_title.setText(file.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(position);
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, holder, position);
                }
            }
        });

        if (select == position) {
            holder.item_title.setTextColor(Color.parseColor("#00A1D6"));
            holder.item_title.setSelected(true);
        } else {
            holder.item_title.setTextColor(Color.parseColor("#ffffff"));
            holder.item_title.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView item_title;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_title = itemView.findViewById(R.id.item_title);
        }
    }
}
