package com.xu.xmaster.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.xu.xmaster.R;
import com.xu.xmaster.beans.MusicListBean;
import com.xu.xmaster.utils.GlideUtils;
import com.xu.xmaster.utils.PixelUtils;

import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    private List<MusicListBean> list;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public MusicListAdapter(Context context, List<MusicListBean> list) {
        this.context = context;
        this.list = list;
    }

    public MusicListBean getItemObject(int position) {
        return list.get(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_musiclist, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        MusicListBean songListBean = list.get(position);

        holder.item_listname.setText(songListBean.getListName());

        if (songListBean.getMusicList().size() > 0) {
            holder.item_song1.setText("1. " + songListBean.getMusicList().get(0).getSongname() + " - " +
                    songListBean.getMusicList().get(0).getSinger());
        } else {
            holder.item_song1.setText("1.");
        }
        if (songListBean.getMusicList().size() > 1) {
            holder.item_song2.setText("2. " + songListBean.getMusicList().get(1).getSongname() + " - " +
                    songListBean.getMusicList().get(1).getSinger());
        } else {
            holder.item_song2.setText("2.");
        }
        if (songListBean.getMusicList().size() > 2) {
            holder.item_song3.setText("3. " + songListBean.getMusicList().get(2).getSongname() + " - " +
                    songListBean.getMusicList().get(2).getSinger());
        } else {
            holder.item_song3.setText("3.");
        }

        GlideUtils.loadImage(context, songListBean.getPicUrl(), R.drawable.pic_splash, holder.item_pic);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, holder, position);
                }
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new SpaceItemDecoration(PixelUtils.dp2px(context, 10)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView item_listname, item_song1, item_song2, item_song3;
        private ImageView item_pic;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_listname = itemView.findViewById(R.id.item_listname);
            item_song1 = itemView.findViewById(R.id.item_song1);
            item_song2 = itemView.findViewById(R.id.item_song2);
            item_song3 = itemView.findViewById(R.id.item_song3);
            item_pic = itemView.findViewById(R.id.item_pic);
        }
    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildPosition(view) == 0)
                outRect.top = space;
        }
    }
}