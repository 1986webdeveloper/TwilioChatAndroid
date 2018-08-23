package com.acquaint.twiliochat.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acquaint.twiliochat.listener.OnSelectListener;
import com.acquaint.twiliochat.R;
import com.acquaint.twiliochat.adapter.ChannelAdapter;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.ChannelDescriptor;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Paginator;
import com.twilio.chat.StatusListener;

import java.util.ArrayList;

import static com.acquaint.twiliochat.Constants.SERVER_TOKEN_URL;

public class SelectChannelActivity extends AppCompatActivity implements View.OnClickListener,OnSelectListener {
    private ChatClient mChatClient;
    private Channel mGeneralChannel;
    private static String TAG = SelectChannelActivity.class.getSimpleName();
    private ArrayList<Channel> ar_channel=new ArrayList<>();
    private RecyclerView rv_channel;
    private ChannelAdapter mAdapter;
    private String mIdentity;
    private FloatingActionButton fab_add;
    private ProgressBar progress_channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_channel);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mIdentity=getIntent().getStringExtra("identity");
        initwidgets();
        initListeners();
        retrieveAccessTokenfromServer();
        setRvAdapter();

    }

    private void initListeners() {
        fab_add.setOnClickListener(this);
    }

    private void setRvAdapter() {
        mAdapter = new ChannelAdapter(ar_channel,this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rv_channel.setLayoutManager(mLayoutManager);
        rv_channel.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
        rv_channel.setAdapter(mAdapter);

    }

    private void initwidgets() {
        rv_channel=findViewById(R.id.rv_channel);
        fab_add=findViewById(R.id.fab_add);
        progress_channel=findViewById(R.id.progress_channel);
    }

    private void getChannel(){

        mChatClient.getChannels().getPublicChannelsList(new CallbackListener<Paginator<ChannelDescriptor>>() {
            @Override
            public void onSuccess(Paginator<ChannelDescriptor> channelPaginator) {
                for (ChannelDescriptor channel : channelPaginator.getItems()) {
                  channel.getChannel(new CallbackListener<Channel>() {
                        @Override
                        public void onSuccess(Channel channel) {
                            ar_channel.add(channel);
                            Log.d(TAG, "Channel named: " + channel.getUniqueName());
                            mAdapter.notifyDataSetChanged();
                            progress_channel.setVisibility(View.GONE);
                        }
                    });
                    Log.d(TAG, "Channel named: " + channel.getMessagesCount());
                }

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_add:
                openDialog();
                break;
        }

    }

    private void openDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        Window window = dialog.getWindow();
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Create Channel");
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        final EditText et_channel = dialog.findViewById(R.id.et_channel);

        Button bt_ok = dialog.findViewById(R.id.bt_ok);
        Button bt_cancel = dialog.findViewById(R.id.bt_cancel);
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String channel = et_channel.getText().toString();
                if(channel.length()>0){
                    createChannel(channel);
                    dialog.dismiss();
                }
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();

    }

    private void createChannel(String channel) {
        mChatClient.getChannels().channelBuilder().withUniqueName(channel).withType(Channel.ChannelType.PUBLIC).build(new CallbackListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                if (channel != null) {
                    mGeneralChannel = channel;
                    ar_channel.add(channel);
                    mAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Toast.makeText(SelectChannelActivity.this,""+errorInfo.getMessage(),Toast.LENGTH_LONG).show();
                Log.e(TAG,"Error creating channel: " + errorInfo.getMessage());
            }
        });
    }

    @Override
    public void onSelect(int position, Channel channel) {
        Intent intent = new Intent(SelectChannelActivity.this,MainActivity.class);
        intent.putExtra("channel",channel.getUniqueName());
        intent.putExtra("identity",mIdentity);
        startActivity(intent);
    }

    @Override
    public void onDelete(int position, Channel channel) {
        OpenConfirmDialog(position,channel);
    }

    private void OpenConfirmDialog(int position, final Channel mChannel) {
        final Dialog dialog = new Dialog(this,R.style.Dialog);
        Window window = dialog.getWindow();
        dialog.setContentView(R.layout.custom_dialog);
       // dialog.setTitle("Are you sure you want to delete the channel " +mChannel+ "?");
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        final TextView tv_confirm = dialog.findViewById(R.id.tv_confirm);
        tv_confirm.setVisibility(View.VISIBLE);
        tv_confirm.setTextColor(getResources().getColor(R.color.txtblack));
        tv_confirm.setText("Are you sure you want to delete the channel " +mChannel.getUniqueName()+ "?");
        final EditText et_channel = dialog.findViewById(R.id.et_channel);
        et_channel.setVisibility(View.GONE);

        Button bt_ok = dialog.findViewById(R.id.bt_ok);
        Button bt_cancel = dialog.findViewById(R.id.bt_cancel);
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                   deleteChannel(mChannel);
                    dialog.dismiss();

            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    private void deleteChannel(final Channel mChannel) {
        mChannel.destroy(new StatusListener() {
            @Override
            public void onSuccess() {
                ar_channel.remove(mChannel);
                mAdapter.notifyDataSetChanged();
                Toast.makeText(SelectChannelActivity.this,"Channel Deleted Successfully!",Toast.LENGTH_LONG).show();

            }
            @Override
            public void onError(ErrorInfo errorInfo) {
                Toast.makeText(SelectChannelActivity.this,""+errorInfo.getMessage(),Toast.LENGTH_LONG).show();
              //  Log.d(TAG, "Error deleting channel: " + errorInfo.getMessage());
            }
        });
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    private void retrieveAccessTokenfromServer() {
        progress_channel.setVisibility(View.VISIBLE);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        String tokenURL = SERVER_TOKEN_URL + "?device=" + deviceId + "&identity=" + mIdentity;

        Ion.with(this)
                .load(tokenURL)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.e("result","result"+result);
                        if (e == null) {
                            String accessToken = result.get("token").getAsString();

                            setTitle(mIdentity);

                            ChatClient.Properties.Builder builder = new ChatClient.Properties.Builder();
                            // builder.setSynchronizationStrategy(ChatClient.SynchronizationStrategy.ALL);
                            builder.setDeferCertificateTrustToPlatform(true);
                            ChatClient.Properties props = builder.createProperties();
                            ChatClient.create(SelectChannelActivity.this,accessToken,props,mChatClientCallback);

                        } else {
                            Log.e(TAG,e.getMessage(),e);
                            Toast.makeText(SelectChannelActivity.this,
                                    R.string.error_retrieving_access_token, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }
    private CallbackListener<ChatClient> mChatClientCallback =
            new CallbackListener<ChatClient>() {
                @Override
                public void onSuccess(ChatClient chatClient) {
                    mChatClient = chatClient;
                    getChannel();
                    Log.d(TAG, "Success creating Twilio Chat Client");
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    Log.e(TAG,"Error creating Twilio Chat Client: " + errorInfo.getMessage());
                }
            };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
