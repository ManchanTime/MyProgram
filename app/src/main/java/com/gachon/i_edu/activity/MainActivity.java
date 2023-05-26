package com.gachon.i_edu.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.gachon.i_edu.BasicFunctions;
import com.gachon.i_edu.R;
import com.gachon.i_edu.fragment.MainFragment;
import com.gachon.i_edu.fragment.MyPageFragment;
import com.gachon.i_edu.fragment.PostListFragment;
import com.gachon.i_edu.fragment.ChatListFragment;
import com.gachon.i_edu.kakaoApplication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends BasicFunctions {

    private ImageView mmyPage, mBoardList, mCamera, mChat, mHome;
    private ArrayList<ImageView> mainButtons;
    private ArrayList<Integer> images;
    private int lastButton;
    private FragmentManager fragmentManager;
    private Fragment chatListFragment, mainFragment, myPageFragment, postListFragment;
    private static final int fragment_chat_list = 1, fragment_main = 2, fragment_post_list = 3, fragment_my_page = 4;
    private Uri uri;
    private static final int REQUEST_CAMERA = 0;
    private TextView text_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        chatListFragment = new ChatListFragment();
        mainFragment = new MainFragment();

        fragmentManager.beginTransaction().add(R.id.fragment_container, chatListFragment).commit();
        fragmentManager.beginTransaction().show(chatListFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, mainFragment).commit();
        fragmentManager.beginTransaction().show(mainFragment).commit();

        mainButtons = new ArrayList<>();
        images = new ArrayList<>();
        mmyPage = findViewById(R.id.image_my_page);
        mBoardList = findViewById(R.id.image_post_list);
        mCamera = findViewById(R.id.image_camera_post);
        mChat = findViewById(R.id.image_chat);
        mHome = findViewById(R.id.image_home);
        text_count = findViewById(R.id.text_count);

        init();

        mBoardList.setOnClickListener(onClickListener);
        mmyPage.setOnClickListener(onClickListener);
        mCamera.setOnClickListener(onClickListener);
        mChat.setOnClickListener(onClickListener);
        mHome.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.image_camera_post:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    goCamera();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                }
                break;
            case R.id.image_chat:
                setButtons(1, R.drawable.chat_push);
                fragmentView(fragment_chat_list);
                break;
            case R.id.image_home:
                setButtons(2, R.drawable.home_push);
                fragmentView(fragment_main);
                break;
            case R.id.image_post_list:
                setButtons(3, R.drawable.bubble_push);
                fragmentView(fragment_post_list);
                break;
            case R.id.image_my_page:
                setButtons(4, R.drawable.menu_push);
                fragmentView(fragment_my_page);
                break;
        }
    };

    public void init(){
        mainButtons.add(mCamera);
        mainButtons.add(mChat);
        mainButtons.add(mHome);
        mainButtons.add(mBoardList);
        mainButtons.add(mmyPage);

        images.add(R.drawable.camera_org);
        images.add(R.drawable.chat);
        images.add(R.drawable.home_org);
        images.add(R.drawable.bubble_org);
        images.add(R.drawable.menu_org);
        lastButton = 2;
        mainButtons.get(lastButton).setEnabled(false);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (REQUEST_CAMERA):
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Intent intent = new Intent(this, WritePostActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("field", "질문");
                        bundle.putString("uri",uri.toString());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                }
                break;
        }
    }

    public void setButtons(int i, int image){
        mainButtons.get(lastButton).setEnabled(true);
        mainButtons.get(lastButton).setImageResource(images.get(lastButton));
        lastButton = i;
        mainButtons.get(lastButton).setImageResource(image);
        mainButtons.get(lastButton).setEnabled(false);
    }

    public void fragmentView(int fragment){
        switch (fragment) {
            case 1:
                if(chatListFragment == null){
                    chatListFragment = new ChatListFragment();
                    fragmentManager.beginTransaction().add(R.id.fragment_container, chatListFragment).commit();
                }
                if(mainFragment != null) {
                    fragmentManager.beginTransaction().hide(mainFragment).commit();
                }
                if(postListFragment != null) {
                    fragmentManager.beginTransaction().hide(postListFragment).commit();
                }
                if(myPageFragment != null){
                    fragmentManager.beginTransaction().hide(myPageFragment).commit();
                }
                if(chatListFragment != null){
                    fragmentManager.beginTransaction().show(chatListFragment).commit();
                }
                break;
            case 2:
                if(mainFragment == null){
                    mainFragment = new MainFragment();
                    fragmentManager.beginTransaction().add(R.id.fragment_container, mainFragment).commit();
                }
                if(mainFragment != null) {
                    fragmentManager.beginTransaction().show(mainFragment).commit();
                }
                if(postListFragment != null) {
                    fragmentManager.beginTransaction().hide(postListFragment).commit();
                }
                if(myPageFragment != null){
                    fragmentManager.beginTransaction().hide(myPageFragment).commit();
                }
                if(chatListFragment != null){
                    fragmentManager.beginTransaction().hide(chatListFragment).commit();
                }
                break;
            case 3:
                if(postListFragment == null){
                    postListFragment = new PostListFragment();
                    fragmentManager.beginTransaction().add(R.id.fragment_container, postListFragment).commit();
                }
                if(mainFragment != null) {
                    fragmentManager.beginTransaction().hide(mainFragment).commit();
                }
                if(postListFragment != null) {
                    fragmentManager.beginTransaction().show(postListFragment).commit();
                }
                if(myPageFragment != null){
                    fragmentManager.beginTransaction().hide(myPageFragment).commit();
                }
                if(chatListFragment != null){
                    fragmentManager.beginTransaction().hide(chatListFragment).commit();
                }
                break;
            case 4:
                if(myPageFragment == null){
                    myPageFragment = new MyPageFragment();
                    fragmentManager.beginTransaction().add(R.id.fragment_container, myPageFragment).commit();
                }
                if(mainFragment != null){
                    fragmentManager.beginTransaction().hide(mainFragment).commit();
                }
                if(postListFragment != null) {
                    fragmentManager.beginTransaction().hide(postListFragment).commit();
                }
                if(myPageFragment != null) {
                    fragmentManager.beginTransaction().show(myPageFragment).commit();
                }
                if(chatListFragment != null){
                    fragmentManager.beginTransaction().hide(chatListFragment).commit();
                }
                break;
        }
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