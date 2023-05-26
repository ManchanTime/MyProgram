package com.gachon.i_edu.fragment;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.ContentsActivity;
import com.gachon.i_edu.activity.MainActivity;
import com.gachon.i_edu.adpater.TextViewPagerAdapter;

import java.util.Timer;
import java.util.TimerTask;



public class MainFragment extends Fragment {
    private TextViewPagerAdapter pagerAdapter;
    MainActivity activity;
    private ViewPager viewPager;
    //private TextViewPagerAdapter pagerAdapter;


    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000; // time in milliseconds between successive task executions.


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup fragment_container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, fragment_container, false);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        viewPager = rootView.findViewById(R.id.viewPager2);
        pagerAdapter = new TextViewPagerAdapter(rootView.getContext());
        viewPager.setAdapter(pagerAdapter);
        ImageView imageView;
        ImageView imageView2;
        Button contents;
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            @Override
            public void run() {
                if(currentPage == 3) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);


        Button note = rootView.findViewById(R.id.note);
        Button btn_link = rootView.findViewById(R.id.btn_link);
        Button btn_link2 = rootView.findViewById(R.id.btn_link2);

        contents = rootView.findViewById(R.id.contents);
        contents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ContentsActivity.class);
                startActivity(intent);
            }
        });

        imageView = rootView.findViewById(R.id.youtube);
        String imageStr = "https://img.youtube.com/vi/FLXFbDudeBs/default.jpg";
        Glide.with(this).load(imageStr).into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=FLXFbDudeBs"));
                startActivity(intent);
            }
        });

        btn_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=FLXFbDudeBs"));
                startActivity(intent);
            }
        });




        imageView2 = rootView.findViewById(R.id.youtube2);
        String imageStr2 = "https://img.youtube.com/vi/wDfqXR_5yyQ/default.jpg";
        Glide.with(this).load(imageStr2).into(imageView2);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=wDfqXR_5yyQ"));
                startActivity(intent);
            }
        });

        btn_link2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=wDfqXR_5yyQ"));
                startActivity(intent);
            }
        });


        return rootView;
    }

}
