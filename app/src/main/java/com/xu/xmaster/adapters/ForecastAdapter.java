package com.xu.xmaster.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xu.xmaster.R;
import com.xu.xmaster.utils.GlideUtils;
import com.xu.xmaster.utils.WeatherManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    private List<ForecastBase> list;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public ForecastAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    public void setList(List<ForecastBase> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public List<ForecastBase> getList() {
        return list;
    }

    public ForecastBase getItem(int position) {
        return list.get(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wea_forecast, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        ForecastBase forecastBase = list.get(position);

        String time = forecastBase.getDate();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(time);
            if (position == 0) {
                dateFormat.applyPattern("MM月dd日 今天");
            } else {
                dateFormat.applyPattern("MM月dd日 E");
            }
            time = dateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.item_time.setText(time);

        GlideUtils.loadImage(context,
                "file:///android_asset/" + forecastBase.getCond_code_d() + ".png",
                holder.item_img_d);
        if (WeatherManager.hasNight(forecastBase.getCond_code_n())) {//存在晚上icon
            GlideUtils.loadImage(context,
                    "file:///android_asset/" + forecastBase.getCond_code_n() + "n.png",
                    holder.item_img_n);
        } else {
            GlideUtils.loadImage(context,
                    "file:///android_asset/" + forecastBase.getCond_code_n() + ".png",
                    holder.item_img_n);
        }

        String tmp_max = forecastBase.getTmp_max() + "°C";
        String tmp_min = forecastBase.getTmp_min() + "°C";
        String tmpStr = "<font color='#ffffff'>" + tmp_max +
                "</font><font color='#e0e0e0'> / " + tmp_min + "</font>";
        holder.item_tmp.setText(Html.fromHtml(tmpStr));

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
        recyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView item_time, item_tmp;
        private ImageView item_img_d, item_img_n;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_time = itemView.findViewById(R.id.item_time);
            item_tmp = itemView.findViewById(R.id.item_tmp);
            item_img_d = itemView.findViewById(R.id.item_img_d);
            item_img_n = itemView.findViewById(R.id.item_img_n);
        }
    }
}
