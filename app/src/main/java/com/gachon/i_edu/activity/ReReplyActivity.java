package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.ReplyAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.ReplyInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.Date;

public class ReReplyActivity extends AppCompatActivity {

    //기본 세팅
    private Button btnCamera, btnGallery;
    private ImageView btnBack;
    private ImageView imageMenu;
    private Button btnDeleteReply, btnDeleteCancel;
    private CardView layoutImageSet;

    //원댓 정보
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private ReplyInfo getInfo;
    private ImageView profileImage;
    private TextView profileName;
    private TextView profileTier;
    private String imageUrl, imageProfile;
    private ImageView profileLike;
    private TextView profileLikeCount;
    private ArrayList<String> profileListList;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private RelativeLayout layoutDelete, layoutChoose, layoutImage;
    private Long reply_count;
    private static final int REQUEST_CAMERA = 0, REQUEST_GALLERY = 1;

    //대댓 정보
    private CollectionReference collectionReference;
    private String name;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final ArrayList<ReplyInfo> replyList = new ArrayList<>();
    private ReplyAdapter reReplyAdapter;
    private ProgressDialog customProgressDialog;

    //대댓 작성
    private EditText editReReply;
    private ImageView imageComplete;
    private ImageView imageEnrollment;
    private String text_contents;
    private Uri write_uri;
    private int image_count;
    private ArrayList<String> reply_contents;
    private ReplyInfo reReplyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_re_reply);

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);

        //툴바 세팅
        btnDeleteCancel = findViewById(R.id.btn_cancel_reply);
        btnDeleteReply = findViewById(R.id.btn_delete_reply);
        btnDeleteCancel.setOnClickListener(onClickListener);
        btnDeleteReply.setOnClickListener(onClickListener);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(onClickListener);

        //이미지 버튼
        btnCamera = findViewById(R.id.btn_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        btnCamera.setOnClickListener(onClickListener);
        btnGallery.setOnClickListener(onClickListener);
        imageEnrollment = findViewById(R.id.image_reply_enrollment);
        //원댓 세팅
        Intent getIntent = getIntent();
        if(getIntent != null){
            getInfo = (ReplyInfo) getIntent.getSerializableExtra("object");
            originalReplySetting(getInfo);
        }

        //대댓 세팅
        swipeRefreshLayout = findViewById(R.id.layout_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh_top();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        //대댓가져오기
        RecyclerView recyclerView = findViewById(R.id.recycler_re_reply);
        reReplyAdapter = new ReplyAdapter(ReReplyActivity.this, replyList, getInfo.getPublisher());
        reReplyAdapter.setHasStableIds(true);
        //recyclerView.setItemViewCacheSize(100);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ReReplyActivity.this));
        recyclerView.setAdapter(reReplyAdapter);
        set_up();
        customProgressDialog.cancel();

        //대댓 작성
        //입력창
        ImageView imageImage = findViewById(R.id.btn_image);
        imageImage.setOnClickListener(onClickListener);
        imageComplete = findViewById(R.id.btn_complete);
        imageComplete.setOnClickListener(onClickListener);
        editReReply = findViewById(R.id.edit_reply);
        editReReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @SuppressLint("ResourceAsColor")
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editReReply.getText().toString();
                if(!text.equals("") || imageComplete.getVisibility() == View.VISIBLE){
                    imageComplete.setVisibility(View.VISIBLE);
                }else{
                    imageComplete.setVisibility(View.GONE);
                }
            }
        });
    }

    private void originalReplySetting(ReplyInfo replyInfo){
        layoutImageSet = findViewById(R.id.layout_image_take);
        layoutDelete = findViewById(R.id.buttons_choose_delete);
        layoutDelete.setOnClickListener(onClickListener);
        layoutChoose = findViewById(R.id.layout_choose);
        layoutChoose.setOnClickListener(onClickListener);
        layoutImage = findViewById(R.id.layout_set_image);
        layoutImage.setOnClickListener(onClickListener);

        imageMenu = findViewById(R.id.btn_menu);
        if(user.getUid().equals(replyInfo.getPublisher()))
            imageMenu.setVisibility(View.VISIBLE);
        imageMenu.setOnClickListener(onClickListener);

        firebaseFirestore = FirebaseFirestore.getInstance();
        profileImage = findViewById(R.id.image_profile);
        profileImage.setOnClickListener(onClickListener);

        profileName = findViewById(R.id.text_name);
        profileTier = findViewById(R.id.text_tier);
        TextView profileTime = findViewById(R.id.text_time);
        TextView profileTextContent = findViewById(R.id.text_content);

        ImageView profileImageContent = findViewById(R.id.image_picture);
        profileImageContent.setOnClickListener(onClickListener);

        profileLike = findViewById(R.id.image_like);
        profileLikeCount = findViewById(R.id.text_count_like);

        documentReference = firebaseFirestore.collection("users").document(replyInfo.getPublisher());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getData().get("photoUri") != null) {
                            imageProfile = document.getData().get("photoUri").toString();
                            Glide.with(getApplicationContext()).load(imageProfile).override(200).into(profileImage);
                        }
                        profileName.setText(document.getData().get("name").toString());
                        profileTier.setText(document.getData().get("level").toString());
                    } else {

                    }
                }
            }
        });
        String result = "";
        Long now = System.currentTimeMillis();
        Date date_now = new Date(now);
        Date date = replyInfo.getCreatedAt();
        Long time = (date_now.getTime() - date.getTime())/(60*1000);
        if(time < 1){
            result = "방금 전";
        }else if(time < 60){
            result = time + "분 전";
        }else if(time < 1440){
            result = (time/60) + "시간 전";
        }else{
            result = ((time/60)/24) + "일 전";
        }
        profileTime.setText(result);
        profileTextContent.setText(replyInfo.getContent().get(0));
        if(replyInfo.getContent().size() > 1){
            layoutImageSet.setVisibility(View.VISIBLE);
            imageUrl = replyInfo.getContent().get(1);
            Glide.with(getApplicationContext()).load(replyInfo.getContent().get(1)).override(1000).into(profileImageContent);
        }
        //좋아요 수 댓글 수
        profileListList = replyInfo.getLike_list();
        profileLikeCount.setText(replyInfo.getLike_count()+"");
        profileLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setClick(profileLike, profileListList, profileLikeCount, replyInfo);
            }
        });
        if(profileListList.contains(user.getUid())){
            profileLike.setImageResource(R.drawable.like);
        }
        else{
            profileLike.setImageResource(R.drawable.unlike);
        }
    }

    private void setClick(ImageView imageLike, ArrayList<String> likeList,TextView textLike, ReplyInfo replyInfo){
        if(likeList.contains(user.getUid())){
            imageLike.setImageResource(R.drawable.unlike);
            likeList.remove(user.getUid());
        }else{
            imageLike.setImageResource(R.drawable.like);
            likeList.add(user.getUid());
        }
        documentReference = firebaseFirestore.collection("replies").document(replyInfo.getId());
        documentReference.update(
                "like_list", likeList,
                "like_count", likeList.size()
        );
        textLike.setText(likeList.size()+"");
    }

    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.image_profile:
                Intent intent = new Intent(this, UserPageActivity.class);
                Bundle exportBundle = new Bundle();
                exportBundle.putString("uid",getInfo.getPublisher());
                intent.putExtras(exportBundle);
                startActivity(intent);
                break;
            case R.id.image_picture:
                Intent imageIntent = new Intent(this, ImageActivity.class);
                imageIntent.putExtra("image", imageUrl);
                startActivity(imageIntent);
                break;
            case R.id.btn_complete:
                text_contents = editReReply.getText().toString();
                startUpload();
                imageComplete.setVisibility(View.GONE);
                break;
            case R.id.btn_image:
                if(image_count == 1){
                    Toast.makeText(this,"사진은 한 장만 추가할 수 있어요", Toast.LENGTH_SHORT);
                }
                else
                    layoutChoose.setVisibility(View.VISIBLE);
                break;
            case R.id.layout_choose:
                layoutChoose.setVisibility(View.GONE);
                break;
            case R.id.btn_menu:
                layoutDelete.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_camera:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    goCamera(REQUEST_CAMERA);
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                }
                break;
            case R.id.btn_gallery:
                goGallery(REQUEST_GALLERY);
                break;
            case R.id.btn_delete:
                image_count--;
                break;
            case R.id.buttons_choose_delete:
                layoutDelete.setVisibility(View.GONE);
                break;
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_delete_reply:
                deleteReply();
                break;
            case R.id.btn_cancel_reply:
                break;
        }
    };

    private void deleteReply(){
        //문서 가져오기
        collectionReference = firebaseFirestore.collection("replies");
        collectionReference.whereEqualTo("postId", getInfo.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                documentReference = firebaseFirestore.collection("replies")
                                        .document(document.getData().get("id").toString());
                                documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {}
                                });
                            }
                            documentReference = firebaseFirestore.collection("replies").document(getInfo.getId());
                            documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    DocumentReference documentReference = firebaseFirestore.collection("posts").document(getInfo.getPostId());
                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            Long reply_count = Long.parseLong(task.getResult().getData().get("reply_count").toString());
                                            documentReference.update("reply_count", (reply_count - 1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
    }

    private void set_up(){
        //문서 가져오기
        Date date = replyList.size() == 0 ? new Date() : replyList.get(replyList.size()-1).getCreatedAt();
        collectionReference = firebaseFirestore.collection("replies");
        collectionReference.whereLessThan("createdAt", date)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .whereEqualTo("postId", getInfo.getId())
                .limit(5)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                replyList.add(new ReplyInfo(
                                                document.getData().get("id").toString(),
                                                document.getData().get("name").toString(),
                                                document.getData().get("postId").toString(),
                                                document.getData().get("publisher").toString(),
                                                (ArrayList<String>) document.getData().get("content"),
                                                new Date(document.getDate("createdAt").getTime()),
                                                (ArrayList<String>) document.getData().get("like_list"),
                                                Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                Long.valueOf(String.valueOf(document.getData().get("reply_count"))),
                                                (boolean) document.getData().get("read")
                                        )
                                );
                            }
                            //리사이클러 뷰 초기화
                            reReplyAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void refresh_top(){
        //문서 가져오기
        collectionReference = firebaseFirestore.collection("replies");
        collectionReference.orderBy("createdAt", Query.Direction.DESCENDING)
                .whereEqualTo("postId", getInfo.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            replyList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                replyList.add(new ReplyInfo(
                                                document.getData().get("id").toString(),
                                                document.getData().get("name").toString(),
                                                document.getData().get("postId").toString(),
                                                document.getData().get("publisher").toString(),
                                                (ArrayList<String>) document.getData().get("content"),
                                                new Date(document.getDate("createdAt").getTime()),
                                                (ArrayList<String>) document.getData().get("like_list"),
                                                Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                Long.valueOf(String.valueOf(document.getData().get("reply_count"))),
                                                (boolean) document.getData().get("read")
                                        )
                                );
                            }
                            //리사이클러 뷰 초기화
                            reReplyAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    //댓글 사진
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_GALLERY):
                    write_uri = data.getData();
                case (REQUEST_CAMERA):
                    layoutImage.setVisibility(View.VISIBLE);
                    layoutChoose.setVisibility(View.GONE);
                    Glide.with(this).load(write_uri).override(300).into(imageEnrollment);
                    image_count++;
                    break;
            }
        }
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
            write_uri = FileProvider.getUriForFile(this, "com.gachon.i_edu.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, write_uri);
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

    private void startUpload() {
        //댓글 컨텐츠
        reply_contents = new ArrayList<>();
        String postId = getInfo.getId();
        String publisher = user.getUid();
        reply_contents.add(text_contents);
        ArrayList<String> reReplyLikeList = new ArrayList<>();

        documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    name = documentSnapshot.getData().get("name").toString();
                    //로딩창
                    customProgressDialog.show();
                    //화면터치 방지
                    customProgressDialog.setCanceledOnTouchOutside(false);
                    //뒤로가기 방지
                    customProgressDialog.setCancelable(false);
                    documentReference = firebaseFirestore.collection("replies").document(getInfo.getId());
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                reply_count = Long.valueOf(String.valueOf(documentSnapshot.getData().get("reply_count")));
                            }
                        }
                    });
                    //답글 데이터베이스
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference();
                    final DocumentReference documentReference = firebaseFirestore.collection("replies").document();
                    if (write_uri == null) {
                        reReplyInfo = new ReplyInfo(documentReference.getId(), name, postId, publisher,
                                reply_contents, new Date(), reReplyLikeList, 0L,0L, false);
                        storeUpload(documentReference, reReplyInfo);
                    }
                    else {
                        final StorageReference mountainImagesRef = storageReference
                                .child("replies/" + documentReference.getId() + "/" + getInfo.getId() + "_" + reply_count + "jpg");
                        try {
                            UploadTask uploadTask = mountainImagesRef.putFile(write_uri);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            reply_contents.add(uri.toString());
                                            reReplyInfo =
                                                    new ReplyInfo(documentReference.getId(), name, postId,
                                                            publisher, reply_contents, new Date(),
                                                            reReplyLikeList,0L, 0L, false);
                                            storeUpload(documentReference, reReplyInfo);
                                        }
                                    });
                                }
                            });
                        } catch (Exception e) {
                            Log.d("로그", "에러 : " + e.toString());
                        }
                    }
                }
            }
        });
    }

    private void storeUpload(DocumentReference documentReference, ReplyInfo replyInfo){
        //좋아요 데이터베이스
        documentReference.set(replyInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DocumentReference documentPost = firebaseFirestore.collection("replies").document(getInfo.getId());
                        documentPost.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                Long reply_count = Long.valueOf(String.valueOf(task.getResult().getData().get("reply_count")));
                                documentPost.update("reply_count", reply_count+1);
                            }
                        });
                        setReplyData();
                        editReReply.setText("");
                        imageEnrollment.setImageResource(0);
                        if(layoutImage.getVisibility() == View.VISIBLE)
                            layoutImage.setVisibility(View.GONE);
                        write_uri = null;
                        refresh_top();
                        customProgressDialog.cancel();
                        customProgressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    //문서 바꾸기
    public void setReplyData(){
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference documentReference;
        documentReference = firebaseFirestore.collection("replies").document(user.getUid());
        documentReference.update("reply_count", reply_count+1 ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        });
    }
}