package com.gachon.i_edu.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.PostListAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.PostInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

public class QuestionFragment extends Fragment {

    private View view;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<PostInfo> postList = new ArrayList<>();
    private PostListAdapter postListAdapter;
    private ProgressDialog customProgressDialog;
    private TabLayout tabs;
    private String subject;
    private boolean updating;

    @Override
    public void onResume(){
        super.onResume();
        if (postListAdapter != null)
            refresh_top();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Firebase);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_question, container, false);

        subject = "전체";

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(getActivity());
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        swipeRefreshLayout = view.findViewById(R.id.layout_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                set_up();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        loadPost();
        tabs = view.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("전체"));
        tabs.addTab(tabs.newTab().setText("수학"));
        tabs.addTab(tabs.newTab().setText("영어"));
        tabs.addTab(tabs.newTab().setText("국어"));
        tabs.addTab(tabs.newTab().setText("과학"));
        tabs.addTab(tabs.newTab().setText("사회"));
        tabs.addTab(tabs.newTab().setText("코딩"));
        tabs.addTab(tabs.newTab().setText("기타"));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position){
                    case 0:
                        subject = "전체";
                        break;
                    case 1:
                        subject = "수학";
                        break;
                    case 2:
                        subject = "영어";
                        break;
                    case 3:
                        subject = "국어";
                        break;
                    case 4:
                        subject = "과학";
                        break;
                    case 5:
                        subject = "사회";
                        break;
                    case 6:
                        subject = "코딩";
                        break;
                    case 7:
                        subject = "기타";
                        break;
                }
                postList.clear();
                loadPost();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        return view;
    }
    private void loadPost() {
        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        //리사이클러 뷰 초기화
        recyclerView = view.findViewById(R.id.recycler_board_list);
        postListAdapter = new PostListAdapter(getActivity(), postList);
        postListAdapter.setHasStableIds(true);
        //recyclerView.setItemViewCacheSize(100);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(postListAdapter);
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
                    set_up();
                }
            }
        });
        set_up();
    }

    private void set_up(){
        //문서 가져오기
        updating = true;
        Date date = postList.size() == 0 ? new Date() : postList.get(postList.size()-1).getCreatedAt();
        collectionReference = firebaseFirestore.collection("posts");
        if(subject.equals("전체")){
            collectionReference.whereLessThan("createdAt", date)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .whereEqualTo("field", "질문")
                    .limit(5).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                postListAdapter.notifyDataSetChanged();
                            }
                            updating = false;
                            customProgressDialog.cancel();
                        }
                    });
        }
        else{
            collectionReference.whereLessThan("createdAt", date)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .whereEqualTo("subject", subject)
                    .limit(5).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                postListAdapter.notifyDataSetChanged();
                            }
                            updating = false;
                            customProgressDialog.cancel();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Err","Err");
                            customProgressDialog.cancel();
                        }
                    });
        }
    }

    private void refresh_top() {
        //문서 가져오기
        if (subject.equals("전체")) {
            collectionReference = firebaseFirestore.collection("posts");
            collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)
                    .whereEqualTo("field", "질문")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                postList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    postList.add(new PostInfo(
                                                    document.getData().get("id").toString(),
                                                    document.getData().get("publisher").toString(),
                                                    document.getData().get("field").toString(),
                                                    document.getData().get("subject").toString(),
                                                    document.getData().get("title").toString(),
                                                    (ArrayList<String>) document.getData().get("main_content"),
                                                    (ArrayList<String>) document.getData().get("sub_content"),
                                                    new Date(document.getDate("createdAt").getTime()),
                                                    Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                    Long.valueOf(String.valueOf(document.getData().get("reply_count")))
                                            )
                                    );
                                }
                                //리사이클러 뷰 초기화
                                postListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }else{
            collectionReference = firebaseFirestore.collection("posts");
            collectionReference.orderBy("createdAt", Query.Direction.ASCENDING)
                    .whereEqualTo("subject", subject)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    postList.add(0, new PostInfo(
                                                    document.getData().get("id").toString(),
                                                    document.getData().get("publisher").toString(),
                                                    document.getData().get("field").toString(),
                                                    document.getData().get("subject").toString(),
                                                    document.getData().get("title").toString(),
                                                    (ArrayList<String>) document.getData().get("main_content"),
                                                    (ArrayList<String>) document.getData().get("sub_content"),
                                                    new Date(document.getDate("createdAt").getTime()),
                                                    Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                    Long.valueOf(String.valueOf(document.getData().get("reply_count")))
                                            )
                                    );
                                }
                                //리사이클러 뷰 초기화
                                postListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }
}