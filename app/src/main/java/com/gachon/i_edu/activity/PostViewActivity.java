package com.gachon.i_edu.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.PhOffsetItemDecoration;
import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.ReplyAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.ChatInfo;
import com.gachon.i_edu.info.PostInfo;
import com.gachon.i_edu.info.ReplyInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
import java.util.List;
import java.util.Map;

public class PostViewActivity extends BasicFunctions {

    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private ImageView btn_back;
    private TextView text_title, text_field, text_name, text_tier, text_time, text_count_like, text_reply_count;
    private ImageView image_profile, image_like, image_reply, btn_menu;
    private String name, profile_uri, tier, uid, postId;
    private Long count_like, reply_count;
    private String title;
    private ArrayList<String> like_list;
    private ArrayList<String> main_contents;
    private ArrayList<String> sub_contents;
    private FirebaseUser user;
    private RelativeLayout button_choose;
    private Bundle exportBundle;
    private PostInfo postInfo;
    private ProgressDialog customProgressDialog;
    private TabLayout tabs;

    //댓글 정보
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private ImageView btn_image, image_reply_enrollment, image_delete;
    private Button btn_gallery, btn_camera;
    private RelativeLayout layout_reply_image, layout_choose;
    private ImageView btn_complete;
    private EditText edit_reply;
    private ImageView image_menu;
    private Button btn_delete_post, btn_cancel;
    private Date writeDate;
    private ArrayList<ReplyInfo> replyList = new ArrayList<>();
    private RecyclerView replyRecyclerview;
    private ReplyAdapter replyAdapter;
    private String order;
    private CollectionReference collectionReference;
    private SwipeRefreshLayout swipeRefreshLayout;

    //댓글 입력
    private ReplyInfo replyInfo;
    private ArrayList<String> reply_contents;
    private String text_contents, field, subject;
    private Uri write_uri;
    private int image_count;

