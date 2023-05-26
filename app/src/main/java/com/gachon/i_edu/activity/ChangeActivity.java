package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChangeActivity extends BasicFunctions {

    private EditText edit_change;
    private TextView title, text_image_change;
    private Button btn_store;
    private ImageView profile;
    private RelativeLayout layout_image_change;
    private ImageView btn_back;
    private Bundle get;
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private String title_m;
    private String origin;
    private TextView textCompare;
    private boolean result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);

        title = findViewById(R.id.text_title);

        edit_change = findViewById(R.id.edit);
        btn_store = findViewById(R.id.btn_store);
        btn_store.setVisibility(View.VISIBLE);
        btn_back = findViewById(R.id.btn_back);
        textCompare = findViewById(R.id.text_compare);

        layout_image_change = findViewById(R.id.buttons_choose);
        text_image_change = findViewById(R.id.text_profile);
        profile = findViewById(R.id.image_profile);

        btn_store.setOnClickListener(onClickListener);
        btn_back.setOnClickListener(onClickListener);

        Intent intent = getIntent();
        if(intent != null){
            get = intent.getExtras();
            title_m = get.getString("title");
            edit_change.setText(get.getString("sub"));
            if(title_m.equals("name")){
                title.setText("닉네임");
            }else if(title_m.equals("school")){
                title.setText("학교 or 직장");
            }else{
                title.setText("목표");
            }
        }

        origin = edit_change.getText().toString();
        edit_change.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void afterTextChanged(Editable editable) {
                String text = edit_change.getText().toString();
                checkName(text);
                if(!text.equals("") && !text.equals(origin) && result){
                    btn_store.setTextColor(Color.BLACK);
                    btn_store.setEnabled(true);
                }else{
                    btn_store.setTextColor(R.color.text);
                    btn_store.setEnabled(false);
                }
            }
        });
    }


    @SuppressLint("NonConstantResourceId")
    View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_store:
                setStringData();
                finish();
                break;
        }
    };

    //문서 바꾸기
    public void setStringData(){
        String newData = edit_change.getText().toString();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.update(title_m, newData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ChangeActivity.this, "변경 완료", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkName(String name){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference;
        collectionReference = firebaseFirestore.collection("users");
        collectionReference.whereEqualTo("name", name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().size() == 0){
                                textCompare.setVisibility(View.VISIBLE);
                                result = true;
                            }
                            else{
                                textCompare.setVisibility(View.GONE);
                                result = false;
                            }
                        }
                    }
                });
    }
}