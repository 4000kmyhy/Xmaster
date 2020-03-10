package com.xu.xmaster.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xu.xmaster.R;
import com.xu.xmaster.beans.FileBean;
import com.xu.xmaster.utils.PixelUtils;
import com.xu.xxplayer.utils.XXPlayerUtil;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    private static final String TAG = "VideoAdapter";

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    private List<FileBean> list;
    private Context context;
    private int select = -1;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public VideoAdapter(Context context, List<FileBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        FileBean fileBean = list.get(position);
        Glide.with(context)
                .load(fileBean.getUrl())
                .override(PixelUtils.dp2px(context, 120),
                        PixelUtils.dp2px(context, 75))
                .placeholder(R.drawable.pic_splash)
                .into(holder.item_img);
        holder.item_title.setText(fileBean.getName());
        holder.item_time.setText(XXPlayerUtil.stringForTime(fileBean.getDuration()));

        if (select == position) {
            holder.item_title.setTextColor(context.getResources().getColor(R.color.colorBlue));
            holder.item_time.setTextColor(context.getResources().getColor(R.color.colorBlue));
        } else {
            holder.item_title.setTextColor(context.getResources().getColor(R.color.colorGray40));
            holder.item_time.setTextColor(context.getResources().getColor(R.color.colorGray80));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, holder, position);
                }
            }
        });
    }

    public void setSelect(int position) {
        if (select != position) {
            notifyItemChanged(select);
            notifyItemChanged(position);
            select = position;
        }
    }

    public int getSelect() {
        return select;
    }

    public String getUrl(int position) {
        return list.get(position).getUrl();
    }

    public int getPosition(String url) {
        for (int i = 0; i < list.size(); i++) {
            if (TextUtils.equals(url, list.get(i).getUrl())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView item_title, item_time;
        private ImageView item_img;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_title = itemView.findViewById(R.id.item_title);
            item_time = itemView.findViewById(R.id.item_time);
            item_img = itemView.findViewById(R.id.item_img);
        }
    }
}
