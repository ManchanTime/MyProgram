package com.gachon.i_edu.activity;



import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.regex.Pattern;

public class RegisterActivity extends BasicFunctions {
    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스
    private EditText mEtEmail, mEtPwd, mEtCheck;
    private Button mBtnRegister;
    private ProgressDialog customProgressDialog;

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

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void signUp() {
        //회원가입 처리 시작
        String strEmail = mEtEmail.getText().toString();
        String e_mailPattern = "^[a-zA-z0-9]+@[a-zA-z0-9.]+$";
        String strPwd = mEtPwd.getText().toString();
        String strCheck = mEtCheck.getText().toString();
        StringBuilder strsubP1 = new StringBuilder(strPwd);
        if (!Pattern.matches(e_mailPattern, strEmail)) {
            Toast.makeText(RegisterActivity.this, "이메일 형식을 확인해주세요.", Toast.LENGTH_SHORT).show();
        } else if (strsubP1.length() < 8) {
            Toast.makeText(RegisterActivity.this, "비밀번호는 최소 8자리 이상입니다.", Toast.LENGTH_SHORT).show();
        } else if (strPwd.equals("") || strCheck.equals("")) {
            Toast.makeText(RegisterActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if (!strPwd.equals(strCheck)) {
            Toast.makeText(RegisterActivity.this, "비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
        } else {
            customProgressDialog.show();
            //화면터치 방지
            customProgressDialog.setCanceledOnTouchOutside(false);
            //뒤로가기 방지
            customProgressDialog.setCancelable(false);
            //Firebase Auth 진행
            mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPwd)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                assert user != null;
                                customProgressDialog.cancel();
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> sendTask) {
                                        if (sendTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "이메일을 전송했습니다.",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "이메일 전송 실패", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                customProgressDialog.cancel();
                                // If sign in fails, display a message to the user.
                                Toast.makeText(RegisterActivity.this, "회원가입 실패",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    @Override
    public void onBackPressed() {
        finish();
    }
}