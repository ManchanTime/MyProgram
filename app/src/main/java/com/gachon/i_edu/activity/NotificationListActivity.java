package com.gachon.i_edu.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.NotificationAdapter;
import com.gachon.i_edu.info.NotificationInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class NotificationListActivity extends BasicFunctions {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private ArrayList<NotificationInfo> notificationList = new ArrayList<>();
    private ImageView btnBack, btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        btnBack = findViewById(R.id.btn_back);
        btnClear = findViewById(R.id.btn_clear);
        btnBack.setOnClickListener(onClickListener);
        btnClear.setOnClickListener(onClickListener);

        Intent getIntent = getIntent();
        if(getIntent != null){
            notificationList = (ArrayList<NotificationInfo>) getIntent.getSerializableExtra("object");

            recyclerView = findViewById(R.id.recycler);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemViewCacheSize(100);
            notificationAdapter = new NotificationAdapter(this, notificationList);
            notificationAdapter.setHasStableIds(true);
            recyclerView.setAdapter(notificationAdapter);
        }
    }

    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.btn_clear:
                for(int i=0;i<notificationList.size();i++){
                    Log.e("test", notificationList.get(i).getId());
                    DocumentReference documentReference = FirebaseFirestore.getInstance()
                            .collection("replies").document(notificationList.get(i).getId());
                    documentReference.update("read", true);
                }
                notificationList.clear();
                notificationAdapter.notifyDataSetChanged();
                finish();
                break;
            case R.id.btn_back:
                finish();
                break;
        }
    };
}