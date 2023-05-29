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
import com.gachon.i_edu.adpater.UserPostAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
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

import java.util.ArrayList;
import java.util.Date;

public class UserPostFragment extends Fragment {

    private View view;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String uid;
    private ArrayList<String> likeList;
    private ProgressDialog customProgressDialog;
    private UserPostAdapter userPostAdapter;
    private ArrayList<PostInfo> postList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Firebase);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_user_post, container, false);

        customProgressDialog = new ProgressDialog(getActivity());
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        firebaseFirestore = FirebaseFirestore.getInstance();
        swipeRefreshLayout = view.findViewById(R.id.layout_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_top();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //리사이클러 뷰 초기화
        recyclerView = view.findViewById(R.id.recycler_post_list);
        userPostAdapter = new UserPostAdapter(getActivity(), postList);
        userPostAdapter.setHasStableIds(true);
        recyclerView.setAdapter(userPostAdapter);
        //recyclerView.setItemViewCacheSize(100);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(userPostAdapter);

        set_up();
        customProgressDialog.cancel();

        return view;
    }

    public void onResume(){
        super.onResume();
        //refresh();
    }

    private void set_up(){
        //문서 가져오기
        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);
        collectionReference = firebaseFirestore.collection("posts");
        collectionReference.whereEqualTo("publisher",uid)
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
                                                document.getData().get("title").toString(),
                                                document.getData().get("field").toString(),
                                                document.getData().get("subject").toString(),
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

    private void refresh_top(){
        //문서 가져오기
        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);
        Date date = postList.size() == 0 ? new Date() : postList.get(0).getCreatedAt();
        collectionReference = firebaseFirestore.collection("posts");
        collectionReference.orderBy("createdAt", Query.Direction.ASCENDING).whereGreaterThan("createdAt", date)
                .whereEqualTo("publisher",uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                postList.add(0, new PostInfo(
                                                document.getData().get("id").toString(),
                                                document.getData().get("publisher").toString(),
                                                document.getData().get("title").toString(),
                                                document.getData().get("field").toString(),
                                                document.getData().get("subject").toString(),
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
}