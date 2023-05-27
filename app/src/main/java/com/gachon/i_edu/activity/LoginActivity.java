package com.gachon.i_edu.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;


public class LoginActivity extends BasicFunctions {
    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스
    private EditText mEtEmail, mEtPwd, mEtCheck;
    private Button mBtnRegister, mBtnLogin, mBtnFind; //구글 로그인
    private GoogleSignInClient mGoogleSignInClient; //구글 로그인 객체
    private GoogleSignInAccount gsa;
    private ImageView kImage, mGoogle;
    private static final int RC_SIGN_IN = 9001;
    private ProgressDialog customProgressDialog;
    private DocumentSnapshot document;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mEtEmail = findViewById(R.id.et_email);
        mEtPwd = findViewById(R.id.et_pwd);
        mBtnRegister = findViewById(R.id.btn_register);
        mBtnLogin = findViewById(R.id.btn_login);
        mBtnFind = findViewById(R.id.btn_findPassword);
        kImage = findViewById(R.id.image_kakao);
        mGoogle = findViewById(R.id.google);

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        mGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customProgressDialog.show();
                gsa = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);

                if (gsa != null) { // 로그인 돼있는 경우
                    //로그인 성공
                    Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                    checkInit();
                    customProgressDialog.cancel();
                }
                else
                    signIn();
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();

                if (strEmail.length() == 0 || strPwd.length() == 0) {
                    Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    customProgressDialog.show();
                    //화면터치 방지
                    customProgressDialog.setCanceledOnTouchOutside(false);
                    //뒤로가기 방지
                    customProgressDialog.setCancelable(false);
                    mFirebaseAuth.signInWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(
                            LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                        if(user.isEmailVerified()) {
                                            //로그인 성공
                                            Toast.makeText(LoginActivity.this, "환영합니다!!", Toast.LENGTH_SHORT).show();
                                            checkInit();

                                        }else{
                                            Toast.makeText(LoginActivity.this, "이메일 인증을 해주세요.", Toast.LENGTH_SHORT).show();
                                            customProgressDialog.cancel();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                                        customProgressDialog.cancel();
                                    }
                                }
                            });
                }
            }
        });
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mBtnFind.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(LoginActivity.this, PasswordResetActivity.class);
                startActivity(intent);
            }
        });

        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                if (oAuthToken != null) {
                    Log.i("user", oAuthToken.getAccessToken() + " " + oAuthToken.getRefreshToken());
                }
                if (throwable != null) {
                    // TBD
                    Log.w("gachon", "invoke: " + throwable.getLocalizedMessage());
                }
                updateKakaoLoginUi();
                return null;
            }
        };

        kImage.setOnClickListener(new View.OnClickListener() {   // 로그인 버튼 클릭 시
            @Override
            public void onClick(View v) {
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) {
                    // 카카오톡이 있을 경우?
                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);
                } else {
                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
                }
            }
        });
        updateKakaoLoginUi();
    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

            if (acct != null) {
                firebaseAuthWithGoogle(acct.getIdToken());
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        checkInit();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void updateKakaoLoginUi(){
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null) {
                    // 유저 정보가 정상 전달 되었을 경우
                    Log.i("gachon", "id " + user.getId());   // 유저의 고유 아이디를 불러옵니다.
                    Log.i("gachon", "invoke: nickname=" + user.getKakaoAccount().getProfile().getNickname());
                    // 유저의 닉네임을 불러옵니다.
                    Log.i("gachon", "userimage " + user.getKakaoAccount().getProfile().getProfileImageUrl());
                    // 유저의 이미지 URL을 불러옵니다.
                    // 이 부분에는 로그인이 정상적으로 되었을 경우 어떤 일을 수행할 지 적으면 됩니다.
                    checkInit();
                }
                else{
                    // 로그인 시 오류 났을 때
                    // 키해시가 등록 안 되어 있으면 오류 납니다.
                    Log.w("gachon", "invoke: " + throwable.getLocalizedMessage());
                }
                return null;
            }
        });
    }

    private void checkInit(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firebaseFirestore;
        DocumentReference docRef;

        if(user == null){
        }else {
            //로그인 됨
            firebaseFirestore = FirebaseFirestore.getInstance();
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    String fcmToken = task.getResult();
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
                    documentReference.update("fcmToken", fcmToken);
                }
            });
            docRef = firebaseFirestore.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        document = task.getResult();
                        if(document != null) {
                            if (!document.exists()) {
                                myStartActivity(MemberInitActivity.class);
                            }else{
                                myStartActivity(MainActivity.class);
                            }
                            customProgressDialog.cancel();
                            finish();
                        }
                    }else{
                        customProgressDialog.cancel();
                    }
                }
            });
        }
    }

    public void myStartActivity(Class c){
        Intent intent = new Intent(getApplicationContext(), c);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        builder.setTitle("종료할까요?"); // 다이얼로그 제목
        builder.setCancelable(false);   // 다이얼로그 화면 밖 터치 방지
        builder.setPositiveButton("예", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                exit();
            }
        });

        builder.setNegativeButton("아니요", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setNeutralButton("취소", new AlertDialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show(); // 다이얼로그 보이기
    }
}