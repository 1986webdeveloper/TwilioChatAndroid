package com.acquaint.twiliochat.adapter;

import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.acquaint.twiliochat.listener.OnSelectListener;
import com.acquaint.twiliochat.R;
import com.twilio.chat.Channel;

import java.util.ArrayList;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.MyViewHolder> {
    ArrayList<Channel> ar_channel = new ArrayList<>();
    OnSelectListener onSelectListener;

      public ChannelAdapter(ArrayList<Channel> ar_channel, OnSelectListener onSelectListener)
      {
          this.onSelectListener=onSelectListener;
          this.ar_channel=ar_channel;
      }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final Channel channel=ar_channel.get(position);
        String text = "<font color=#E30047>"+channel.getUniqueName()+"</font> <font color=#808080> created by </font> <font color=#E30047>"+channel.getCreatedBy()+"</font>";
        holder.tv_name.setText(Html.fromHtml(text));
        holder.tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectListener.onSelect(position,channel);
            }
        });
        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelectListener.onDelete(position,channel);
            }
        });

    }

    @Override
    public int getItemCount() {
        return ar_channel.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_name;
        public ImageView iv_delete;

        public MyViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            iv_delete=view.findViewById(R.id.iv_delete);
        }
    }

}
