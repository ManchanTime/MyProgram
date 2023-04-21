package com.gachon.i_edu.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gachon.i_edu.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private final long finishtimeed = 1000;
    private long presstime = 0;
    private Button et_logout;
    private FloatingActionButton et_board;
    private TextView et_text;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount gsa;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        et_logout = findViewById(R.id.btn_logout);
        et_board = findViewById(R.id.btn_board);
        FirebaseUser user = mAuth.getCurrentUser();

        if(user == null){
            myStartActivity(LoginActivity.class);
            finish();
        }else {
            //로그인 됨
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document != null) {
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapShot data : " + document.getData());
                            } else {
                                Log.d(TAG,"No document", task.getException());
                                myStartActivity(MemberInitActivity.class);
                            }
                        }
                    }else{
                        Log.d(TAG,"get failed with", task.getException());
                    }
                }
            });
        }

        et_logout.setOnClickListener(onClickListener);
        et_board.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = (v) ->{
        switch(v.getId()){
            case R.id.btn_logout:
                //파이어베이스 로그아웃
                mAuth.signOut();
                signOut();
                Toast.makeText(MainActivity.this,"로그아웃",Toast.LENGTH_SHORT);
                // 로그인 화면으로 이동
                myStartActivity(LoginActivity.class);
                finish();
                break;
            case R.id.btn_board:
                myStartActivity(WritePostActivity.class);
                break;
        }
    };

    private void myStartActivity(Class c){
        Intent intent = new Intent(MainActivity.this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    mAuth.signOut();
                });
        gsa = null;
    }
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - presstime;

        if (0 <= intervalTime && finishtimeed >= intervalTime)
        {
            finishAffinity();
            System.runFinalization();
            System.exit(0);
        }
        else
        {
            presstime = tempTime;
            Toast.makeText(getApplicationContext(), "한번 더 누르시면 앱이 종료됩니다", Toast.LENGTH_SHORT).show();
        }
    }

}