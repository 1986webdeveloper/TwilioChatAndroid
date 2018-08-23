package com.acquaint.twiliochat.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.acquaint.twiliochat.R;
import com.github.barteksc.pdfviewer.PDFView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.ChannelDescriptor;
import com.twilio.chat.ChannelListener;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Member;
import com.twilio.chat.Message;
import com.twilio.chat.Paginator;
import com.twilio.chat.ProgressListener;
import com.twilio.chat.StatusListener;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private static final int CHOOSE_FILE_REQUESTCODE=1;

    /*
       Change this URL to match the token URL for your Twilio Function
    */
    final static String SERVER_TOKEN_URL = "https://regalia-porpoise-7200.twil.io/chat-token";
    //final static String SERVER_TOKEN_URL = "https://raspberry-turkey-5474.twil.io/chat-token";

    static String DEFAULT_CHANNEL_NAME = "general";
    final static String TAG = "TwilioChat";

    // Update this identity for each individual user, for instance after they login
    private String mIdentity = "acquaint";
    private Message message;
    private RecyclerView mMessagesRecyclerView;
    private MessagesAdapter mMessagesAdapter;
    private ArrayList<Message> mMessages = new ArrayList<>();
    private FloatingActionButton ib_attachment;

    private EditText mWriteMessageEditText;
    private FloatingActionButton mSendChatMessageButton;

    private ChatClient mChatClient;
    private String type;

    private Channel mGeneralChannel;
    InputStream inputStream;
    ImageView iv_preview;
    Button bt_cross;
    RelativeLayout rl_preview,rl_videomessage;
    VideoView vv_preview;
    PDFView pdfView_preview;
    ProgressBar progress_media,progress_msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String identity=getIntent().getStringExtra("identity");
        String channel =getIntent().getStringExtra("channel");
        if(identity!=null){
            mIdentity=identity;
        }
        if(channel!=null){
            DEFAULT_CHANNEL_NAME=channel;

        }
        progress_msg=findViewById(R.id.progress_msg);
        progress_media=findViewById(R.id.progress_media);
        pdfView_preview=findViewById(R.id.pdfView);
        rl_videomessage=findViewById(R.id.rl_videomessage);
        rl_preview=findViewById(R.id.rl_preview);
        iv_preview=findViewById(R.id.iv_preview);
        bt_cross=findViewById(R.id.bt_cross);
        vv_preview=findViewById(R.id.iv_preview_video);
        mMessagesRecyclerView =findViewById(R.id.messagesRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // for a chat app, show latest at the bottom
        layoutManager.setStackFromEnd(true);
        mMessagesRecyclerView.setLayoutManager(layoutManager);

        mMessagesAdapter = new MessagesAdapter();
        mMessagesRecyclerView.setAdapter(mMessagesAdapter);

        mWriteMessageEditText = (EditText) findViewById(R.id.writeMessageEditText);

        ib_attachment=findViewById(R.id.ib_attachment);

        mSendChatMessageButton =  findViewById(R.id.sendChatMessageButton);
        ib_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendChatMessageButton.setEnabled(true);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                Intent i = Intent.createChooser(intent, "File");
                startActivityForResult(i, CHOOSE_FILE_REQUESTCODE);
            }
        });

        mSendChatMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGeneralChannel != null) {
                    String messageBody = mWriteMessageEditText.getText().toString();
                    Boolean check=false;
                    try {
                        if (inputStream.available() != 0) {
                            rl_preview.setVisibility(View.GONE);
                            mWriteMessageEditText.setVisibility(View.VISIBLE);
                            check = true;
                        }
                        else {
                            check=false;
                        }
                    }
                    catch (Exception e){

                    }

                    if(check){
                        if(type!=null && type.toLowerCase().contains("video")){

                            mGeneralChannel.getMessages().sendMessage(
                                    Message.options()
                                            .withMedia(inputStream, "video/*")
                                            .withMediaFileName("file.mp4")
                                            .withMediaProgressListener(new ProgressListener() {
                                                @Override
                                                public void onStarted() {
                                                    //  Toast.makeText(getApplicationContext(),"Upload started",Toast.LENGTH_LONG).show();
                                                   // progress_media.setVisibility(View.VISIBLE);

                                                }

                                                @Override
                                                public void onProgress(long bytes) {
                                                    //    Toast.makeText(getApplicationContext(),"Uploaded"+bytes,Toast.LENGTH_LONG).show();
                                                   // progress_media.setProgress((int)bytes);
                                                }

                                                @Override
                                                public void onCompleted(String mediaSid) {
                                                    //  Toast.makeText(getApplicationContext(),"Completed mediasid"+mediaSid,Toast.LENGTH_LONG).show();
                                                   // progress_media.setVisibility(View.GONE);
                                                }
                                            }),
                                    new CallbackListener<Message>() {
                                        @Override
                                        public void onSuccess(Message msg) {
                                            mWriteMessageEditText.setText("");
                                            mSendChatMessageButton.setEnabled(true);
                                        }

                                        @Override
                                        public void onError(ErrorInfo error) {
                                            Log.e("Error","Uploading Media"+error);

                                        }
                                    });
                        }
                        else if(type!=null && type.toLowerCase().contains("image")){
                            mGeneralChannel.getMessages().sendMessage(
                                    Message.options()
                                            .withMedia(inputStream, "image/*")
                                            .withMediaFileName("file.png")
                                            .withMediaProgressListener(new ProgressListener() {
                                                @Override
                                                public void onStarted() {
                                                    //  Toast.makeText(getApplicationContext(),"Upload started",Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onProgress(long bytes) {
                                                    //    Toast.makeText(getApplicationContext(),"Uploaded"+bytes,Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onCompleted(String mediaSid) {
                                                    //  Toast.makeText(getApplicationContext(),"Completed mediasid"+mediaSid,Toast.LENGTH_LONG).show();
                                                }
                                            }),
                                    new CallbackListener<Message>() {
                                        @Override
                                        public void onSuccess(Message msg) {
                                            mWriteMessageEditText.setText("");
                                            mSendChatMessageButton.setEnabled(true);
                                        }

                                        @Override
                                        public void onError(ErrorInfo error) {
                                            Log.e("Error","Uploading Media"+error);

                                        }
                                    });
                        }
                        else if(type!=null && type.toLowerCase().contains("pdf")){
                            mGeneralChannel.getMessages().sendMessage(
                                    Message.options()
                                            .withMedia(inputStream, "application/pdf")
                                            .withMediaFileName("file.pdf")
                                            .withMediaProgressListener(new ProgressListener() {
                                                @Override
                                                public void onStarted() {
                                                    //  Toast.makeText(getApplicationContext(),"Upload started",Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onProgress(long bytes) {
                                                    //    Toast.makeText(getApplicationContext(),"Uploaded"+bytes,Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onCompleted(String mediaSid) {
                                                    //  Toast.makeText(getApplicationContext(),"Completed mediasid"+mediaSid,Toast.LENGTH_LONG).show();
                                                }
                                            }),
                                    new CallbackListener<Message>() {
                                        @Override
                                        public void onSuccess(Message msg) {
                                            mWriteMessageEditText.setText("");
                                            mSendChatMessageButton.setEnabled(true);
                                        }

                                        @Override
                                        public void onError(ErrorInfo error) {
                                            Log.e("Error","Uploading Media"+error);

                                        }
                                    });
                        }

                        else {
                            //Type Unknown
                            Log.e("Type","Unknown");
                        }





                    }


                   else if(messageBody.length()!=0) {


                        Message.Options options = Message.options().withBody(messageBody);

                        mGeneralChannel.getMessages().sendMessage(options, new CallbackListener<Message>() {

                            @Override
                            public void onSuccess(Message message) {
                                mWriteMessageEditText.setText("");
                                mSendChatMessageButton.setEnabled(true);
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // need to modify user interface elements on the UI thread
                                        mWriteMessageEditText.setText("");
                                    }
                                });

                            }

                            @Override
                            public void onError(ErrorInfo errorInfo) {
                                Log.e(TAG, "Error sending message: " + errorInfo.getMessage());
                            }
                        });
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Please Enter Message",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        retrieveAccessTokenfromServer();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK && requestCode==CHOOSE_FILE_REQUESTCODE){
            Uri Selected_Image_Uri = data.getData();
            type = Selected_Image_Uri.toString().substring(Selected_Image_Uri.toString().lastIndexOf("."));
            Log.e("Type","SelectedFile"+type);
            rl_preview.setVisibility(View.VISIBLE);
            mWriteMessageEditText.setVisibility(View.GONE);

            if(type.toLowerCase().contains("image")) {
                iv_preview.setVisibility(View.VISIBLE);
                vv_preview.setVisibility(View.GONE);
                pdfView_preview.setVisibility(View.GONE);


                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Selected_Image_Uri);
                    iv_preview.setImageBitmap(bitmap);
                    bt_cross.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rl_preview.setVisibility(View.GONE);
                            mWriteMessageEditText.setVisibility(View.VISIBLE);
                            inputStream = null;
                        }
                    });
                } catch (Exception e) {

                }
            }
           else if(type.toLowerCase().contains("video")){
                iv_preview.setVisibility(View.GONE);
                vv_preview.setVisibility(View.VISIBLE);
                pdfView_preview.setVisibility(View.GONE);
                vv_preview.setVideoURI(Selected_Image_Uri);
                vv_preview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        vv_preview.start();
                        vv_preview.pause();
                    }
                });


                bt_cross.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rl_preview.setVisibility(View.GONE);
                        mWriteMessageEditText.setVisibility(View.VISIBLE);
                        inputStream = null;
                    }
                });


            }
           else if(type.toLowerCase().contains("pdf")){
                iv_preview.setVisibility(View.GONE);
                vv_preview.setVisibility(View.GONE);
                pdfView_preview.setVisibility(View.VISIBLE);
                Log.e("PDF","URi"+Selected_Image_Uri);

               pdfView_preview.fromUri(Selected_Image_Uri)
               .enableSwipe(true) // allows to block changing pages using swipe
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .defaultPage(0)
                        // allows to draw something on the current page, usually visible in the middle of the screen

                        .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                        .password(null)
                        .scrollHandle(null)
                        .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                        // spacing between pages in dp. To define spacing color, set view background
                        .spacing(0)
                        .load();

                vv_preview.start();
                bt_cross.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rl_preview.setVisibility(View.GONE);
                        mWriteMessageEditText.setVisibility(View.VISIBLE);
                        inputStream = null;
                    }
                });


            }
            else {
                iv_preview.setVisibility(View.GONE);
                vv_preview.setVisibility(View.GONE);
                pdfView_preview.setVisibility(View.GONE);
                rl_preview.setVisibility(View.GONE);
                mWriteMessageEditText.setVisibility(View.VISIBLE);
               Toast.makeText(MainActivity.this,"Invalid File",Toast.LENGTH_LONG).show();
               inputStream=null;
            }




            try {
                inputStream= getContentResolver().openInputStream(Selected_Image_Uri);
                mSendChatMessageButton.setEnabled(true);
               // sendAttchment(inputStream);
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Exception InputStream",Toast.LENGTH_LONG);
            }


        }
    }


    private void retrieveAccessTokenfromServer() {
        progress_msg.setVisibility(View.VISIBLE);
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

                            setTitle(DEFAULT_CHANNEL_NAME);

                            ChatClient.Properties.Builder builder = new ChatClient.Properties.Builder();
                            builder.setDeferCertificateTrustToPlatform(true);
                            ChatClient.Properties props = builder.createProperties();
                            ChatClient.create(MainActivity.this,accessToken,props,mChatClientCallback);

                        } else {
                            Log.e(TAG,e.getMessage(),e);
                            Toast.makeText(MainActivity.this,
                                    R.string.error_retrieving_access_token, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void loadChannels() {


      mChatClient.getChannels().getChannel(DEFAULT_CHANNEL_NAME, new CallbackListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {

                if (channel != null ) {

                    getChannel(channel);


               }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG,"Error retrieving channel: " + errorInfo.getMessage());
                mChatClient.getChannels().channelBuilder().withUniqueName(DEFAULT_CHANNEL_NAME).withType(Channel.ChannelType.PUBLIC).build(new CallbackListener<Channel>() {
                    @Override
                    public void onSuccess(Channel channel) {
                        if (channel != null) {
                           mGeneralChannel = channel;

                            joinChannel(mGeneralChannel);


                        }
                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        Log.e(TAG,"Error creating channel: " + errorInfo.getMessage());
                    }
                });


            }
        });

    }



    private void loadMessages(Channel channel){
        mGeneralChannel=channel;

try{


    channel.getMessages().getLastMessages(50, new CallbackListener<List<Message>>() {

        @Override
        public void onSuccess(List<Message> messages) {
            for (Message message : messages) {
                mMessages.add(message);
                mMessagesAdapter.notifyDataSetChanged();

            }
            progress_msg.setVisibility(View.GONE);

        }

        @Override
        public void onError(ErrorInfo errorInfo) {
            Log.e("LoadMessage", "Error" + errorInfo);
          Toast.makeText(MainActivity.this,""+errorInfo,Toast.LENGTH_LONG).show();
        }
    });
}catch (NullPointerException e){

    loadMessages(mGeneralChannel);

}

    }
    private void getChannel(final Channel mychannel){
        mChatClient.getChannels().getPublicChannelsList(new CallbackListener<Paginator<ChannelDescriptor>>() {
            @Override
            public void onSuccess(Paginator<ChannelDescriptor> channelPaginator) {
                for (ChannelDescriptor channel : channelPaginator.getItems()) {
                    Log.d(TAG, "Channel named: " + channel.getMessagesCount());
                    if(channel.getUniqueName().equals(mychannel.getUniqueName())){
                        channel.getChannel(new CallbackListener<Channel>() {
                            @Override
                            public void onSuccess(Channel channel) {
                                Log.d(TAG, "Channel Status: " + channel.getMessages());

                                mGeneralChannel=channel;
                                joinChannel(channel);
                                loadMessages(mGeneralChannel);
                                mGeneralChannel.addListener(mDefaultChannelListener);
                            }

                            @Override
                            public void onError(ErrorInfo errorInfo) {
                                Log.e("OnError","getChannelMessage"+errorInfo);
                            }
                        });

                    }
                }
            }
        });
    }

    private void joinChannel(final Channel channel) {



        if(channel.getStatus() != Channel.ChannelStatus.JOINED){
           channel.join(new StatusListener() {
                @Override
                public void onSuccess() {
                    mGeneralChannel = channel;
                    Log.d(TAG, "Joined default channel");
                    loadMessages(mGeneralChannel);

                    mGeneralChannel.addListener(mDefaultChannelListener);
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    Log.e(TAG,"Error joining channel: " + errorInfo.getMessage());
                }
            });
        }
        else {
            mGeneralChannel = channel;
            Log.d(TAG, "Joined default channel");
            loadMessages(mGeneralChannel);
            mGeneralChannel.addListener(mDefaultChannelListener);

        }



    }


    private CallbackListener<ChatClient> mChatClientCallback =
            new CallbackListener<ChatClient>() {
                @Override
                public void onSuccess(ChatClient chatClient) {
                    mChatClient = chatClient;
                    loadChannels();
                    Log.d(TAG, "Success creating Twilio Chat Client");
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    Log.e(TAG,"Error creating Twilio Chat Client: " + errorInfo.getMessage());
                }
            };

    private ChannelListener mDefaultChannelListener = new ChannelListener() {


        @Override
        public void onMessageAdded(final Message message) {
            Log.d(TAG, "Message added");
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // need to modify user interface elements on the UI thread

                    mMessages.add(message);
                    mMessagesAdapter.notifyDataSetChanged();
                    mMessagesRecyclerView.smoothScrollToPosition(mMessages.size());
                }
            });

        }

        @Override
        public void onMessageUpdated(Message message, Message.UpdateReason updateReason) {
            Log.d(TAG, "Message updated: " + message.getMessageBody());
        }



        @Override
        public void onMessageDeleted(Message message) {
            Log.d(TAG, "Message deleted");
        }

        @Override
        public void onMemberAdded(Member member) {

        }

        @Override
        public void onMemberUpdated(Member member, Member.UpdateReason updateReason) {
            Log.d(TAG, "Member updated: " + member.getIdentity());

        }



        @Override
        public void onMemberDeleted(Member member) {
            Log.d(TAG, "Member deleted: " + member.getIdentity());
        }

        @Override
        public void onTypingStarted(Channel channel, Member member) {
            Log.d(TAG, "Started Typing: " + member.getIdentity());
        }

        @Override
        public void onTypingEnded(Channel channel, Member member) {
            Log.d(TAG, "Ended Typing: " + member.getIdentity());
        }



        @Override
        public void onSynchronizationChanged(Channel channel) {

        }
    };


    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {



        class ViewHolder extends RecyclerView.ViewHolder {

            public TextView mMessageTextView;
            public ImageView mMessageMedia;
            public VideoView mMessageVideo;
            public RelativeLayout rl_videomessage;
            public ProgressBar progressBar;
            public PDFView mMessagePDFView;

            //public SimpleExoPlayerView mMessageVideo;

            public ViewHolder(View view) {
                super(view);

                mMessageTextView = view.findViewById(R.id.tv_msg);
                mMessageMedia= view.findViewById(R.id.iv_image);
                mMessageVideo=view.findViewById(R.id.vv_media);
                rl_videomessage=view.findViewById(R.id.rl_videomessage);
                progressBar = view.findViewById(R.id.progressbar);
                mMessagePDFView=view.findViewById(R.id.pdfViewMessage);

            }
        }

        public MessagesAdapter() {

        }

        @Override
        public MessagesAdapter
                .ViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {

            View messageTextView =  LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_text_view, parent, false);
            return new ViewHolder(messageTextView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Message message = mMessages.get(position);
            if (message.hasMedia()) {



                final Message.Media media = message.getMedia();

                String sid = media.getSid();
                String type = media.getType();
                String fn = media.getFileName();
                long size = media.getSize();
                final String author = message.getAuthor();

                holder.mMessageTextView.setVisibility(View.VISIBLE);

               String s1= author + " : ";
                int textSize2 = getResources().getDimensionPixelSize(R.dimen.txt_size_20);
                SpannableString ss1=  new SpannableString(s1);
                ss1.setSpan(new AbsoluteSizeSpan(textSize2), 0,s1.length(), 0); // set size
                ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#E30047")), 0, s1.length(), 0);// set color
                holder.mMessageTextView.setText(ss1);
                if (type.contentEquals("image/*")) {
                    holder.mMessagePDFView.setVisibility(View.GONE);
                    holder.mMessageMedia.setVisibility(View.VISIBLE);
                    holder.mMessageVideo.setVisibility(View.GONE);
                    holder.rl_videomessage.setVisibility(View.GONE);

                    holder.mMessageMedia.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File file = new File(getApplicationContext().getCacheDir(), "" + media.getSid() + media.getFileName());
                            if (file.exists()) {
                                Intent i = new Intent(MainActivity.this,PreviewActivity.class);
                                i.putExtra("type","image");
                                i.putExtra("bmp",""+file.getPath());
                                getApplicationContext().startActivity(i);
                            }
                            Bitmap bm=((BitmapDrawable)holder.mMessageMedia.getDrawable()).getBitmap();
                            String bitmap = BitMapToString(bm);


                        }
                    });
                    File file = new File(getApplicationContext().getCacheDir(), "" + media.getSid() + media.getFileName());

                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                        holder.mMessageMedia.setImageBitmap(bitmap);
                        holder.mMessageTextView.setVisibility(View.VISIBLE);

                        Log.e("Author","name"+author);

                    }
                    else {


                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        media.download(out, new StatusListener() {
                            @Override
                            public void onSuccess() {
                                String content = out.toString();
                                byte[] bitmapdata = out.toByteArray();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
                                holder.mMessageMedia.setImageBitmap(bitmap);

                                try {


                                    File file = new File(getApplicationContext().getCacheDir(), "" + media.getSid() + media.getFileName());

                                    if (!file.exists()) {

                                        file.createNewFile();
                                    }

                                    //Convert bitmap to byte array
                                    Bitmap bitmap2 = bitmap;
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    bitmap2.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                                    byte[] bitmapdata2 = bos.toByteArray();

                                    //write the bytes in file
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(bitmapdata2);
                                    fos.flush();
                                    fos.close();
                                } catch (Exception e) {

                                }


                            }

                            @Override
                            public void onError(ErrorInfo error) {

                            }
                        }, new ProgressListener() {
                            @Override
                            public void onStarted() {

                            }

                            @Override
                            public void onProgress(long bytes) {

                            }

                            @Override
                            public void onCompleted(String mediaSid) {

                            }
                        });
                    }
                }
                if(type.contentEquals("video/*")){
                    holder.mMessagePDFView.setVisibility(View.GONE);
                    holder.mMessageMedia.setVisibility(View.GONE);
                    holder.mMessageVideo.setVisibility(View.VISIBLE);
                    holder.rl_videomessage.setVisibility(View.VISIBLE);
                   final File file2 = new File(getApplicationContext().getCacheDir(),""+media.getSid()+media.getFileName()+".mp4");
                   holder.rl_videomessage.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Intent i = new Intent(MainActivity.this,PreviewActivity.class);
                           i.putExtra("URI",""+file2.getPath());
                           i.putExtra("type","video");

                           Log.e("Try","videouri"+file2);

                           getApplicationContext().startActivity(i);
                       }
                   });



                    File file = new File(getApplicationContext().getCacheDir(),""+media.getSid()+media.getFileName()+".mp4");
                    if(file.exists()){

                        holder.mMessageTextView.setVisibility(View.VISIBLE);
                        Log.e("Author","name"+author);
                        holder.mMessageMedia.setVisibility(View.GONE);

                        holder.mMessageVideo.setVideoPath(file.getPath());
                        holder.mMessageVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                holder.mMessageVideo.start();
                                holder.mMessageVideo.pause();
                            }
                        });

                   /*     holder.mMessageVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                // TODO Auto-generated method stub
                                mp.start();
                                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                    @Override
                                    public void onVideoSizeChanged(MediaPlayer mp, int arg1,
                                                                   int arg2) {
                                        // TODO Auto-generated method stub

                                        mp.start();
                                    }
                                });
                            }
                        });*/
                    }
                    else {
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        media.download(out, new StatusListener() {
                            @Override
                            public void onSuccess() {

                                byte[] bitmapdata = out.toByteArray();

                                try {

                                    File file = new File(getApplicationContext().getCacheDir(),""+media.getSid()+media.getFileName()+".mp4");

                                    if (!file.exists()) {

                                        file.createNewFile();
                                    }
                                    Log.d("File", "path" + file.getPath());
                                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                                    bos.write(bitmapdata);
                                    bos.flush();
                                    bos.close();
                                    Uri uri = Uri.fromFile(new File(file.getPath()));
                                    Log.d("File", "path" + uri);



                                    holder.mMessageMedia.setVisibility(View.GONE);

                                    holder.mMessageVideo.setVideoURI(uri);
                                    holder.mMessageVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mediaPlayer) {
                                            holder.mMessageVideo.start();
                                            holder.mMessageVideo.pause();
                                        }
                                    });


                                } catch (Exception e) {
                                    Log.e("Exception", "Video: " + e);
                                }


                            }

                            @Override
                            public void onError(ErrorInfo error) {



                            }
                        }, new ProgressListener() {
                            @Override
                            public void onStarted() {

                            }

                            @Override
                            public void onProgress(long bytes) {

                            }

                            @Override
                            public void onCompleted(String mediaSid) {

                            }
                        });
                    }


                }
                if(type.contentEquals("application/pdf")){
                    holder.mMessageMedia.setVisibility(View.GONE);
                    holder.mMessageVideo.setVisibility(View.GONE);
                    holder.rl_videomessage.setVisibility(View.GONE);
                    holder.mMessagePDFView.setVisibility(View.VISIBLE);
                    final File file2 = new File(getApplicationContext().getCacheDir(),""+media.getSid()+media.getFileName()+".pdf");
                    holder.mMessagePDFView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(MainActivity.this,PreviewActivity.class);
                            i.putExtra("URI",""+file2.getPath());
                            i.putExtra("type","pdf");


                            getApplicationContext().startActivity(i);
                        }
                    });



                    File file = new File(getApplicationContext().getCacheDir(),""+media.getSid()+media.getFileName()+".pdf");
                    if(file.exists()){

                        holder.mMessageTextView.setVisibility(View.VISIBLE);

                        holder.mMessagePDFView.fromFile(file)
                                .enableSwipe(true) // allows to block changing pages using swipe
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .defaultPage(0)
                                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                                .password(null)
                                .scrollHandle(null)
                                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                                .spacing(0)
                                .load();

                    }
                    else {
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        media.download(out, new StatusListener() {
                            @Override
                            public void onSuccess() {

                                byte[] bitmapdata = out.toByteArray();

                                try {

                                    File file = new File(getApplicationContext().getCacheDir(),""+media.getSid()+media.getFileName()+".pdf");

                                    if (!file.exists()) {

                                        file.createNewFile();
                                    }
                                    Log.d("File", "path" + file.getPath());
                                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                                    bos.write(bitmapdata);
                                    bos.flush();
                                    bos.close();
                                    Uri uri = Uri.fromFile(new File(file.getPath()));
                                    Log.d("File", "path" + uri);



                                    holder.mMessagePDFView.fromFile(file)
                                            .enableSwipe(true) // allows to block changing pages using swipe
                                            .swipeHorizontal(false)
                                            .enableDoubletap(true)
                                            .defaultPage(0)
                                            .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                                            .password(null)
                                            .scrollHandle(null)
                                            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                                            .spacing(0)
                                            .load();

                                } catch (Exception e) {
                                    Log.e("Exception", "Video: " + e);
                                }


                            }

                            @Override
                            public void onError(ErrorInfo error) {



                            }
                        }, new ProgressListener() {
                            @Override
                            public void onStarted() {

                            }

                            @Override
                            public void onProgress(long bytes) {
                            /*Timber.d("Downloaded "+bytes+" bytes");*/
                            }

                            @Override
                            public void onCompleted(String mediaSid) {

                            }
                        });
                    }


                }
            }
            else {
                holder.mMessagePDFView.setVisibility(View.GONE);
                holder.mMessageTextView.setVisibility(View.VISIBLE);
                holder.mMessageMedia.setVisibility(View.GONE);
                holder.mMessageVideo.setVisibility(View.GONE);
                holder.rl_videomessage.setVisibility(View.GONE);

                int textSize2 = getResources().getDimensionPixelSize(R.dimen.txt_size_20);
                String s1= message.getAuthor()+ ": ";
                String s2 = message.getMessageBody();
                SpannableString ss1=  new SpannableString(s1);
                ss1.setSpan(new AbsoluteSizeSpan(textSize2), 0,s1.length(), 0); // set size
                ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#E30047")), 0, s1.length(), 0);// set color
                SpannableString ss2 = new SpannableString(s2);
                ss2.setSpan(new ForegroundColorSpan(Color.parseColor("#808080")),0,s2.length(),0);
                holder.mMessageTextView.setText(ss1);
                holder.mMessageTextView.append(ss2);
            }



        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }
    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
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