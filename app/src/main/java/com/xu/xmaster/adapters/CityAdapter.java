package com.xu.xmaster.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xu.xmaster.R;

import java.util.ArrayList;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.basic.Basic;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.MyViewHolder> {

    private static final String TAG = "CityAdapter";
    private static final int VIEWTYPE_NORMAL = 0;
    private static final int VIEWTYPE_HEADER = 1;

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position);

        void directSearch();
    }

    private List<Basic> list;
    private Context context;
    private String name = "";

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public CityAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
    }

    public void setList(List<Basic> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public String getLocation(int position) {
        return list.get(position).getLocation();
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0) {
            return VIEWTYPE_NORMAL;
        } else {
            return VIEWTYPE_HEADER;
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEWTYPE_NORMAL) {
            view = LayoutInflater.from(context).inflate(R.layout.item_wea_city, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_direct_search, parent, false);
        }
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (position > 0) {
            Basic basic = list.get(position - 1);

            holder.item_location.setText(basic.getLocation());
            holder.item_parent_city.setText("所在城市：" + basic.getParent_city());
            holder.item_admin_area.setText("所在省份：" + basic.getAdmin_area());
            holder.item_cnty.setText("所在国家：" + basic.getCnty());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, holder, position - 1);
                    }
                }
            });
        } else {
            holder.item_name.setText(name);
            holder.item_name.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            holder.item_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.directSearch();
                    }
                }
            });
        }
    }

    public void setName(String name) {
        this.name = name;
        notifyItemChanged(0);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
    }

    @Override
    public int getItemCount() {
        return list.size() + 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView item_location, item_parent_city, item_admin_area, item_cnty, item_name;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_location = itemView.findViewById(R.id.item_location);
            item_parent_city = itemView.findViewById(R.id.item_parent_city);
            item_admin_area = itemView.findViewById(R.id.item_admin_area);
            item_cnty = itemView.findViewById(R.id.item_cnty);
            item_name = itemView.findViewById(R.id.item_name);
        }
    }
}
