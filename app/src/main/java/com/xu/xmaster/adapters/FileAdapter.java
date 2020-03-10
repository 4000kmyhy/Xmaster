package com.xu.xmaster.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xu.xmaster.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    private List<String> list;
    private Context context;
    private int lastPosition = -1;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public FileAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        String url = list.get(position);
        if (url.endsWith(".gif")) {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .placeholder(R.drawable.pic_splash)
                    .into(holder.item_img);
        } else {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .placeholder(R.drawable.pic_splash)
                    .into(holder.item_img);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, holder, position);
                }
            }
        });

        addAnimation(position, holder);
    }

    private void addAnimation(int position, MyViewHolder holder) {
        Animation animation = AnimationUtils.loadAnimation(context,
                position > lastPosition ? R.anim.item_slide_down : R.anim.item_slide_up);
        holder.itemView.setAnimation(animation);
        lastPosition = position;
    }

    public String getItemObject(int position) {
        return list.get(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView item_img;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_img = itemView.findViewById(R.id.item_img);
        }
    }
}
