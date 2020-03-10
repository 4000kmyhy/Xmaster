package com.xu.xmaster.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xu.xmaster.R;
import com.xu.xmaster.beans.NewsBean;
import com.xu.xmaster.utils.PixelUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);
    }

    private List<NewsBean> list;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public NewsAdapter(Context context, List<NewsBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        NewsBean newsBean = list.get(position);

        if (TextUtils.isEmpty(newsBean.getImgsrc())) {//没有图片
            holder.item_img.setVisibility(View.GONE);
            holder.item_title.setLines(1);
        } else {
            holder.item_img.setVisibility(View.VISIBLE);
            holder.item_title.setLines(2);
            Glide.with(context)
                    .load(newsBean.getImgsrc())
                    .override(PixelUtils.dp2px(context, 120),
                            PixelUtils.dp2px(context, 75))
                    .placeholder(R.drawable.pic_splash)
                    .into(holder.item_img);
        }
        holder.item_title.setText(newsBean.getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = "";
        try {
            Date newsDate = dateFormat.parse(newsBean.getTime());
            dateFormat.applyPattern("yyyyMMdd");
            String newsTime = dateFormat.format(newsDate);
            String nowTime = dateFormat.format(new Date());
            if (TextUtils.equals(newsTime, nowTime)) {
                dateFormat.applyPattern("今天 HH:mm");
                str = dateFormat.format(newsDate);
            } else {
                dateFormat.applyPattern("MM-dd HH:mm");
                str = dateFormat.format(newsDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        str = newsBean.getSource() + "&#8194;" + str;
        holder.item_source_time.setText(Html.fromHtml(str));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, holder, position);
                }
            }
        });
    }

    public String getItemUrl(int position) {
        return list.get(position).getUrl();
    }

    public String getItemTitle(int position) {
        return list.get(position).getTitle();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView item_title, item_source_time;
        private ImageView item_img;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_title = itemView.findViewById(R.id.item_title);
            item_source_time = itemView.findViewById(R.id.item_source_time);
            item_img = itemView.findViewById(R.id.item_img);
        }
    }
}
