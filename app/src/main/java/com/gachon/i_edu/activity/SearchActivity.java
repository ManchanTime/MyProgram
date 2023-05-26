package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.PostListAdapter;
import com.gachon.i_edu.adpater.ReplyAdapter;
import com.gachon.i_edu.adpater.UserPostAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.PostInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class SearchActivity extends AppCompatActivity {

    //검색 결과
    private RecyclerView recyclerSearch;
    private PostListAdapter postListAdapter;
    private ImageView imageBack;
    private EditText editSearch;
    private ArrayList<PostInfo> postList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout noSearch;
    private boolean updating;
    private String search_data;

    //공통
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private ProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        swipeRefreshLayout = findViewById(R.id.layout_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //데이터베이스
        firebaseFirestore = FirebaseFirestore.getInstance();

        //어댑터
        postListAdapter = new PostListAdapter(this, postList);
        postListAdapter.setHasStableIds(true);

        //리사이클러 뷰 초기화
        recyclerSearch = findViewById(R.id.recycler_search);
        recyclerSearch.setAdapter(postListAdapter);
        //recyclerView.setItemViewCacheSize(100);
        recyclerSearch.setHasFixedSize(true);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearch.setAdapter(postListAdapter);
        recyclerSearch.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    search(search_data);
                }
            }
        });

        noSearch = findViewById(R.id.layout_no_search);
        imageBack = findViewById(R.id.btn_back);
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
                postList.clear();
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
        updating = true;
        Date date = postList.size() == 0 ? new Date() : postList.get(postList.size()-1).getCreatedAt();
        collectionReference = firebaseFirestore.collection("posts");
        collectionReference.whereLessThan("createdAt", date)
                .limit(5)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String title = document.getData().get("title").toString();
                                ArrayList<String> main_content = (ArrayList<String>)document.getData().get("main_content");
                                ArrayList<String> sub_content = (ArrayList<String>)document.getData().get("sub_content");
                                String title_content = main_content.get(0);
                                boolean check = false;
                                int size = sub_content.size();
                                for(int i=0;i<size;i++){
                                    if(!Patterns.WEB_URL.matcher(sub_content.get(i)).matches() && sub_content.get(i).contains(search_data)){
                                        check = true;
                                        break;
                                    }
                                }
                                if(title.contains(search_data) || title_content.contains(search_data) || check) {
                                    postList.add(new PostInfo(
                                            document.getData().get("id").toString(),
                                            document.getData().get("publisher").toString(),
                                            document.getData().get("field").toString(),
                                            document.getData().get("subject").toString(),
                                            title,
                                            main_content,
                                            sub_content,
                                            new Date(document.getDate("createdAt").getTime()),
                                            Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                            Long.valueOf(String.valueOf(document.getData().get("reply_count")))
                                            )
                                    );
                                }
                            }
                            if(postList.size() == 0){
                                noSearch.setVisibility(View.VISIBLE);
                            }
                            //리사이클러 뷰 초기화
                            postListAdapter.notifyDataSetChanged();
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