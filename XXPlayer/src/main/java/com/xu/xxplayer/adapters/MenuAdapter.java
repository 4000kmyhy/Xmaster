package com.xu.xxplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xu.xxplayer.R;
import com.xu.xxplayer.players.BasePlayerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MyViewHolder> {


    private String[] speeds = {"0.5", "0.75", "1.0", "1.25", "1.5", "2.0"};
    private String[] scales = {"适应", "拉伸", "填充", "16:9", "4:3"};
    private String[] modes = {"播完暂停", "单集循环", "自动连播", "列表循环"};

    protected List<String> list;
    protected Context context;
    protected int select;

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public MenuAdapter(Context context) {
        this.context = context;
    }

    public MenuAdapter(Context context, List<String> list) {
        this.list = list;
        this.context = context;
    }

    public MenuAdapter(Context context, String type) {
        this.context = context;
        this.list = new ArrayList<>();
        switch (type) {
            case "speed":
                list.addAll(Arrays.asList(speeds));
                break;
            case "scale":
                list.addAll(Arrays.asList(scales));
                break;
            case "mode":
                list.addAll(Arrays.asList(modes));
                break;
        }
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

    public int getSpeedPosition(String speed) {
        int position = 2;
        for (int i = 0; i < speeds.length; i++) {
            if (TextUtils.equals(speed, speeds[i])) {
                position = i;
            }
        }
        return position;
    }

    public float getSpeed(int position) {
        float speed = 1.0f;
        switch (list.get(position)) {
            case "0.5":
                speed = 0.5f;
                break;
            case "0.75":
                speed = 0.75f;
                break;
            case "1.0":
                speed = 1.0f;
                break;
            case "1.25":
                speed = 1.25f;
                break;
            case "1.5":
                speed = 1.5f;
                break;
            case "2.0":
                speed = 2.0f;
                break;
        }
        return speed;
    }

    public int getScale(int position) {
        int scale = 0;
        switch (list.get(position)) {
            case "适应":
                scale = BasePlayerView.SCREEN_SCALE_ADAPT;
                break;
            case "拉伸":
                scale = BasePlayerView.SCREEN_SCALE_STRETCH;
                break;
            case "填充":
                scale = BasePlayerView.SCREEN_SCALE_FILL;
                break;
            case "16:9":
                scale = BasePlayerView.SCREEN_SCALE_16_9;
                break;
            case "4:3":
                scale = BasePlayerView.SCREEN_SCALE_4_3;
                break;
        }
        return scale;
    }

    public int getMode(int position) {
        int mode = 0;
        switch (list.get(position)) {
            case "播完暂停":
                mode = BasePlayerView.PLAY_MODE_NORMAL;
                break;
            case "单集循环":
                mode = BasePlayerView.PLAY_MODE_LOOP;
                break;
            case "自动连播":
                mode = BasePlayerView.PLAY_MODE_LIST;
                break;
            case "列表循环":
                mode = BasePlayerView.PLAY_MODE_LIST_LOOP;
                break;
        }
        return mode;
    }

    @Override
    public MenuAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.player_item_menu, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MenuAdapter.MyViewHolder holder, final int position) {
        holder.item_btn.setText(list.get(position));

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
            holder.item_btn.setTextColor(Color.parseColor("#00A1D6"));
        } else {
            holder.item_btn.setTextColor(Color.parseColor("#ffffff"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(context, getItemCount()));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        Button item_btn;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_btn = itemView.findViewById(R.id.item_btn);
        }
    }
}
