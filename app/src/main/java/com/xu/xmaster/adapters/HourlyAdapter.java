package com.xu.xmaster.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xu.xmaster.R;
import com.xu.xmaster.utils.GlideUtils;
import com.xu.xmaster.utils.WeatherManager;

import java.util.ArrayList;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.weather.hourly.HourlyBase;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    private List<HourlyBase> list;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public HourlyAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    public void setList(List<HourlyBase> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wea_hourly, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        HourlyBase hourlyBase = list.get(position);

        if (WeatherManager.isNight(hourlyBase.getTime()) &&
                WeatherManager.hasNight(hourlyBase.getCond_code())) {//当前时间是晚上且存在晚上icon
            GlideUtils.loadImage(context,
                    "file:///android_asset/" + hourlyBase.getCond_code() + "n.png",
                    holder.item_img);
        } else {
            GlideUtils.loadImage(context,
                    "file:///android_asset/" + hourlyBase.getCond_code() + ".png",
                    holder.item_img);
        }
        holder.item_tmp.setText(hourlyBase.getTmp() + "°C");
        String time = hourlyBase.getTime();
        if (time.split(" ").length > 1) {
            time = time.split(" ")[1];
        }
        holder.item_time.setText(time);
        holder.item_tmp.setTextColor(Color.parseColor("#ffffff"));
        holder.item_time.setTextColor(Color.parseColor("#ffffff"));
        holder.item_img.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ffffff")));

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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView item_time, item_tmp;
        private ImageView item_img;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_time = itemView.findViewById(R.id.item_time);
            item_tmp = itemView.findViewById(R.id.item_tmp);
            item_img = itemView.findViewById(R.id.item_img);
        }
    }
}