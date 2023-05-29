package com.gachon.i_edu.fragment;

import android.annotation.SuppressLint;
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

import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.MyPageActivity;
import com.gachon.i_edu.activity.SearchActivity;
import com.gachon.i_edu.activity.SearchUserActivity;
import com.gachon.i_edu.adpater.UserListAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.MemberInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class PersonalChatFragment extends Fragment {

    private View view;
    private ArrayList<MemberInfo> userList, filteredList;
    private ProgressDialog customProgressDialog;
    private RecyclerView recyclerView;
    private UserListAdapter userListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textTitle;
    private ImageView btnSearch, btnMyPage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Firebase);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_personal_chat, container, false);

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(getActivity());
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        userList = new ArrayList<>();
        filteredList = new ArrayList<>();

        textTitle = view.findViewById(R.id.text_title);
        textTitle.setText("채팅");
        btnSearch = view.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(onClickListener);
        btnMyPage = view.findViewById(R.id.btn_my_page);
        btnMyPage.setOnClickListener(onClickListener);

        //어댑터
        userListAdapter = new UserListAdapter(getActivity(), userList);
        userListAdapter.setHasStableIds(true);

        //리사이클러 뷰 초기화
        recyclerView = view.findViewById(R.id.recycler_user);
        recyclerView.setAdapter(userListAdapter);
        //recyclerView.setItemViewCacheSize(100);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(userListAdapter);

        swipeRefreshLayout = view.findViewById(R.id.layout_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setOn();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        setOn();
        return view;
    }

    private void setOn(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("users");
        collectionReference.whereNotEqualTo("uid", user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        customProgressDialog.show();
                        //화면터치 방지
                        customProgressDialog.setCanceledOnTouchOutside(false);
                        //뒤로가기 방지
                        customProgressDialog.setCancelable(false);
                        if(task.isSuccessful()){
                            userList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userList.add(new MemberInfo(
                                        document.getData().get("uid").toString(),
                                        document.getData().get("photoUri").toString(),
                                        document.getData().get("name").toString(),
                                        document.getData().get("birthday").toString(),
                                        document.getData().get("school").toString(),
                                        document.getData().get("goal").toString(),
                                        document.getData().get("level").toString(),
                                        Integer.valueOf(String.valueOf(document.getData().get("solve"))),
                                        Integer.valueOf(String.valueOf(document.getData().get("question"))),
                                        (ArrayList<String>) document.getData().get("like_post"))
                                );
                            }
                            //리사이클러 뷰 초기화
                            userListAdapter.notifyDataSetChanged();
                            customProgressDialog.cancel();
                        }
                    }
                });
    }

    View.OnClickListener onClickListener = (v) -> {
        Intent intent;
        switch (v.getId()){
            case R.id.btn_search:
                intent = new Intent(getActivity(), SearchUserActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_my_page:
                 intent = new Intent(getActivity(), MyPageActivity.class);
                 startActivity(intent);
                 break;
        }
    };
}