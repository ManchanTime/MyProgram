package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.gachon.i_edu.info.MemberInfo;
import com.gachon.i_edu.info.PostInfo;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class UserInfoSettingActivity extends BasicFunctions {

    private TextView text_Image, text_image_change;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount gsa;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private ImageView userProfile;
    private TextView userName, userEmail, userSchool, userGoal, userBirthDay, userId;
    private ImageView image_back;
    private Bundle bundle;
    private String main;
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private Uri uri;
    private RelativeLayout layout_image_change;
    private Button btn_gallery, btn_picture, btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_setting);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userProfile = findViewById(R.id.image_profile);
        userName = findViewById(R.id.text_name);
        userEmail = findViewById(R.id.text_email);
        userSchool = findViewById(R.id.text_university);
        userGoal = findViewById(R.id.text_goal);
        userBirthDay = findViewById(R.id.text_birthday);
        userId = findViewById(R.id.text_id);
        image_back = findViewById(R.id.image_back);

        btn_gallery = findViewById(R.id.btn_gallery);
        btn_picture = findViewById(R.id.btn_picture);
        btn_cancel = findViewById(R.id.btn_cancel);
        layout_image_change = findViewById(R.id.buttons_choose);
        text_image_change = findViewById(R.id.text_profile);
        text_image_change.setOnClickListener(onClickListener);
        userProfile.setOnClickListener(onClickListener);
        btn_gallery.setOnClickListener(onClickListener);
        btn_picture.setOnClickListener(onClickListener);
        btn_cancel.setOnClickListener(onClickListener);

        userName.setOnClickListener(onClickListener);
        userSchool.setOnClickListener(onClickListener);
        userGoal.setOnClickListener(onClickListener);
        image_back.setOnClickListener(onClickListener);


        bundle = new Bundle();
        getUserData();
    }

    @Override
    public void onResume(){
        super.onResume();
        getUserData();
    }

    @SuppressLint("NonConstantResourceId")
    View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.text_name:
                main = "name";
                String name = userName.getText().toString();
                bundle.putString("title",main);
                bundle.putString("sub", name);
                startIntent(ChangeActivity.class, bundle);
                break;
            case R.id.text_university:
                main = "school";
                String school = userSchool.getText().toString();
                bundle.putString("title",main);
                bundle.putString("sub", school);
                startIntent(ChangeActivity.class, bundle);
                break;
            case R.id.text_goal:
                main = "goal";
                String goal = userGoal.getText().toString();
                bundle.putString("title",main);
                bundle.putString("sub", goal);
                startIntent(ChangeActivity.class, bundle);
                break;
            case R.id.image_back:
                finish();
                break;
            case R.id.image_profile:
            case R.id.text_profile:
                if (layout_image_change.getVisibility() == View.VISIBLE) {
                    layout_image_change.setVisibility(View.GONE);
                } else {
                    layout_image_change.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_gallery:
                goGallery();
                layout_image_change.setVisibility(View.GONE);
                break;
            case R.id.btn_picture:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    goCamera();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                }
                layout_image_change.setVisibility(View.GONE);
                break;
            case R.id.btn_cancel:
                layout_image_change.setVisibility(View.GONE);
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_GALLERY):
                    try {
                        uri = data.getData();
                    } catch (Exception e) {
                    }
                case (REQUEST_CAMERA):
                    try {
                        layout_image_change.setVisibility(View.GONE);
                        Glide.with(this).load(uri).into(userProfile);
                    } catch (Exception e) {
                    }
                    break;
            }
            setProfileData();
    }
    }

    public void setProfileData(){
        //이미지 교체
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        final StorageReference mountainImageRef = storageReference.child("users/" + user.getUid() + "/profileImage.jpg");
        UploadTask uploadTask = mountainImageRef.putFile(uri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return mountainImageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Log.e("tlqkf",task.getResult().toString());
                    firebaseFirestore = FirebaseFirestore.getInstance();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    documentReference = firebaseFirestore.collection("users").document(user.getUid());
                    documentReference.update("photoUri", task.getResult()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
                }
            }
        });

    }

    public void getUserData(){
        //문서 가져오기
        documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.getData().get("photoUri") != null)
                    Glide.with(getApplicationContext()).load(documentSnapshot.getData().get("photoUri").toString()).into(userProfile);
                userName.setText(documentSnapshot.getData().get("name").toString());
                userEmail.setText(user.getEmail());
                userBirthDay.setText(documentSnapshot.getData().get("birthday").toString());
                userSchool.setText(documentSnapshot.getData().get("school").toString());
                userGoal.setText(documentSnapshot.getData().get("goal").toString());
                userId.setText(user.getUid());
            }
        });
    }

    //갤러리 intent
    public void goGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    //카메라 intent
    public void goCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
        }
        // 사진을 저장하고 이미지뷰에 출력
        if (photoFile != null) {
            uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private File createImageFile() throws IOException {
        // 파일이름을 세팅 및 저장경로 세팅
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    public void startIntent(Class c, Bundle bundle){
        Intent intent = new Intent(this, c);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}