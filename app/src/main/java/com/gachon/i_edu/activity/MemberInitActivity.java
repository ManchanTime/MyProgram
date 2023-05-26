package com.gachon.i_edu.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.info.MemberInfo;
import com.gachon.i_edu.R;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.ReplyInfo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MemberInitActivity extends BasicFunctions {

    private Button mBtnComplete, mBtnPicture, mBtnGallery, mBtnCancel;
    private ImageView mImageProfile;
    private ImageView btnCheck;
    private TextView mTextChange;
    private TextView birthday;
    private TextView textName;
    private Uri uri;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private ProgressDialog customProgressDialog;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private FirebaseFirestore db;
    private RelativeLayout layoutChoose;
    private Date date;
    private SimpleDateFormat dateFormat1;
    private boolean result;
    private String name;
    private String image = getURLForResource(R.drawable.book_push);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);
        mBtnComplete = findViewById(R.id.btn_com);
        mImageProfile = findViewById(R.id.image_profile);
        mTextChange = findViewById(R.id.text_profile);
        mBtnPicture = findViewById(R.id.btn_picture);
        mBtnGallery = findViewById(R.id.btn_gallery);
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        textName = findViewById(R.id.text_check);
        btnCheck = findViewById(R.id.btn_check);
        layoutChoose = findViewById(R.id.buttons_choose);
        mBtnCancel = findViewById(R.id.btn_cancel);
        birthday = findViewById(R.id.text_birthday);
        date = new Date(System.currentTimeMillis());
        dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        birthday.setText(dateFormat1.format(date));

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mImageProfile.setOnClickListener(onClickListener);

        mTextChange.setOnClickListener(onClickListener);

        mBtnComplete.setOnClickListener(onClickListener);

        mBtnGallery.setOnClickListener(onClickListener);

        mBtnPicture.setOnClickListener(onClickListener);

        mBtnCancel.setOnClickListener(onClickListener);

        birthday.setOnClickListener(onClickListener);

        btnCheck.setOnClickListener(onClickListener);

        uri = Uri.parse(image);
    }

    @SuppressLint("NonConstantResourceId")
    View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.btn_com:
                profileUpdate();
                layoutChoose.setVisibility(View.GONE);
                break;
            case R.id.image_profile:
            case R.id.text_profile:
                if (layoutChoose.getVisibility() == View.VISIBLE) {
                    layoutChoose.setVisibility(View.GONE);
                } else {
                    layoutChoose.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_picture:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    goCamera();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                }
                layoutChoose.setVisibility(View.GONE);
                break;
            case R.id.btn_gallery:
                goGallery();
                layoutChoose.setVisibility(View.GONE);
                break;
            case R.id.btn_cancel:
                layoutChoose.setVisibility(View.GONE);
                break;
            case R.id.text_birthday:
                mOnClick_DatePick();
                layoutChoose.setVisibility(View.GONE);
                break;
            case R.id.btn_check:
                name = ((EditText) (findViewById(R.id.edit_name))).getText().toString();
                if(name.equals("")){
                    startToast("닉네임을 입력해주세요");
                }else{
                    checkName(name);
                }
                break;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (REQUEST_GALLERY):
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        layoutChoose.setVisibility(View.GONE);
                        uri = data.getData();
                        Glide.with(this).load(uri).into(mImageProfile);
                    } catch (Exception e) {
                    }
                }
                break;
            case (REQUEST_CAMERA):
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        layoutChoose.setVisibility(View.GONE);
                        Glide.with(this).load(uri).into(mImageProfile);
                    } catch (Exception e) {
                    }
                }
                break;
        }
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

    public void profileUpdate() {
        final String bt = birthday.getText().toString();
        final String university = ((EditText) (findViewById(R.id.edit_university))).getText().toString();
        final String age = ((EditText) (findViewById(R.id.edit_grade))).getText().toString();

        textName.setVisibility(View.VISIBLE);
        if (!result) {
            startToast("닉네임을 확인해주세요.");
            return;
        }
        if (birthday.length() > 5 && university.length() > 0 && age.length() > 0) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            final StorageReference mountainImageRef = storageReference.child("users/" + user.getUid() + "/profileImage.jpg");

            customProgressDialog.show();
            //화면터치 방지
            customProgressDialog.setCanceledOnTouchOutside(false);
            //뒤로가기 방지
            customProgressDialog.setCancelable(false);

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
                        Uri downloadUri = task.getResult();
                        ArrayList<String> like_post = new ArrayList<>();
                        MemberInfo memberInfo = new MemberInfo(user.getUid(), downloadUri.toString(), name, bt, university, age, "Iron", 0, 0, like_post);
                        uploader(memberInfo);
                    } else {
                        startToast("회원정보 저장에 실패했습니다.");
                    }
                    customProgressDialog.cancel();
                }
            });
        }
    }

    private void uploader(MemberInfo memberInfo) {
        //리얼타임 데이터베이스
        //mDatabase.getReference().child("users").child(user.getUid()).setValue(memberInfo);
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).set(memberInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startToast("회원정보 등록에 성공하였습니다.");
                        mystartActivity(MainActivity.class);
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

    DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int yy, int mm, int dd) {
                    // Date Picker에서 선택한 날짜를 TextView에 설정
                    if(mm < 10)
                        mm = Integer.parseInt("0" + mm);
                    if(dd < 10){
                        dd = Integer.parseInt("0" + dd);
                    }
                    birthday.setText(String.format("%d-%d-%d", yy, mm + 1, dd));
                }
            };

    public void mOnClick_DatePick(){
        // DATE Picker가 처음 떴을 때, 오늘 날짜가 보이도록 설정.
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, mDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).show();
    }

    public void checkName(String name){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference;
        collectionReference = firebaseFirestore.collection("users");
        collectionReference.whereEqualTo("name", name)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().size() == 0){
                                textName.setText("사용 가능한 닉네임입니다.");
                                textName.setTextColor(Color.parseColor("#0000FF"));
                                result = true;
                            }
                            else{
                                textName.setText("이미 사용중인 닉네임입니다.");
                                textName.setTextColor(Color.parseColor("#ff0000"));
                                result = false;
                            }
                        }
                    }
                });
    }

    private String getURLForResource(int resId) {
        return Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + resId).toString();
    }

    private void startToast(String msg){
        Toast.makeText(MemberInitActivity.this,msg,Toast.LENGTH_SHORT).show();
    }
    private void mystartActivity(Class c){
        Intent intent = new Intent(MemberInitActivity.this, c);
        startActivityForResult(intent, 0);
    }
}