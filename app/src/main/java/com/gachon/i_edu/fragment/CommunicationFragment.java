package com.gachon.i_edu.fragment;

import android.content.Context;
import android.content.Intent;
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

import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.PostListAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.PostInfo;
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


public class CommunicationFragment extends Fragment {
    private View view;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<String> likeList;
    private ArrayList<PostInfo> postList = new ArrayList<>();
    private PostListAdapter postListAdapter;
    private ProgressDialog customProgressDialog;
    private boolean updating;

    @Override
    public void onResume() {
        super.onResume();
        if (postListAdapter != null)
            refresh_top();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Firebase);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_communication, container, false);

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(getActivity());
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);
        swipeRefreshLayout = view.findViewById(R.id.layout_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_top();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

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
        customProgressDialog.cancel();
        return view;
    }

    private void set_up(){
        //문서 가져오기
        updating = true;
        Date date = postList.size() == 0 ? new Date() : postList.get(postList.size()-1).getCreatedAt();
        collectionReference = firebaseFirestore.collection("posts");
        collectionReference.whereLessThan("createdAt", date)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .whereEqualTo("field", "소통")
                .limit(5)
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
                            postListAdapter.notifyDataSetChanged();
                        }
                        updating = false;
                    }
                });
    }

    private void refresh_top(){
        //문서 가져오기
        collectionReference = firebaseFirestore.collection("posts");
        collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)
                .whereEqualTo("field", "소통")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
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
    }

}