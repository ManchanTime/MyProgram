package com.gachon.i_edu.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageActivity extends BasicFunctions {

    PhotoView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        String image = getIntent().getStringExtra("image");
        img = findViewById(R.id.image_full);

        ImageView over = findViewById(R.id.btn_cancel);
        Glide.with(this).load(image).into(img);
        over.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