    @Override
    protected void onResume(){
        super.onResume();
        refresh_top(order);
        documentReference = firebaseFirestore.collection("posts").document(postId);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                text_reply_count.setText(task.getResult().getData().get("reply_count").toString());
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                like_list = (ArrayList<String>) task.getResult().getData().get("like_post");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);

        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        if(intent != null) {
            //데이터 가져오기
            postInfo = (PostInfo) intent.getSerializableExtra("object");
            writeDate = postInfo.getCreatedAt();
            field = postInfo.getField();
            subject = postInfo.getSubject();
            postId = postInfo.getId();
            main_contents = postInfo.getMain_content();
            sub_contents = postInfo.getSub_content();
            title = postInfo.getTitle();
            uid = postInfo.getPublisher();
            //like_list = (ArrayList<String>) intent.getSerializableExtra("like_list");
            String publisher = postInfo.getPublisher();
            btn_delete_post = findViewById(R.id.btn_delete_post);
            btn_cancel = findViewById(R.id.btn_cancel);
            btn_delete_post.setOnClickListener(onClickListener);
            btn_cancel.setOnClickListener(onClickListener);
            tabs = findViewById(R.id.tabs);
            tabs.addTab(tabs.newTab().setText("최신순"));
            tabs.addTab(tabs.newTab().setText("인기순"));
            tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    switch (position){
                        case 0:
                            order = "createdAt";
                            break;
                        case 1:
                            order = "like_count";
                            break;
                    }
                    refresh_top(order);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            customProgressDialog.show();
            //화면터치 방지
            customProgressDialog.setCanceledOnTouchOutside(false);
            //뒤로가기 방지
            customProgressDialog.setCancelable(false);

            documentReference = firebaseFirestore.collection("users").document(uid);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    name = documentSnapshot.getData().get("name").toString();
                    tier = documentSnapshot.getData().get("level").toString();
                    image_profile = findViewById(R.id.image_profile);
                    if (documentSnapshot.getData().get("photoUri") != null) {
                        profile_uri = documentSnapshot.getData().get("photoUri").toString();
                        Glide.with(getApplicationContext()).load(profile_uri).override(200).into(image_profile);
                    }
                    image_profile.setOnClickListener(onClickListener);
                    text_name = findViewById(R.id.text_name);
                    text_name.setText(name);
                    text_tier = findViewById(R.id.text_tier);
                    text_tier.setText(tier);

                    setting();
                }
            });

            //댓글 리스트
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            firebaseFirestore = FirebaseFirestore.getInstance();

            swipeRefreshLayout = findViewById(R.id.layout_refresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refresh_top(order);
                    documentReference = firebaseFirestore.collection("posts").document(postInfo.getId());
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                reply_count = Long.valueOf(String.valueOf(documentSnapshot.getData().get("reply_count")));
                                count_like = Long.valueOf(String.valueOf(documentSnapshot.getData().get("like_count")));
                                text_count_like.setText(count_like + "");
                                text_reply_count.setText(reply_count + "");
                                setStringData(image_like);
                                image_like.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        checkLike(setStringData(image_like), image_like, text_count_like);
                                    }
                                });
                            }
                        }
                    });
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            //리사이클러 뷰 초기화
            replyRecyclerview = findViewById(R.id.recycler_reply);
            replyRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

            PhOffsetItemDecoration itemDecoration = new PhOffsetItemDecoration(0);
            replyRecyclerview.addItemDecoration(itemDecoration);
            replyAdapter = new ReplyAdapter(this, replyList, postInfo.getPublisher());
            replyAdapter.setHasStableIds(true);
            //recyclerView.setItemViewCacheSize(100);
            replyRecyclerview.setHasFixedSize(true);
            replyRecyclerview.setLayoutManager(new LinearLayoutManager(this));
            replyRecyclerview.setAdapter(replyAdapter);
            order = "createdAt";
            set_up();

            //댓글 창
            btn_image = findViewById(R.id.btn_image);
            btn_image.setOnClickListener(onClickListener);
            btn_gallery = findViewById(R.id.btn_gallery);
            btn_gallery.setOnClickListener(onClickListener);
            btn_camera = findViewById(R.id.btn_camera);
            btn_camera.setOnClickListener(onClickListener);
            layout_reply_image = findViewById(R.id.layout_set_image);
            layout_choose = findViewById(R.id.layout_choose);
            layout_choose.setOnClickListener(onClickListener);
            image_reply_enrollment = findViewById(R.id.image_reply_enrollment);
            image_delete = findViewById(R.id.btn_delete);
            image_delete.setOnClickListener(onClickListener);
            btn_complete = findViewById(R.id.btn_complete);
            btn_complete.setOnClickListener(onClickListener);

            //버튼
            btn_back = findViewById(R.id.btn_back);
            btn_back.setOnClickListener(onClickListener);
            btn_menu = findViewById(R.id.btn_menu);
            btn_menu.setVisibility(View.VISIBLE);
            btn_menu.setOnClickListener(onClickListener);
            button_choose = findViewById(R.id.buttons_choose);
            button_choose.setOnClickListener(onClickListener);

            //텍스트뷰
            text_title = findViewById(R.id.text_post_title);
            text_title.setText(title);
            text_field = findViewById(R.id.text_subject);
            if(subject != null)
                text_field.setText(field + " " + subject);
            else
                text_field.setText(field);
            text_count_like = findViewById(R.id.text_count_like);
            text_reply_count = findViewById(R.id.text_reply_count);
            text_time = findViewById(R.id.text_time);
            String result="";
            Long now = System.currentTimeMillis();
            Date date_now = new Date(now);
            Long time = (date_now.getTime() - writeDate.getTime())/(60*1000);
            if(time < 1){
                result = "방금 전";
            }else if(time < 60){
                result = time + "분 전";
            }else if(time < 1440){
                result = (time/60) + "시간 전";
            }else{
                result = ((time/60)/24) + "일 전";
            }
            text_time.setText(result);

            if(publisher.equals(user.getUid())){
                image_menu = findViewById(R.id.btn_menu);
                image_menu.setVisibility(View.VISIBLE);
            }

            //이미지
            image_like = findViewById(R.id.image_like);
            image_reply = findViewById(R.id.image_reply);

            //입력창
            edit_reply = findViewById(R.id.edit_reply);
            edit_reply.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @SuppressLint("ResourceAsColor")
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @SuppressLint("ResourceAsColor")
                @Override
                public void afterTextChanged(Editable editable) {
                    String text = edit_reply.getText().toString();
                    if(!text.equals("") || layout_reply_image.getVisibility() == View.VISIBLE){
                        btn_complete.setVisibility(View.VISIBLE);
                    }else{
                        btn_complete.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void set_up(){
        //문서 가져오기
        collectionReference = firebaseFirestore.collection("replies");
        collectionReference.whereEqualTo("postId",postId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                replyList.add(new ReplyInfo(
                                                document.getData().get("id").toString(),
                                                document.getData().get("postId").toString(),
                                                document.getData().get("postPublisher").toString(),
                                                document.getData().get("publisher").toString(),
                                                (ArrayList<String>)document.getData().get("content"),
                                                new Date(document.getDate("createdAt").getTime()),
                                                (ArrayList<String>)document.getData().get("like_list"),
                                                Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                Long.valueOf(String.valueOf(document.getData().get("reply_count"))),
                                                (boolean)document.getData().get("flag"),
                                                (boolean) document.getData().get("read")
                                        )
                                );
                            }
                            //리사이클러 뷰 초기화
                            replyAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void refresh_top(String order) {
        //문서 가져오기
        collectionReference = firebaseFirestore.collection("replies");
        collectionReference.whereEqualTo("postId", postId)
                .orderBy(order, Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            replyList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                replyList.add(new ReplyInfo(
                                                document.getData().get("id").toString(),
                                                document.getData().get("postId").toString(),
                                                document.getData().get("postPublisher").toString(),
                                                document.getData().get("publisher").toString(),
                                                (ArrayList<String>) document.getData().get("content"),
                                                new Date(document.getDate("createdAt").getTime()),
                                                (ArrayList<String>) document.getData().get("like_list"),
                                                Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                Long.valueOf(String.valueOf(document.getData().get("reply_count"))),
                                                (boolean)document.getData().get("flag"),
                                                (boolean) document.getData().get("read")
                                        )
                                );
                            }
                            //리사이클러 뷰 초기화
                            replyAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
    public void setting(){
        documentReference = firebaseFirestore.collection("posts").document(postInfo.getId());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    reply_count = Long.valueOf(String.valueOf(documentSnapshot.getData().get("reply_count")));
                    count_like = Long.valueOf(String.valueOf(documentSnapshot.getData().get("like_count")));
                    text_count_like.setText(count_like + "");
                    text_reply_count.setText(reply_count + "");
                    setStringData(image_like);
                    image_like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkLike(setStringData(image_like), image_like, text_count_like);
                        }
                    });
                }
            }
        });

        //컨텐츠
        LinearLayout contentsLayout = findViewById(R.id.layout_pictures);
        LinearLayout mainContentsLayout = contentsLayout.findViewById(R.id.layout_main_contents);
        TextView contentTextView = findViewById(R.id.text_content);
        contentTextView.setText(main_contents.get(0));

        int contentsSize = main_contents.size();
        if (contentsSize > 1) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 400);
            layoutParams.weight = 1f;
            layoutParams.rightMargin = 20;
            for (int i = 1; i < contentsSize; i++) {
                String contents = main_contents.get(i);
                if (Patterns.WEB_URL.matcher(contents).matches()) {
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    mainContentsLayout.addView(imageView);
                    Glide.with(this).load(contents).override(1000).into(imageView);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
                            intent.putExtra("image", contents);
                            startActivity(intent);
                        }
                    });
                }
            }
        }

        int subContentsSize = sub_contents.size();
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(500, 500);
        if (subContentsSize > 1) {
            for (int i = 0; i < subContentsSize; i++) {
                String contents = sub_contents.get(i);
                LinearLayout subContentLayout = new LinearLayout(this);
                subContentLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                layoutParams.rightMargin = 20;
                layoutParams.bottomMargin = 20;
                subContentLayout.setLayoutParams(layoutParams);
                if (Patterns.WEB_URL.matcher(contents).matches()) {
                    ImageView imageView = new ImageView(this);
                    imageView.setLayoutParams(imageParams);
                    subContentLayout.addView(imageView);
                    Glide.with(this).load(contents).override(1000).into(imageView);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
                            intent.putExtra("image", contents);
                            startActivity(intent);
                        }
                    });
                    if(Patterns.WEB_URL.matcher(sub_contents.get(i+1)).matches()){
                        View view = new View(this);
                        view.setLayoutParams(layoutParams);
                        subContentLayout.addView(view);
                    }else{
                        TextView textView = new TextView(this);
                        textView.setLayoutParams(layoutParams);
                        textView.setText(sub_contents.get(i+1));
                        i++;
                        subContentLayout.addView(textView);
                    }
                }
                contentsLayout.addView(subContentLayout);
            }
        }
        customProgressDialog.cancel();
    }
    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_menu:
                if(button_choose.getVisibility() == View.VISIBLE){
                    button_choose.setVisibility(View.GONE);
                }else
                    button_choose.setVisibility(View.VISIBLE);
                break;
            case R.id.buttons_choose:
                if(button_choose.getVisibility() == View.VISIBLE){
                    button_choose.setVisibility(View.GONE);
                }
                break;
            case R.id.image_profile:
                Intent intent = new Intent(this, UserPageActivity.class);
                exportBundle = new Bundle();
                exportBundle.putString("name",name);
                exportBundle.putString("uri",profile_uri);
                exportBundle.putString("tier",tier);
                exportBundle.putString("uid",uid);
                intent.putExtras(exportBundle);
                startActivity(intent);
                break;
            case R.id.btn_image:
                if(image_count == 1){
                    Toast.makeText(this,"사진은 한 장만 추가할 수 있어요", Toast.LENGTH_SHORT);
                }
                else if(layout_choose.getVisibility() == View.VISIBLE)
                    layout_choose.setVisibility(View.GONE);
                else
                    layout_choose.setVisibility(View.VISIBLE);
                break;
            case R.id.layout_choose:
                layout_choose.setVisibility(View.GONE);
                break;
            case R.id.btn_gallery:
                goGallery(REQUEST_GALLERY);
                break;
            case R.id.btn_camera:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    goCamera(REQUEST_CAMERA);
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                }
                break;
            case R.id.btn_delete:
                layout_reply_image.setVisibility(View.GONE);
                image_count--;
                break;
            case R.id.btn_complete:
                text_contents = edit_reply.getText().toString();
                startUpload();
                btn_complete.setVisibility(View.GONE);
                break;
            case R.id.btn_delete_post:
                deletePost();
                break;
            case R.id.btn_cancel:
                button_choose.setVisibility(View.GONE);
                break;
        }
    };

    public void deletePost(){
        //문서 가져오기
        collectionReference = firebaseFirestore.collection("replies");
        collectionReference.whereEqualTo("postId", postId)
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
                            documentReference = firebaseFirestore.collection("posts").document(postId);
                            documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    finish();
                                }
                            });
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
                    layout_reply_image.setVisibility(View.VISIBLE);
                    layout_choose.setVisibility(View.GONE);
                    Glide.with(this).load(write_uri).override(300).into(image_reply_enrollment);
                    image_count++;
                    break;
            }
        }
    }

    //좋아요했는지 체크
    public void checkLike(boolean flag, ImageView image_like, TextView text_count_like) {
        image_like.setEnabled(false);
        documentReference = firebaseFirestore.collection("posts").document(postId);
        if (!flag) {
            image_like.setImageResource(R.drawable.like);
            count_like++;
            text_count_like.setText((count_like) + "");
            like_list.add(postId);
        } else {
            image_like.setImageResource(R.drawable.unlike);
            count_like--;
            text_count_like.setText((count_like) + "");
            like_list.remove(postId);
        }
        documentReference.update("like_count", count_like).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                documentReference = firebaseFirestore.collection("users").document(user.getUid());
                documentReference.update("like_post", like_list).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        image_like.setEnabled(true);
                    }
                });
            }
        });
    }

    //좋아요 체크
    public boolean setStringData(ImageView image_like){
        if(!like_list.contains(postId)){
            image_like.setImageResource(R.drawable.unlike);
            return false;
        }else{
            image_like.setImageResource(R.drawable.like);
            return true;
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
        String writeName = name;
        String postId = postInfo.getId();
        String postPublisher = postInfo.getPublisher();
        String publisher = user.getUid();
        reply_contents.add(text_contents);
        ArrayList<String> replyLikeList = new ArrayList<>();

        //로딩창
        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);

        //답글 데이터베이스
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        final DocumentReference documentReference = firebaseFirestore.collection("replies").document();
        if (write_uri == null) {
            replyInfo = new ReplyInfo(documentReference.getId(), postId, postPublisher, publisher,
                    reply_contents, new Date(), replyLikeList, 0L,0L, true, false);
            storeUpload(documentReference, replyInfo);
        }
        else {
            final StorageReference mountainImagesRef = storageReference
                    .child("replies/" + documentReference.getId() + "/" + reply_count + "jpg");
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
                                replyInfo = new ReplyInfo(documentReference.getId(), postId, postPublisher,
                                                publisher, reply_contents, new Date(),
                                                replyLikeList,0L, 0L, true, false);
                                storeUpload(documentReference, replyInfo);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                Log.d("로그", "에러 : " + e.toString());
            }
        }
    }

    private void storeUpload(DocumentReference documentReference, ReplyInfo replyInfo){
        //좋아요 데이터베이스
        documentReference.set(replyInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DocumentReference documentPost = firebaseFirestore.collection("posts").document(postId);
                        documentPost.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                Long reply_count = Long.valueOf(String.valueOf(task.getResult().getData().get("reply_count")));
                                documentPost.update("reply_count", reply_count+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        text_reply_count.setText((reply_count+1) + "");
                                    }
                                });
                            }
                        });
                        setReplyData();
                        edit_reply.setText("");
                        layout_reply_image.setVisibility(View.GONE);
                        image_reply_enrollment.setImageResource(0);
                        write_uri = null;
                        refresh_top(order);
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
        documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.update("solve", reply_count+1 ).addOnSuccessListener(new OnSuccessListener<Void>() {
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
}