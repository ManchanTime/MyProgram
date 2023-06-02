package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.ReplyAdapter;
import com.gachon.i_edu.adpater.UserPostAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.PostInfo;
import com.gachon.i_edu.info.ReplyInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class MyPageActivity extends BasicFunctions {
    private ImageView image_profile, btn_back, image_announce;
    private TextView text_name, text_tier, text_second_name, text_set_title;
    private TextView text_post_count, text_reply_count, text_announce, textCheckLevel;
    private String profile_uri, name, tier;
    private TabLayout tabs;
    private RelativeLayout layout_announce;
    private boolean updating;
    private ProgressBar progressBar;
    private ProgressDialog customProgressDialog;

    //공통
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private int change;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    //포스트
    private RecyclerView recyclerViewPost;
    private ArrayList<PostInfo> postList = new ArrayList<>();
    private UserPostAdapter userPostAdapter;

    //댓글
    private RecyclerView recyclerViewReply;
    private ArrayList<ReplyInfo> replyList = new ArrayList<>();
    private ReplyAdapter replyAdapter;

    //좋아요
    private RecyclerView recyclerViewLike;
    private ArrayList<PostInfo> likeList = new ArrayList<>();
    private UserPostAdapter likePostAdapter;

    @Override
    public void onResume(){
        super.onResume();
        set_up_post();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        textCheckLevel = findViewById(R.id.text_check_level);
        progressBar = findViewById(R.id.progressBar);
        text_set_title = findViewById(R.id.text_title);
        text_set_title.setText("내 프로필");
        layout_announce = findViewById(R.id.layout_announce);
        image_announce = findViewById(R.id.image_announce);
        text_announce = findViewById(R.id.text_announce);
        layout_announce.setOnClickListener(onClickListener);
        image_announce.setOnClickListener(onClickListener);
        text_announce.setOnClickListener(onClickListener);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                String remain = "다음 레벨까지 남은 문제 수 ";
                int question = Integer.parseInt(documentSnapshot.getData().get("question").toString());
                int solve = Integer.parseInt(documentSnapshot.getData().get("solve").toString());
                name = documentSnapshot.getData().get("name").toString();
                tier = documentSnapshot.getData().get("level").toString();
                image_profile = findViewById(R.id.image_profile);
                if (documentSnapshot.getData().get("photoUri") != null) {
                    profile_uri = documentSnapshot.getData().get("photoUri").toString();
                    Glide.with(getApplicationContext()).load(profile_uri).into(image_profile);
                }
                if(tier.equals("Bronze")){
                    remain += (10 - (question + solve - 1)) + "";
                    progressBar.setProgress((int) ((question + solve - 1) / 10.0 * 100));
                }else if(tier.equals("Silver")){
                    remain += (10 - (question + solve - 10)) + "";
                    progressBar.setProgress((int) ((question + solve - 10) / 50.0 * 100));
                }else if(tier.equals("Gold")){
                    remain += (10 - (question + solve - 50)) + "";
                    progressBar.setProgress((int) ((question + solve - 50) / 100.0 * 100));
                }else if(tier.equals("Diamond")){
                    remain = "이미 최고 레벨입니다.";
                    progressBar.setProgress(100);
                }else{
                    remain += (1) + "";
                    progressBar.setProgress(0);
                }
                textCheckLevel.setText(remain);
                String setPost = "게시글 수 " + question;
                String setReply = "댓글 수 " + solve;
                text_post_count = findViewById(R.id.text_post_count);
                text_reply_count = findViewById(R.id.text_reply_count);
                text_post_count.setText(setPost);
                text_reply_count.setText(setReply);
                image_profile.setOnClickListener(onClickListener);
                text_name = findViewById(R.id.text_name);
                text_name.setText(name);
                text_tier = findViewById(R.id.text_tier);
                text_tier.setText(tier);
                text_second_name = findViewById(R.id.second_name);
                text_second_name.setText(documentSnapshot.getData().get("goal").toString());
            }
        });

        //어댑터
        userPostAdapter = new UserPostAdapter(this, postList);
        userPostAdapter.setHasStableIds(true);

        replyAdapter = new ReplyAdapter(this, replyList, null);
        replyAdapter.setHasStableIds(true);

        likePostAdapter = new UserPostAdapter(this, likeList);
        likePostAdapter.setHasStableIds(true);

        settingRecycler();

        tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("게시글"));
        tabs.addTab(tabs.newTab().setText("답변"));
        tabs.addTab(tabs.newTab().setText("좋아요"));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    change = 0;
                    recyclerViewPost.setVisibility(View.VISIBLE);
                    recyclerViewReply.setVisibility(View.GONE);
                    recyclerViewLike.setVisibility(View.GONE);
                    set_up_post();
                } else if(position == 1){
                    change = 1;
                    recyclerViewPost.setVisibility(View.GONE);
                    recyclerViewReply.setVisibility(View.VISIBLE);
                    recyclerViewLike.setVisibility(View.GONE);
                    set_up_reply();
                }else{
                    change = 2;
                    recyclerViewPost.setVisibility(View.GONE);
                    recyclerViewReply.setVisibility(View.GONE);
                    recyclerViewLike.setVisibility(View.VISIBLE);
                    set_up_like();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(onClickListener);
    }

    private void set_up_post(){
        updating = true;
        firebaseFirestore = FirebaseFirestore.getInstance();
        Date date = postList.size() == 0 ? new Date() : postList.get(postList.size()-1).getCreatedAt();
        collectionReference = firebaseFirestore.collection("posts");
        collectionReference.whereEqualTo("publisher",user.getUid()).whereLessThan("createdAt", date)
                .limit(5)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
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
                            userPostAdapter.notifyDataSetChanged();
                        }
                        updating = false;
                    }
                });
    }

    private void set_up_reply(){
        updating = true;
        Date date = replyList.size() == 0 ? new Date() : replyList.get(replyList.size()-1).getCreatedAt();
        collectionReference = firebaseFirestore.collection("replies");
        collectionReference.whereEqualTo("publisher", user.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt", date)
                .limit(5)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                replyList.add(new ReplyInfo(
                                                document.getData().get("id").toString(),
                                                document.getData().get("postId").toString(),
                                                document.getData().get("postPublisher").toString(),
                                                document.getData().get("publisher").toString(),
                                                (ArrayList<String>)document.getData().get("content"),
                                                new Date(document.getDate("createdAt").getTime()),
                                                (ArrayList<String>)document.getData().get("like_list"),
                                                Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                Long.valueOf(String.valueOf(document.getData().get("reply_count"))),
                                                (boolean)document.getData().get("flag"),
                                                (boolean)document.getData().get("read")
                                        )
                                );
                            }
                            //리사이클러 뷰 초기화
                            replyAdapter.notifyDataSetChanged();
                        }
                        updating = false;
                    }
                });
    }

    //좋아요
    private void set_up_like(){
        firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                customProgressDialog.show();
                //화면터치 방지
                customProgressDialog.setCanceledOnTouchOutside(false);
                //뒤로가기 방지
                customProgressDialog.setCancelable(false);
                ArrayList<String> post_like = (ArrayList<String>) task.getResult().getData().get("like_post");
                likeList.clear();
                for(int i=0;i<post_like.size();i++){
                    String postId = post_like.get(i);
                    DocumentReference getPostInfo = firebaseFirestore.collection("posts").document(postId);
                    getPostInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot documentSnapshot = task.getResult();
                                likeList.add(new PostInfo(
                                                documentSnapshot.getData().get("id").toString(),
                                                documentSnapshot.getData().get("publisher").toString(),
                                                documentSnapshot.getData().get("field").toString(),
                                                documentSnapshot.getData().get("subject").toString(),
                                                documentSnapshot.getData().get("title").toString(),
                                                (ArrayList<String>)documentSnapshot.getData().get("main_content"),
                                                (ArrayList<String>)documentSnapshot.getData().get("sub_content"),
                                                new Date(documentSnapshot.getDate("createdAt").getTime()),
                                                Long.valueOf(String.valueOf(documentSnapshot.getData().get("like_count"))),
                                                Long.valueOf(String.valueOf(documentSnapshot.getData().get("reply_count")))
                                        )
                                );
                                //리사이클러 뷰 초기화
                                likePostAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
                customProgressDialog.cancel();
            }
        });
    }

    public void settingRecycler(){
        //리사이클러 뷰 초기화
        recyclerViewPost = findViewById(R.id.recycler_post);
        recyclerViewPost.setHasFixedSize(true);
        recyclerViewPost.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPost.setItemViewCacheSize(100);
        recyclerViewPost.setAdapter(userPostAdapter);
        recyclerViewPost.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

                if (totalItemCount - 2 <= lastVisibleItemPosition && !updating) {
                    if (change == 0)
                        set_up_post();
                }
            }
        });
        recyclerViewReply = findViewById(R.id.recycler_reply);
        recyclerViewReply.setHasFixedSize(true);
        recyclerViewReply.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReply.setItemViewCacheSize(100);
        recyclerViewReply.setAdapter(replyAdapter);
        recyclerViewReply.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

                if (totalItemCount - 2 <= lastVisibleItemPosition && !updating) {
                    if(change == 1)
                        set_up_reply();
                }
            }
        });
        recyclerViewLike = findViewById(R.id.recycler_like);
        recyclerViewLike.setHasFixedSize(true);
        recyclerViewLike.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewLike.setItemViewCacheSize(100);
        recyclerViewLike.setAdapter(likePostAdapter);
    }

    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.btn_back:
                finish();
                break;
            case R.id.image_announce:
            case R.id.text_announce:
                layout_announce.setVisibility(View.VISIBLE);
                break;
            case R.id.layout_announce:
                layout_announce.setVisibility(View.GONE);
        }
    };
}