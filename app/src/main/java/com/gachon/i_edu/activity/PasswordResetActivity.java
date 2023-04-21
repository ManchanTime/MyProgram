package com.gachon.i_edu.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gachon.i_edu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button mBtnSend, mBtnComplete;
    private EditText mTextEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mAuth = FirebaseAuth.getInstance();
        mBtnSend = findViewById(R.id.btn_com);
        mBtnComplete = findViewById(R.id.btn_complete);

        mBtnComplete.setOnClickListener(onClickListener);
        mBtnSend.setOnClickListener(onClickListener);
    }
    View.OnClickListener onClickListener = (view) -> {
        switch (view.getId()){
            case R.id.btn_com:
                send();
                break;
            case R.id.btn_complete:
                goback();
                break;
        }
    };

    private void goback(){
        Intent intent = new Intent(PasswordResetActivity.this,LoginActivity.class);
        startActivity(intent);
    }
    private void send(){
        String email = ((EditText)findViewById(R.id.edit_name)).getText().toString();

        if(email.length() > 0){
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(PasswordResetActivity.this,"이메일을 보냈습니다.",Toast.LENGTH_SHORT).show();
                        Log.d("Reset","Email sent.");
                    }
                }
            });
        }else{
            Toast.makeText(this,"이메일을 입력해주세요.",Toast.LENGTH_SHORT).show();
        }
    }
}