package com.gachon.i_edu.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.LoginActivity;
import com.gachon.i_edu.activity.MyPageActivity;
import com.gachon.i_edu.activity.UserInfoSettingActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.Executor;

public class MyPageFragment extends Fragment {

    private View view;
    private Button et_logout, et_setting, et_my_page;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount gsa;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private ImageView userProfile;
    private TextView userName, userEmail, userLevel, userSolve, userQuestion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Firebase);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_my_page, container, false);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(),gso);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        et_logout = view.findViewById(R.id.btn_logout);
        et_logout.setOnClickListener(onClickListener);
        userProfile = view.findViewById(R.id.image_profile);
        userName = view.findViewById(R.id.text_name);
        userEmail = view.findViewById(R.id.text_email);
        userLevel = view.findViewById(R.id.text_level);
        userSolve = view.findViewById(R.id.text_solve);
        userQuestion = view.findViewById(R.id.text_question);
        et_setting = view.findViewById(R.id.btn_setting_user);
        et_setting.setOnClickListener(onClickListener);
        et_my_page = view.findViewById(R.id.btn_my_page);
        et_my_page.setOnClickListener(onClickListener);

        getUserData();

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        getUserData();
    }

    View.OnClickListener onClickListener = (v) ->{
        switch(v.getId()){
            case R.id.btn_logout:
                //파이어베이스 로그아웃
                mAuth.signOut();
                Toast.makeText(getActivity(),"로그아웃",Toast.LENGTH_SHORT);
                // 로그인 화면으로 이동
                myStartActivity(LoginActivity.class);
                break;
            case R.id.btn_setting_user:
                myStartActivity(UserInfoSettingActivity.class);
                break;
            case R.id.btn_my_page:
                myStartActivity(MyPageActivity.class);
                break;
        }
    };
    public void getUserData(){
        //문서 가져오기
        documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getData().get("photoUri") != null)
                    Glide.with(getActivity()).load(documentSnapshot.getData().get("photoUri").toString()).into(userProfile);
                userName.setText(documentSnapshot.getData().get("name").toString());
                userEmail.setText(user.getEmail());
                userLevel.setText(documentSnapshot.getData().get("level").toString());
                userSolve.setText(documentSnapshot.getData().get("solve").toString());
                userQuestion.setText(documentSnapshot.getData().get("question").toString());
            }
        });
    }

    private void myStartActivity(Class c){
        Intent intent = new Intent(getActivity(), c);
        startActivity(intent);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener((Executor) this, task -> {
                    mAuth.signOut();
                });
        gsa = null;
    }
}