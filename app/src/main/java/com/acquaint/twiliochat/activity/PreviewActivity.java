package com.acquaint.twiliochat.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.acquaint.twiliochat.R;
import com.github.barteksc.pdfviewer.PDFView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class PreviewActivity extends Activity {
    String type;
    String uri;
    String bitmap;
    ImageView iv_fullscreenimage;
    VideoView vv_fullscreenvideo;
    PDFView pdfViewFullScreen;
    Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        iv_fullscreenimage=(ImageView)findViewById(R.id.iv_fullscreenimage);
        vv_fullscreenvideo=(VideoView)findViewById(R.id.vv_fullscreenvideo);
        pdfViewFullScreen=(PDFView)findViewById(R.id.pdfFullScreen);
        type=getIntent().getStringExtra("type");
        Log.e("PreviewActivity","type"+type);
        if(type.equals("image")){

            bitmap=getIntent().getStringExtra("bmp");
            Log.e("PreviewActivity","Uri"+uri);


            Bitmap bmp = BitmapFactory.decodeFile(bitmap);
            iv_fullscreenimage.setVisibility(View.VISIBLE);
            vv_fullscreenvideo.setVisibility(View.GONE);
            pdfViewFullScreen.setVisibility(View.GONE);
            iv_fullscreenimage.setImageBitmap(bmp);

        }
        else if(type.equals("video")) {
            uri=getIntent().getStringExtra("URI");
            Log.e("PreviewActivity","Uri"+uri);
            iv_fullscreenimage.setVisibility(View.GONE);
            vv_fullscreenvideo.setVisibility(View.VISIBLE);
            pdfViewFullScreen.setVisibility(View.GONE);
            vv_fullscreenvideo.setVideoPath(uri);
            vv_fullscreenvideo.start();
        }
        else if(type.equals("pdf")){

            uri=getIntent().getStringExtra("URI");
            iv_fullscreenimage.setVisibility(View.GONE);
            vv_fullscreenvideo.setVisibility(View.GONE);
            pdfViewFullScreen.setVisibility(View.VISIBLE);
            FileInputStream fileStream;
            try {
                File file = new File(uri);
                fileStream = new FileInputStream(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            pdfViewFullScreen.fromStream(fileStream)
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
            Toast.makeText(getApplicationContext(),"Invalid File",Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public void onBackPressed() {
        this.finish();
    }
}
