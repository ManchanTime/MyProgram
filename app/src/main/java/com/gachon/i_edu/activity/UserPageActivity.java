package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.ReplyAdapter;
import com.gachon.i_edu.adpater.UserPostAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.MemberInfo;
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

public class UserPageActivity extends BasicFunctions {
    private ImageView image_profile, btn_back;
    private TextView text_name, text_tier, text_second_name;
    private String profile_uri, name, tier, uid;
    private TabLayout tabs;
    private Bundle bundle;
    private ProgressDialog customProgressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button btnChat;
    private boolean updating;
    private MemberInfo memberInfo;

    //공통
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private boolean change;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerView recyclerView;

    //포스트
    private ArrayList<PostInfo> postList = new ArrayList<>();
    private UserPostAdapter userPostAdapter;

    //댓글
    private ArrayList<ReplyInfo> replyList = new ArrayList<>();
    private ReplyAdapter replyAdapter;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        btnChat = findViewById(R.id.btn_chat);
        btnChat.setOnClickListener(onClickListener);

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        swipeRefreshLayout = findViewById(R.id.layout_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!change)
                    refresh_top_post();
                else
                    refresh_top_reply();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        Intent intent = getIntent();
        if(intent != null) {
            Bundle get = intent.getExtras();
            uid = get.getString("uid");
            bundle = new Bundle();
            bundle.putString("uid", uid);

            if(uid.equals(user.getUid())){
                btnChat.setVisibility(View.GONE);
            }

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    name = documentSnapshot.getData().get("name").toString();
                    tier = documentSnapshot.getData().get("level").toString();
                    image_profile = findViewById(R.id.image_profile);
                    if (documentSnapshot.getData().get("photoUri") != null) {
                        profile_uri = documentSnapshot.getData().get("photoUri").toString();
                        Glide.with(getApplicationContext()).load(profile_uri).into(image_profile);
                    }
                    image_profile.setOnClickListener(onClickListener);
                    text_name = findViewById(R.id.text_name);
                    text_name.setText(name);
                    text_tier = findViewById(R.id.text_tier);
                    text_tier.setText(tier);
                    text_second_name = findViewById(R.id.second_name);
                    text_second_name.setText(documentSnapshot.getData().get("goal").toString());
                    memberInfo = new MemberInfo(
                            uid,
                            profile_uri,
                            name,
                            documentSnapshot.getData().get("birthday").toString(),
                            documentSnapshot.getData().get("school").toString(),
                            documentSnapshot.getData().get("goal").toString(),
                            documentSnapshot.getData().get("level").toString(),
                            Integer.valueOf(String.valueOf(documentSnapshot.getData().get("solve"))),
                            Integer.valueOf(String.valueOf(documentSnapshot.getData().get("question"))),
                            (ArrayList<String>) documentSnapshot.getData().get("like_post"));
                    customProgressDialog.cancel();
                }
            });

            //어댑터
            userPostAdapter = new UserPostAdapter(UserPageActivity.this, postList);
            userPostAdapter.setHasStableIds(true);
            replyAdapter = new ReplyAdapter(UserPageActivity.this, replyList, null);
            replyAdapter.setHasStableIds(true);

            //리사이클러 뷰 초기화
            recyclerView = findViewById(R.id.recycler);
            recyclerView.setAdapter(userPostAdapter);
            //recyclerView.setItemViewCacheSize(100);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(UserPageActivity.this));
            recyclerView.setAdapter(userPostAdapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        if (!change)
                            set_up_post();
                        else
                            set_up_reply();
                    }
                }
            });

            userPostAdapter = new UserPostAdapter(UserPageActivity.this, postList);
            userPostAdapter.setHasStableIds(true);
            recyclerView.setAdapter(userPostAdapter);
            //recyclerView.setItemViewCacheSize(100);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(UserPageActivity.this));
            recyclerView.setAdapter(userPostAdapter);
            set_up_post();

            tabs = findViewById(R.id.tabs);
            tabs.addTab(tabs.newTab().setText("게시글"));
            tabs.addTab(tabs.newTab().setText("답변"));
            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    if(position == 0){
                        change = false;
                        recyclerView.setAdapter(userPostAdapter);
                        set_up_post();
                    }
                    else{
                        change = true;
                        recyclerView.setAdapter(replyAdapter);
                        set_up_reply();
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            btn_back = findViewById(R.id.btn_back);
            btn_back.setOnClickListener(onClickListener);
        }
    }
    private void set_up_post(){
        //문서 가져오기
        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);
        updating = true;
        firebaseFirestore = FirebaseFirestore.getInstance();
        Date date = postList.size() == 0 ? new Date() : postList.get(postList.size()-1).getCreatedAt();
        collectionReference = firebaseFirestore.collection("posts");
        collectionReference.whereEqualTo("publisher",uid).whereLessThan("createdAt", date)
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
                        customProgressDialog.cancel();
                    }
                });
    }

    private void refresh_top_post(){
        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);
        //문서 가져오기
        collectionReference = firebaseFirestore.collection("posts");
        collectionReference.orderBy("createdAt", Query.Direction.DESCENDING).whereEqualTo("publisher",uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            userPostAdapter.notifyDataSetChanged();
                        }
                        customProgressDialog.cancel();
                    }
                });
    }

    private void set_up_reply(){
        //문서 가져오기
        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);
        updating = true;
        Date date = replyList.size() == 0 ? new Date() : replyList.get(replyList.size()-1).getCreatedAt();
        collectionReference = firebaseFirestore.collection("replies");
        collectionReference.whereEqualTo("publisher", uid)
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
                                                (boolean) document.getData().get("flag"),
                                                (boolean) document.getData().get("read")
                                        )
                                );
                            }
                            //리사이클러 뷰 초기화
                            replyAdapter.notifyDataSetChanged();
                            customProgressDialog.cancel();
                        }
                        updating = false;
                    }
                });
    }

    private void refresh_top_reply() {
        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);
        //문서 가져오기
        collectionReference = firebaseFirestore.collection("replies");
        collectionReference.whereEqualTo("publisher", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            replyList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                replyList.add(new ReplyInfo(
                                                document.getData().get("id").toString(),
                                                document.getData().get("postId").toString(),
                                                document.getData().get("postPublisher").toString(),
                                                document.getData().get("publisher").toString(),
                                                (ArrayList<String>) document.getData().get("content"),
                                                new Date(document.getDate("createdAt").getTime()),
                                                (ArrayList<String>) document.getData().get("like_list"),
                                                Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                Long.valueOf(String.valueOf(document.getData().get("reply_count"))),
                                                (boolean) document.getData().get("flag"),
                                                (boolean) document.getData().get("read")
                                        )
                                );
                            }
                            //리사이클러 뷰 초기화
                            replyAdapter.notifyDataSetChanged();
                            customProgressDialog.cancel();
                        }
                    }
                });
    }

    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_chat:
                //로딩창
                customProgressDialog.show();
                //화면터치 방지
                customProgressDialog.setCanceledOnTouchOutside(false);
                //뒤로가기 방지
                customProgressDialog.setCancelable(false);
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                firebaseFirestore.collection("users").document(user.getUid())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                Intent intent = new Intent(UserPageActivity.this, ChattingActivity.class);
                                String intentName = task.getResult().getData().get("name").toString();
                                String photoUrl = task.getResult().getData().get("photoUri").toString();
                                intent.putExtra("photoUrl", photoUrl);
                                intent.putExtra("object", memberInfo);
                                intent.putExtra("name", intentName);
                                startActivity(intent);
                                customProgressDialog.cancel();
                            }
                        });
                break;
        }
    };
}