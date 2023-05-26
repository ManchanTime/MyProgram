package com.gachon.i_edu.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;

public class ContentsActivity extends BasicFunctions {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        ImageView imageView;
        ImageView imageView2;
        Button btn_link;
        Button btn_link2;
        btn_link = findViewById(R.id.btn_link);
        btn_link2 = findViewById(R.id.btn_link2);

        //그림1
        imageView = findViewById(R.id.youtube);
        String imageStr = "https://img.youtube.com/vi/glmxKRoB3_s/default.jpg";
        Glide.with(this).load(imageStr).into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=glmxKRoB3_s"));
                startActivity(intent);
            }
        });

        btn_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=glmxKRoB3_s"));
                startActivity(intent);
            }
        });


        //그림2
        imageView2 = findViewById(R.id.youtube2);
        String imageStr2 = "https://img.youtube.com/vi/D16Ilhnzbcc/default.jpg";
        Glide.with(this).load(imageStr2).into(imageView2);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=D16Ilhnzbcc"));
                startActivity(intent);
            }
        });

        btn_link2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=D16Ilhnzbcc"));
                startActivity(intent);
            }
        });
    }
}
