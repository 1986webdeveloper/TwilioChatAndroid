package com.acquaint.twiliochat.listener;


import com.google.android.exoplayer2.C;
import com.twilio.chat.Channel;

public interface OnSelectListener {
    public void onSelect(int position, Channel channel);
    public void onDelete(int position, Channel channel);
}
