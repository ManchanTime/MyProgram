package com.gachon.i_edu.fragment;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.ContentsActivity;
import com.gachon.i_edu.activity.MainActivity;
import com.gachon.i_edu.adpater.HotAdapter;
import com.gachon.i_edu.adpater.TextViewPagerAdapter;
import com.gachon.i_edu.info.PostInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;



public class MainFragment extends Fragment {
    private TextViewPagerAdapter pagerAdapter;
    MainActivity activity;
    private ViewPager viewPager;
    private TextView textGoal;
    private RecyclerView recycler_hot;
    private HotAdapter hotAdapter;
    private ArrayList<PostInfo> postList = new ArrayList<>();
    //private TextViewPagerAdapter pagerAdapter;


    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000; // time in milliseconds between successive task executions.

    @Override
    public void onResume(){
        super.onResume();
        setOn();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                textGoal.setText(task.getResult().getData().get("goal").toString());
            }
        });
    }

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

        recycler_hot = rootView.findViewById(R.id.recycler_hot);
        recycler_hot.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        hotAdapter = new HotAdapter(getActivity(), postList);
        hotAdapter.setHasStableIds(true);
        recycler_hot.setHasFixedSize(true);
        recycler_hot.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_hot.setAdapter(hotAdapter);

        ImageView imageView;
        ImageView imageView2;
        Button contents, answer;
        RadioButton r_btn1, r_btn2, r_btn3;
        RadioGroup radioGroup;
        textGoal = rootView.findViewById(R.id.text_goal);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        textGoal.setText(task.getResult().getData().get("goal").toString());
                    }
                });
        setOn();
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            @Override
            public void run() {
                if (currentPage == 3) {
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


        Button btn_link = rootView.findViewById(R.id.btn_link);
        Button btn_link2 = rootView.findViewById(R.id.btn_link2);
        answer = rootView.findViewById(R.id.answer);

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

        //라디오 그룹 설정
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);



        answer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Toast.makeText(rootView.getContext(), "answer", Toast.LENGTH_SHORT).show();
//                RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
//                        if (i == R.id.rg_btn1) {
//                            Log.e("tgt", "11111111111111111111");
//                        } else if (i == R.id.rg_btn2) {
//                            Log.e("tgt", "22222222222222222222");
//                        } else if (i == R.id.rg_btn3) {
//                            Log.e("tgt", "3333333333333333333333333");
//                        }
//                    }
//                };

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId){
                            case R.id.rg_btn1:
                                Toast.makeText(rootView.getContext(), "11111111111111", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.rg_btn2:
                                Toast.makeText(rootView.getContext(), "22222222222", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.rg_btn3:
                                Toast.makeText(rootView.getContext(), "3333333333", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            }
        });


        return rootView;
    }

    public void setOn(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("posts");
        collectionReference.orderBy("like_count", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            postList.clear();
                            for(QueryDocumentSnapshot document : task.getResult()){
                                postList.add(new PostInfo(
                                                document.getData().get("id").toString(),
                                                document.getData().get("publisher").toString(),
                                                document.getData().get("field").toString(),
                                                document.getData().get("subject").toString(),
                                                document.getData().get("title").toString(),
                                                (ArrayList<String>)document.getData().get("main_content"),
                                                (ArrayList<String>)document.getData().get("sub_content"),
                                                new Date(document.getDate("createdAt").getTime()),
                                                Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                Long.valueOf(String.valueOf(document.getData().get("reply_count")))
                                        )
                                );
                            }
                            //리사이클러 뷰 초기화
                            hotAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

}
