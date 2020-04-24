package com.szd.messagebubbleview;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.szd.messagebubble.MessageBubbleView;

/**
 * Created by szd on 2017/3/31.
 */

public class MyAdapter extends RecyclerView.Adapter {
    String[] data;
    Context context;

    MyViewHolder item;

    OnItemClickListener l;


    public MyAdapter(Context context, String[] data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View holder = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        item = new MyViewHolder(holder);
        return item;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ((MyViewHolder) holder).msg.setNumber(data[position]);
    }


    @Override
    public int getItemCount() {
        return data.length;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        MessageBubbleView msg;

        public MyViewHolder(View itemView) {
            super(itemView);
            msg = (MessageBubbleView) itemView.findViewById(R.id.message);

        }
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.l = l;
    }


    interface OnItemClickListener {
        void OnItemClick(int position);
    }
}
