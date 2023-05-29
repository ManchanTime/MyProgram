package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.PostListAdapter;
import com.gachon.i_edu.adpater.UserListAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.MemberInfo;
import com.gachon.i_edu.info.PostInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class SearchUserActivity extends BasicFunctions {

    //검색 결과
    private RecyclerView recyclerSearch;
    private UserListAdapter userListAdapter;
    private EditText editSearch;
    private ArrayList<MemberInfo> userList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout noSearch;
    private boolean updating;
    private String search_data;
    private String title;

    //공통
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        swipeRefreshLayout = findViewById(R.id.layout_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(search_data != null)
                    search(search_data);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //데이터베이스
        firebaseFirestore = FirebaseFirestore.getInstance();

        //어댑터
        userListAdapter = new UserListAdapter(this, userList);
        userListAdapter.setHasStableIds(true);

        //리사이클러 뷰 초기화
        recyclerSearch = findViewById(R.id.recycler_search);
        recyclerSearch.setAdapter(userListAdapter);
        //recyclerView.setItemViewCacheSize(100);
        recyclerSearch.setHasFixedSize(true);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearch.setAdapter(userListAdapter);

        noSearch = findViewById(R.id.layout_no_search);
        ImageView imageBack = findViewById(R.id.btn_back);
        imageBack.setOnClickListener(onClickListener);
        editSearch = findViewById(R.id.edit_search);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @SuppressLint("ResourceAsColor")
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editSearch.getText().toString();
                if(!text.equals("") || editSearch.getVisibility() == View.VISIBLE){
                    editSearch.setBackground(getDrawable(R.drawable.square_edit_text));
                }else{
                    editSearch.setBackground(getDrawable(R.drawable.square_edit_text_focus));
                }
            }
        });
        editSearch.setImeOptions(EditorInfo.IME_ACTION_DONE); //키보드 다음 버튼을 완료 버튼으로 바꿔줌
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                search_data = v.getText().toString();
                userList.clear();
                if(actionId == EditorInfo.IME_ACTION_DONE && !search_data.equals(""))
                {
                    noSearch.setVisibility(View.GONE);
                    customProgressDialog.show();
                    //화면터치 방지
                    customProgressDialog.setCanceledOnTouchOutside(false);
                    //뒤로가기 방지
                    customProgressDialog.setCancelable(false);
                    search(search_data);
                    return true;
                }else{
                    Toast.makeText(getApplicationContext(),"검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    private void search(String search_data){
        //문서 가져오기
        CollectionReference collectionReference = firebaseFirestore.collection("users");
        collectionReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        userList.clear();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String name = document.getData().get("name").toString();
                                if(name.equals(search_data)) {
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
                            }
                            if(userList.size() == 0){
                                noSearch.setVisibility(View.VISIBLE);
                            }else noSearch.setVisibility(View.GONE);
                            //리사이클러 뷰 초기화
                            userListAdapter.notifyDataSetChanged();
                        }
                        customProgressDialog.cancel();
                        updating = false;
                    }
                });
    }

    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.btn_back:
                finish();
                break;
        }
    };
}