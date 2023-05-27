package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.gachon.i_edu.R;
import com.gachon.i_edu.service.FirebaseMessagingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(2000); // splash 화면 대기 시간
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        startActivity(new Intent(this, LoginActivity.class)); // splash 화면이 끝난 뒤 띄울 Activity
        finish();
    }
}