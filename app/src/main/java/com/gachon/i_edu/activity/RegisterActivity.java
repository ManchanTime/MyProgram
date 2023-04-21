package com.gachon.i_edu.activity;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gachon.i_edu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스
    private EditText mEtEmail, mEtPwd, mEtCheck;
    private Button mBtnRegister;
    private int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mEtEmail = findViewById(R.id.et_email);
        mEtPwd = findViewById(R.id.et_pwd);
        mBtnRegister = findViewById(R.id.btn_register);
        mEtCheck = findViewById(R.id.et_check);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }
    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
    }

    private void signUp(){
        //회원가입 처리 시작
        String strEmail = mEtEmail.getText().toString();
        String e_mailPattern = "^[a-zA-z0-9]+@[a-zA-z0-9.]+$";
        String strPwd = mEtPwd.getText().toString();
        String strCheck = mEtCheck.getText().toString();
        if(!Pattern.matches(e_mailPattern, strEmail)){
            Toast.makeText(RegisterActivity.this,"이메일 형식을 확인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder strbuP1 = new StringBuilder(strPwd);

        if(strbuP1.length() < 8){
            Toast.makeText(RegisterActivity.this,"비밀번호는 최소 8자리 이상입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(strPwd.equals("") || strCheck.equals("")){
            Toast.makeText(RegisterActivity.this,"비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!strPwd.equals(strCheck)){
            Toast.makeText(RegisterActivity.this,"비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Firebase Auth 진행
        mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPwd)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            assert user != null;
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> sendTask) {
                                    if(sendTask.isSuccessful()){
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        Toast.makeText(RegisterActivity.this, "이메일을 전송했습니다.",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Toast.makeText(RegisterActivity.this,"이메일 전송 실패",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "회원가입 실패",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}