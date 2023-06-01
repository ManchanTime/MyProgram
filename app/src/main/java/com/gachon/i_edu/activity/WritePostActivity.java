package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.gachon.i_edu.info.PostInfo;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WritePostActivity extends BasicFunctions {

    private String name, uri_user, tier;
    private ImageView MComplete, mOver;
    private Button MImageMain, MImageSub, mBtnPicture, mBtnGallery, mRemove, mChange, mChangeGallery, mChangeCamera;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private final ArrayList<Uri> main_pathList = new ArrayList<>();
    private final ArrayList<Uri> sub_pathList = new ArrayList<>();
    private LinearLayout parent, mainImageLayout;
    private int main_successCount;
    private int sub_successCount=0;
    private int main_pathCount;
    private int sub_pathCount;
    private Uri uri;
    private ProgressDialog customProgressDialog;
    private RelativeLayout mRelativeLayout;
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_CHANGE_CAMERA = 2;
    private static final int REQUEST_CHANGE_GALLERY = 3;
    private static int flag = 0;
    private ImageView selectedImageView;
    private RelativeLayout mChoose;
    private RelativeLayout mChangeLayout;
    private RelativeLayout mChangeSubject;
    private static final int main_imageSize = 500;
    private static final int sub_imageSize = 400;
    private int count_mainImage;
    private ScrollView mContents;
    private int question_count;
    private FirebaseFirestore firebaseFirestore;
    private Bundle bundle;
    private PostInfo postInfo;
    private ArrayList<String> like_post;
    private String field, subject = "";
    private TextView textSubject;
    //test
    private int selectedUri;

    //과목선택
    private RelativeLayout mChooseSubject;
    private RadioGroup mRadioSubjects;
    private Button radioChoose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        bundle = new Bundle();

        firebaseFirestore = FirebaseFirestore.getInstance();
        MComplete = findViewById(R.id.image_upload);
        MImageMain = findViewById(R.id.btn_image_main);
        MImageSub = findViewById(R.id.btn_image_sub);
        mBtnPicture = findViewById(R.id.btn_picture);
        mBtnGallery = findViewById(R.id.btn_gallery);
        parent = findViewById(R.id.layout_main_contents);
        mRemove = findViewById(R.id.btn_imageRemove);
        mChange = findViewById(R.id.btn_imageEdit);
        mChoose = findViewById(R.id.buttons_choose);
        mChangeGallery = findViewById(R.id.btn_change_gallery);
        mChangeCamera = findViewById(R.id.btn_change_camera);
        mOver = findViewById(R.id.image_over);
        mContents = findViewById(R.id.contents);
        mainImageLayout = findViewById(R.id.layout_image_problem);
        mChangeSubject = findViewById(R.id.layout_change_subject);
        mChangeSubject.setOnClickListener(onClickListener);
        textSubject = findViewById(R.id.text_subject);

        mChooseSubject = findViewById(R.id.choose_subject);
        mRadioSubjects = findViewById(R.id.radio_group);
        radioChoose = findViewById(R.id.radio_choose);
        radioChoose.setOnClickListener(onClickListener);
        mRadioSubjects.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int radioId = mRadioSubjects.getCheckedRadioButtonId();
                switch(radioId){
                    case R.id.radio_math:
                        subject = "수학";
                        break;
                    case R.id.radio_korean:
                        subject = "국어";
                        break;
                    case R.id.radio_english:
                        subject = "영어";
                        break;
                    case R.id.radio_science:
                        subject = "과학";
                        break;
                    case R.id.radio_coding:
                        subject = "코딩";
                        break;
                    case R.id.radio_society:
                        subject = "사회";
                        break;
                    case R.id.radio_any:
                        subject = "기타";
                        break;
                }
                textSubject.setText(subject);
                radioChoose.setEnabled(true);
            }
        });

        MComplete.setOnClickListener(onClickListener);
        MImageMain.setOnClickListener(onClickListener);
        MImageSub.setOnClickListener(onClickListener);
        mOver.setOnClickListener(onClickListener);
        mBtnGallery.setOnClickListener(onClickListener);
        mBtnPicture.setOnClickListener(onClickListener);
        mRemove.setOnClickListener(onClickListener);
        mChange.setOnClickListener(onClickListener);
        mChangeCamera.setOnClickListener(onClickListener);
        mChangeGallery.setOnClickListener(onClickListener);
        mChoose.setOnClickListener(onClickListener);

        //유저 정보 받아오기
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        init();
        Intent intent = getIntent();
        if(intent != null){
            field = intent.getStringExtra("field");
            if(field.equals("소통")){
                Log.e("check","c");
                mChangeSubject.setVisibility(View.GONE);
            }
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                String get = bundle.getString("uri");
                if(get != null) {
                    uri = Uri.parse(get);
                    flag = 0;
                    settingMainData();
                }
            }
        }
        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mRelativeLayout = findViewById(R.id.buttons_backgroundLayout);
        mRelativeLayout.setOnClickListener(onClickListener);

        mChangeLayout = findViewById(R.id.buttons_choose_changeCG);
        mChangeLayout.setOnClickListener(onClickListener);

    }

    public void init(){
        DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name = documentSnapshot.getData().get("name").toString();
                tier = documentSnapshot.getData().get("level").toString();
                question_count = Integer.parseInt(documentSnapshot.getData().get("question").toString());
                like_post = (ArrayList<String>) documentSnapshot.getData().get("like_post");
                if(documentSnapshot.getData().get("photoUri") != null) {
                    uri_user = documentSnapshot.getData().get("photoUri").toString();
                    bundle.putString("uri",uri_user);
                }
                bundle.putString("name",name);
                bundle.putString("tier",tier);
                bundle.putString("uid",user.getUid());
            }
        });
    }

    View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.image_upload:
                storageUpload();
                break;
            case R.id.image_over:
                finish();
                break;
            case R.id.btn_image_main:
                if(count_mainImage > 3){
                    startToast("사진은 2장이상 추가할 수 없습니다.");
                }else {
                    flag = 0;
                    if (mChoose.getVisibility() == View.GONE) {
                        mChoose.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case R.id.btn_image_sub:
                flag = 1;
                if (mChoose.getVisibility() == View.GONE) {
                    mChoose.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_gallery:
                goGallery(REQUEST_GALLERY);
                break;
            case R.id.btn_picture:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    goCamera(REQUEST_CAMERA);
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                }
                break;
            case R.id.buttons_choose:
                if(mChoose.getVisibility() == View.VISIBLE)
                    mChoose.setVisibility(View.GONE);
                break;
            case R.id.buttons_backgroundLayout:
                if(mRelativeLayout.getVisibility() == View.VISIBLE){
                    mRelativeLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_imageEdit:
                mChangeLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_imageRemove:
                mRelativeLayout.setVisibility(View.GONE);
                if(flag == 0){
                    count_mainImage--;
                    mainImageLayout.removeView((View)selectedImageView);
                }else{
                    parent.removeView((View)selectedImageView.getParent());
                }
                mRelativeLayout.setVisibility(View.GONE);
                break;
            case R.id.buttons_choose_changeCG:
                if(mChangeLayout.getVisibility() == View.VISIBLE){
                    mChangeLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_change_camera:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    goCamera(REQUEST_CHANGE_CAMERA);
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                }
                break;
            case R.id.btn_change_gallery:
                goGallery(REQUEST_CHANGE_GALLERY);
                break;
            case R.id.radio_choose:
                mChooseSubject.setVisibility(View.GONE);
                break;
            case R.id.layout_change_subject:
                mChooseSubject.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_GALLERY):
                    uri = data.getData();
                case (REQUEST_CAMERA):
                    if(flag == 0){
                        settingMainData();
                    }else{
                        settingSubData();
                    }
                    //settingData();
                    mChoose.setVisibility(View.GONE);
                    mContents.post(new Runnable() {
                        @Override
                        public void run() {
                            mContents.fullScroll(ScrollView.FOCUS_DOWN);
                            //scrollView.fullScroll(ScrollView.FOCUS_UP);
                        }
                    });
                    break;
                case (REQUEST_CHANGE_GALLERY):
                    mChoose.setVisibility(View.GONE);
                    mRelativeLayout.setVisibility(View.GONE);
                    mChangeLayout.setVisibility(View.GONE);
                    uri = data.getData();
                    String imagePath = uri.toString();
                    if(flag == 0) {
                        main_pathList.set(selectedUri, uri);
                        Glide.with(this).load(imagePath).override(main_imageSize).into(selectedImageView);
                    }
                    else {
                        sub_pathList.set(selectedUri,uri);
                        Glide.with(this).load(imagePath).override(sub_imageSize).into(selectedImageView);
                    }
                    break;
                case(REQUEST_CHANGE_CAMERA):
                    mChoose.setVisibility(View.GONE);
                    mRelativeLayout.setVisibility(View.GONE);
                    mChangeLayout.setVisibility(View.GONE);
                    if(flag == 0) {
                        main_pathList.set(selectedUri, uri);
                        Glide.with(this).load(uri.toString()).override(main_imageSize).into(selectedImageView);
                    }
                    else {
                        sub_pathList.set(selectedUri,uri);
                        Glide.with(this).load(uri.toString()).override(sub_imageSize).into(selectedImageView);
                    }
                    break;
            }

        }
    }

    //게시글 메인 데이터 생성
    public void settingMainData(){
        String imagePath = uri.toString();
        main_pathList.add(uri);

        ImageView mainImage = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight=1;
        mainImage.setLayoutParams(params);

        Glide.with(this).load(imagePath).override(main_imageSize).into(mainImage);
        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRelativeLayout.setVisibility(View.VISIBLE);
                selectedImageView = (ImageView) view;
                selectedUri = mainImageLayout.indexOfChild(selectedImageView);
                flag = 0;
            }
        });
        mainImageLayout.addView(mainImage);
        count_mainImage++;
    }

    //게시글 서브 데이터 생성
    public void settingSubData(){
        String imagePath = uri.toString();
        sub_pathList.add(uri);

        LinearLayout sub = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 30;
        sub.setLayoutParams(params);

        ImageView subImage = new ImageView(this);
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subImage.setLayoutParams(imageLayoutParams);
        subImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRelativeLayout.setVisibility(View.VISIBLE);
                selectedImageView = (ImageView) view;
                selectedUri = parent.indexOfChild((LinearLayout)selectedImageView.getParent())-2;
                flag = 1;
            }
        });
        Glide.with(this).load(imagePath).override(sub_imageSize).into(subImage);
        sub.addView(subImage);

        EditText subText = new EditText(this);
        LinearLayout.LayoutParams editLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editLayoutParams.leftMargin=10;
        subText.setLayoutParams(editLayoutParams);
        subText.setHint("설명을 입력하세요.");
        sub.addView(subText);

        parent.addView(sub);
    }


    //갤러리 intent
    public void goGallery(int code){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, code);
    }

    //카메라 intent
    public void goCamera(int code){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {}

        // 사진을 저장하고 이미지뷰에 출력
        if (photoFile != null) {
            uri = FileProvider.getUriForFile(this, "com.gachon.i_edu.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, code);
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
            final ArrayList<String> main_contentsList = new ArrayList<>();
            final ArrayList<String> sub_contentsList = new ArrayList<>();
            assert user != null;
            publisher = user.getUid();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            //포스트 데이터베이스
            final DocumentReference documentReference = firebaseFirestore.collection("posts").document();
            String main_text = ((EditText) parent.getChildAt(1)).getText().toString();
            LinearLayout main_images = (LinearLayout) parent.getChildAt(0);
            int size = parent.getChildCount();
            if (main_text.length() == 0) {
                startToast("내용을 입력해 주세요.");
            } else {
                //로딩창
                customProgressDialog.show();
                //화면터치 방지
                customProgressDialog.setCanceledOnTouchOutside(false);
                //뒤로가기 방지
                customProgressDialog.setCancelable(false);

                main_contentsList.add(main_text);
                int main_image_count = main_images.getChildCount();
                if (main_image_count == 0) {
                    postInfo = new PostInfo(documentReference.getId(), publisher, field, subject,
                            title, main_contentsList, sub_contentsList, new Date(),
                            0L, 0L);
                    storeUpload(documentReference, postInfo);
                }
                for (int i = 0; i < main_images.getChildCount(); i++) {
                    main_contentsList.add(main_pathList.get(i).toString());
                    final StorageReference mountainImagesRef = storageReference
                            .child("posts/" + documentReference.getId() + "/" + main_pathCount + "jpg");
                    try {
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setCustomMetadata("index", "" + (main_contentsList.size() - 1)).build();
                        UploadTask uploadTask = mountainImagesRef.putFile(main_pathList.get(main_pathCount), metadata);
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
                                        main_contentsList.set(index, uri.toString());
                                        main_successCount++;
                                        if (size <= 2) {
                                            if (main_pathList.size() == main_successCount) {
                                                postInfo = new PostInfo(documentReference.getId(), publisher, field, subject,
                                                        title, main_contentsList, sub_contentsList, new Date(),
                                                        0L, 0L);
                                                storeUpload(documentReference, postInfo);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    } catch (Exception e) {
                        Log.d("로그", "에러 : " + e.toString());
                    }
                    main_pathCount++;
                }
                if (size > 2) {
                    for (int i = 2; i < size; i++) {
                        LinearLayout sub_layout = (LinearLayout) (parent.getChildAt(i));
                        for (int j = 0; j < sub_layout.getChildCount(); j++) {
                            View view = sub_layout.getChildAt(j);
                            if (view instanceof EditText) {
                                String text = ((EditText) view).getText().toString();
                                if (text.length() > 0) {
                                    sub_contentsList.add(text);
                                }
                            } else {
                                sub_contentsList.add(sub_pathList.get(sub_pathCount).toString());
                                final StorageReference mountainImagesRef = storageReference
                                        .child("posts/" + documentReference.getId() + "/" + sub_pathCount + "jpg");
                                try {
                                    StorageMetadata metadata = new StorageMetadata.Builder()
                                            .setCustomMetadata("index", "" + (sub_contentsList.size() - 1)).build();
                                    UploadTask uploadTask = mountainImagesRef.putFile(sub_pathList.get(sub_pathCount), metadata);
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("tag", "check2");
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
                                            mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    sub_contentsList.set(index, uri.toString());
                                                    sub_successCount++;
                                                    if (main_pathList.size() == main_successCount && sub_pathList.size() == sub_successCount) {
                                                        postInfo = new PostInfo(documentReference.getId(), publisher, field, subject,
                                                                title, main_contentsList, sub_contentsList, new Date(),
                                                                0L, 0L);
                                                        storeUpload(documentReference, postInfo);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.d("로그", "에러 : " + e.toString());
                                }
                                sub_pathCount++;
                            }
                        }
                    }
                }
            }
        }
    }
    private void storeUpload(DocumentReference documentReference, PostInfo postInfo){
        //좋아요 데이터베이스
        documentReference.set(postInfo)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    setStringData();
                    startToast("업로드 완료");
                    customProgressDialog.cancel();
                    customProgressDialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), PostViewActivity.class);
                    bundle.putString("field",field);
                    bundle.putString("subject",subject);
                    intent.putExtras(bundle);
                    intent.putExtra("object", postInfo);
                    intent.putExtra("like_list", like_post);
                    startActivity(intent);
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

    //문서 바꾸기
    public void setStringData(){
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference documentReference;
        documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.update("question", question_count+1 ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            int count = Integer.parseInt(documentSnapshot.getData().get("question").toString()) +
                                    Integer.parseInt(documentSnapshot.getData().get("solve").toString());
                            if(count >= 1){
                                documentReference.update("level", "Bronze");
                            }else if(count >= 10){
                                documentReference.update("level", "Silver");
                            }
                            else if(count >= 50){
                                documentReference.update("level", "Gold");
                            }else if(count >= 100){
                                documentReference.update("level", "Diamond");
                            }
                        }
                    }
                });
            }
        });
    }

    public void startToast(String content){
        Toast.makeText(this,content,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}