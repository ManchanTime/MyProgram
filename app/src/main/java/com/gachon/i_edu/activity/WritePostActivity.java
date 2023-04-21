package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.WriteInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class WritePostActivity extends AppCompatActivity {

    private Button MComplete, MVideo, MImage, mBtnPicture, mBtnGallery;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private final ArrayList<Uri> pathList = new ArrayList<>();
    private LinearLayout parent;
    private int successCount;
    private int pathCount;
    private Boolean flag = false;
    private Uri uri;
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        MComplete = findViewById(R.id.btn_upload);
        MVideo = findViewById(R.id.btn_video);
        MImage = findViewById(R.id.btn_image);
        mBtnPicture = findViewById(R.id.btn_picture);
        mBtnGallery = findViewById(R.id.btn_gallery);
        parent = findViewById(R.id.contentsLayout);

        MComplete.setOnClickListener(onClickListener);
        MVideo.setOnClickListener(onClickListener);
        MImage.setOnClickListener(onClickListener);

        mBtnGallery.setOnClickListener(onClickListener);

        mBtnPicture.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.btn_upload:
                storageUpload();
                break;
            case R.id.btn_image:
                CardView cardView = findViewById(R.id.buttons_card_view);
                if (cardView.getVisibility() == View.VISIBLE) {
                    cardView.setVisibility(View.GONE);
                } else {
                    cardView.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_gallery:
                goGallery();
                break;
            case R.id.btn_picture:
                goCamera();
                break;
            case R.id.btn_video:

                break;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_GALLERY):
                    try {
                        uri = data.getData();
                    } catch (Exception e) {};
                    break;
                case (REQUEST_CAMERA):
                    break;
            }
            String imagePath = uri.toString();
            pathList.add(uri);

            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            ImageView imageView = new ImageView(WritePostActivity.this);
            imageView.setLayoutParams(layoutParams);
            Glide.with(this).load(imagePath).override(1000).into(imageView);
            parent.addView(imageView);

            EditText editText = new EditText(WritePostActivity.this);
            editText.setLayoutParams(layoutParams);
            editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
            parent.addView(editText);
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

    public void storageUpload() {
        final String publisher;
        final String title = ((EditText) (findViewById(R.id.edit_subject))).getText().toString();

        if (title.length() > 0) {
            final ArrayList<String> contentsList = new ArrayList<>();
            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();
            assert user != null;
            publisher = user.getUid();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            for (int i = 0; i < parent.getChildCount(); i++) {
                View view = parent.getChildAt(i);
                if (view instanceof EditText) {
                    String text = ((EditText) view).getText().toString();
                    if (text.length() > 0) {
                        contentsList.add(text);
                    }
                    if (parent.getChildCount() == 1) {
                        if(((EditText) view).getText().toString().length() == 0) {
                            startToast("내용을 입력해주세요.");
                        }else{
                            WriteInfo writeInfo = new WriteInfo(publisher, title, contentsList, new Date());
                            storeUpload(writeInfo);
                        }
                        return;
                    }
                } else {
                    contentsList.add(pathList.get(pathCount).toString());
                    final StorageReference mountainImagesRef = storageReference.child("users/" + user.getUid() + "/" + pathCount + ".jpg");
                    try {
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setCustomMetadata("index", "" + (contentsList.size() - 1)).build();
                        //Uri imageUri = Uri.parse(pathList.get(pathCount));
                        UploadTask uploadTask = mountainImagesRef.putFile(pathList.get(pathCount), metadata);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
                                mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        contentsList.set(index, uri.toString());
                                        successCount++;
                                        if (pathList.size() == successCount) {
                                            WriteInfo writeInfo = new WriteInfo(publisher, title, contentsList, new Date());
                                            storeUpload(writeInfo);
                                        }
                                    }
                                });
                            }
                        });
                    } catch (Exception e) {
                        Log.d("로그", "에러 : " + e.toString());
                    }
                    pathCount++;
                }
            }
        }else{
            startToast("제목을 입력해주세요.");
        }
    }

    private void storeUpload(WriteInfo writeInfo){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").add(writeInfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        startToast("업로드 완료");
                        Log.e("게시물","등록완료");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        startToast("업로드 실패");
                    }
                });
    }

    private void mystartActivity(Class c){
        Intent intent = new Intent(WritePostActivity.this, c);
        startActivityForResult(intent, 0);
    }

    private void mystartActivity(Class c, String media){
        Intent intent = new Intent(WritePostActivity.this, c);
        intent.putExtra("media",media);
        startActivityForResult(intent, 0);
    }

    public void startToast(String content){
        Toast.makeText(this,content,Toast.LENGTH_LONG).show();
    }
}