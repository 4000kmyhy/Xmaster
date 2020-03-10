package com.xu.xmaster.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xu.xmaster.R;

import java.util.List;

public class TransRecordAdapter extends RecyclerView.Adapter<TransRecordAdapter.MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position, boolean isShowDel);
    }

    private List<String> list;
    private Context context;
    private boolean isShowDel = false;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public TransRecordAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trans_record, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.item_name.setText(list.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, holder, position, isShowDel);
                }
            }
        });

        holder.item_del.setVisibility(isShowDel ? View.VISIBLE : View.GONE);
    }

    public void setShowDel(boolean isShowDel) {
        this.isShowDel = isShowDel;
        notifyDataSetChanged();
    }

    public String getItemObject(int position) {
        return list.get(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView item_name;
        ImageView item_del;

        public MyViewHolder(View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_name);
            item_del = itemView.findViewById(R.id.item_del);
        }
    }
}
