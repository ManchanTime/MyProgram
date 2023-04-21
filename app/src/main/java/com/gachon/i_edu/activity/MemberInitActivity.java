package com.gachon.i_edu.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.MemberInfo;
import com.gachon.i_edu.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MemberInitActivity extends AppCompatActivity {

    private final long finishtimeed = 1000;
    private long presstime = 0;
    private Button mBtnComplete, mBtnPicture, mBtnGallery;
    private ImageView mImageProfile;
    private String profilePath;
    private Uri uri;
    private static final String TAG = "MemberInitActivity";
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);
        mBtnComplete = (Button) findViewById(R.id.btn_com);
        mImageProfile = findViewById(R.id.image_profile);
        mBtnPicture = findViewById(R.id.btn_picture);
        mBtnGallery = findViewById(R.id.btn_gallery);


        mImageProfile.setOnClickListener(onClickListener);

        mBtnComplete.setOnClickListener(onClickListener);

        mBtnGallery.setOnClickListener(onClickListener);

        mBtnPicture.setOnClickListener(onClickListener);

    }

    @SuppressLint("NonConstantResourceId")
    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.btn_com:
                profileUpdate();
                break;
            case R.id.image_profile:
                CardView cardView = findViewById(R.id.buttons_card_view);
                if(cardView.getVisibility() == View.VISIBLE){
                    cardView.setVisibility(View.GONE);
                }
                else {
                    cardView.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_picture:
                goCamera();
                break;
            case R.id.btn_gallery:
                goGallery();
                break;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case (REQUEST_GALLERY):
                if(resultCode == Activity.RESULT_OK) {
                    try {
                        uri = data.getData();
                        Log.e("mI",uri.toString());
                        Glide.with(this).load(uri).centerCrop().override(300).into(mImageProfile);
                    }catch(Exception e){};
                }
                break;
            case (REQUEST_CAMERA):
                if(resultCode == Activity.RESULT_OK) {
                    try {
                        Glide.with(this).load(uri).centerCrop().override(300).into(mImageProfile);
                    }catch(Exception e){};
                }
                break;
        }
    }

    //갤러리 intent
    public void goGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    //카메라 intent
    public void goCamera(){
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

    public void profileUpdate() {
        final String name = ((EditText) (findViewById(R.id.edit_name))).getText().toString();
        final String phone_number = ((EditText) (findViewById(R.id.edit_phone))).getText().toString();
        final String birthday = ((EditText) (findViewById(R.id.edit_birthday))).getText().toString();
        final String address = ((EditText) (findViewById(R.id.edit_address))).getText().toString();
        if (name.length() > 0 && phone_number.length() > 9 && birthday.length() > 5 && address.length() > 0) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            final StorageReference mountainImageRef = storageReference.child("users/" + user.getUid() + "/profileImage.jpg");
            if (uri == null) {
                MemberInfo memberInfo = new MemberInfo(name, phone_number, birthday, address);
                uploader(memberInfo);
            } else {
                UploadTask uploadTask = mountainImageRef.putFile(uri);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return mountainImageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Log.e("image",downloadUri.toString());
                            MemberInfo memberInfo = new MemberInfo(downloadUri.toString(), name, phone_number, birthday, address);
                            uploader(memberInfo);
                        } else {
                            startToast("회원정보 저장에 실패했습니다.");
                        }
                    }
                });
            }
        } else {
            startToast("회원정보를 확인해주세요.");
        }
    }
    private void uploader(MemberInfo memberInfo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).set(memberInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startToast("회원정보 등록에 성공하였습니다.");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        startToast("회원정보 등록에 실패하였습니다.");
                    }
                });
    }

    private void startToast(String msg){
        Toast.makeText(MemberInitActivity.this,msg,Toast.LENGTH_SHORT).show();
    }
    private void mystartActivity(Class c){
        Intent intent = new Intent(MemberInitActivity.this, c);
        startActivityForResult(intent, 0);
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