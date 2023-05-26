package com.gachon.i_edu.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.ChattingActivity;
import com.gachon.i_edu.adpater.EntireMessageAdapter;
import com.gachon.i_edu.adpater.MessageAdapter;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.ChatInfo;
import com.gachon.i_edu.info.MemberInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EntireChatFragment extends Fragment {

    //채팅방 시작하자마자 아이테 ㅁ하나 붙잡을 테니 전역변수로
    FirebaseFirestore firestore;
    CollectionReference chatRef;
    private View view;
    private ProgressDialog customProgressDialog;
    private FirebaseUser user;
    private RelativeLayout layoutChoose, layoutImage;
    private ImageView btnDelete, btnImage, btnBack;
    private Button btnCamera, btnGallery;
    private Uri getUri;
    private int image_count;
    //리사이클러뷰에 넣어줄 아이템
    ArrayList<ChatInfo> messageItems = new ArrayList<>();
    ArrayList<String> storeIndex = new ArrayList<>();
    EntireMessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private TextView textName;
    private ImageView btnComplete, imageEnrollment;
    private EditText editMessage;
    private String photoUrl;
    private String chatName;
    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Firebase);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_entire_chat, container, false);

        customProgressDialog = new ProgressDialog(getActivity());
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        layoutChoose = view.findViewById(R.id.layout_choose);
        layoutImage = view.findViewById(R.id.layout_set_image);
        btnBack = view.findViewById(R.id.btn_back);
        btnBack.setVisibility(View.GONE);
        btnImage = view.findViewById(R.id.btn_image);
        btnCamera = view.findViewById(R.id.btn_camera);
        btnGallery = view.findViewById(R.id.btn_gallery);
        btnDelete = view.findViewById(R.id.btn_delete);
        imageEnrollment = view.findViewById(R.id.image_enrollment);
        btnDelete = view.findViewById(R.id.btn_delete);

        layoutChoose.setOnClickListener(onClickListener);
        btnDelete.setOnClickListener(onClickListener);
        btnImage.setOnClickListener(onClickListener);
        btnCamera.setOnClickListener(onClickListener);
        btnGallery.setOnClickListener(onClickListener);
        btnDelete.setOnClickListener(onClickListener);

        textName = view.findViewById(R.id.text_title);
        textName.setText("자유 채팅");
        btnComplete = view.findViewById(R.id.btn_complete);
        btnComplete.setOnClickListener(onClickListener);
        editMessage = view.findViewById(R.id.edit_message);
        recyclerView = view.findViewById(R.id.recycler);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recycler);
        messageAdapter = new EntireMessageAdapter(getActivity(), messageItems);
        messageAdapter.setHasStableIds(true);

        recyclerView.setAdapter(messageAdapter);
        chatRef = firestore.collection("EntireChat");
        getList();
        return view;
    }

    private void getList() {
        //채팅방이름으로 된 컬렉션에 저장되어 있는 데이터들 읽어오기
        //chatRef의 데이터가 변경될때마다 반응하는 리스너 달기 : get()은 일회용
        chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() { //데이터가 바뀔떄마다 찍음
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //데이터가 바뀔때마다 그냥 add하면 그 순간의 모든것을 찍어 가져오기 때문에 중복되어버림
                //따라서 변경된 Document만 찾아달라고 해야함
                //1. 바뀐 애들 찾온다 - 왜 리스트인가? 처음 시작할 때 문제가 됨 그래서 여러개라고 생각함
                assert value != null;
                List<DocumentChange> documentChanges = value.getDocumentChanges();
                for (DocumentChange documentChange : documentChanges) {
                    //2.변경된 문서내역의 데이터를 촬영한 DocumentSnapshot얻어오기
                    DocumentSnapshot snapshot = documentChange.getDocument();
                    //3.Document에 있는 필드값 가져오기
                    Map<String, Object> msg = snapshot.getData();
                    if (msg != null) {
                        String msgName = msg.get("msgName").toString();
                        String uid = msg.get("userId").toString();
                        String message = msg.get("message").toString();
                        String profileUrl = msg.get("profileUrl").toString();
                        String time = msg.get("time").toString();
                        boolean read = (boolean) msg.get("read");
                        //4.읽어온 메세지를 리스트에 추가
                        messageItems.add(new ChatInfo(msgName, uid, message, profileUrl, time, read));
                        storeIndex.add(msgName);
                        messageAdapter.notifyItemInserted(messageItems.size() - 1);

                        //5.아답터에게 데이터가 추가 되었다고 공지 -> 해야 화면 갱신됨
                        //messageAdapter.notifyItemInserted(messageItems.size() - 1);

                        // notifyDataSetChanged() : 여러개가 한번에 여러개 바뀌었을 때
                        //notifyItemRangeInserted() : 현재 for문 안에서 하나 바뀔때 마다 알려주면 됨
                        //notifyItemRangeInserted(바뀐위치)
                        //바뀐위치는 마지막 번호 = messageItems.size()-1

                        //리사이클러뷰의 스크롤위치 가장 아래로 이동
                        recyclerView.scrollToPosition(messageItems.size() - 1);
                    }
                }
            }
        });
    }

    private void clickSend(Uri uri) {
        //로딩창
        customProgressDialog.show();
        //화면터치 방지
        customProgressDialog.setCanceledOnTouchOutside(false);
        //뒤로가기 방지
        customProgressDialog.setCancelable(false);
        if(uri == null && editMessage.getText().toString().equals("")){
            Toast.makeText(getActivity(),"내용을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        //파이어베이스 디비에 저장할 데이터들 준비 (사진, 이름 메세지 시간)
        String message;
        if(uri != null)
            message = uri.toString();
        else
            message = editMessage.getText().toString();
        DocumentReference documentReference = firestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String profileUrl = documentSnapshot.getData().get("photoUri").toString();
                            //메세지를 작성 시간을 문자열 [시:분]
                            Calendar calendar = Calendar.getInstance();
                            String time = calendar.get(Calendar.HOUR_OF_DAY) + ":"+calendar.get(Calendar.MINUTE);
                            //필드에 넣을 값을 MessageItem 객체로 만들어서 한방에 입력
                            //필드값을 객체로 만들어 저장하자 : 리사이클러뷰에 넣기 위해
                            ChatInfo item = new ChatInfo("MSG_"+ System.currentTimeMillis(), user.getUid(), message, profileUrl, time, false);

                            //'채팅방이름' 컬렉션에 채팅 메세지들을 저장
                            // 단 시간 순으로 정렬되도록 도큐먼트의 이름은 현재시간(밀리세컨드)로 지정
                            chatRef.document("MSG_"+ System.currentTimeMillis()).set(item);

                            //다음 메세지를 입력이 수월하도록 EditText에 있는 글씨 삭제
                            editMessage.setText("");
                            layoutImage.setVisibility(View.GONE);
                            getUri = null;
                            customProgressDialog.cancel();
                        }
                    }
                });
    }

    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.btn_delete:
                layoutImage.setVisibility(View.GONE);
                image_count--;
                break;
            case R.id.btn_image:
                if(image_count == 1){
                    Toast.makeText(getActivity(),"사진은 한 장만 추가할 수 있어요", Toast.LENGTH_SHORT);
                }
                else
                    layoutChoose.setVisibility(View.VISIBLE);
                break;
            case R.id.layout_choose:
                layoutChoose.setVisibility(View.GONE);
                break;
            case R.id.btn_gallery:
                goGallery(REQUEST_GALLERY);
                break;
            case R.id.btn_picture:
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    goCamera(REQUEST_CAMERA);
                }else{
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA}, 1);
                }
                break;
            case R.id.btn_complete:
                startUpload();
                break;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (REQUEST_GALLERY):
                    getUri = data.getData();
                case (REQUEST_CAMERA):
                    settingData();
                    layoutChoose.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public void settingData(){
        String imagePath = getUri.toString();
        layoutImage.setVisibility(View.VISIBLE);
        Glide.with(this).load(imagePath).into(imageEnrollment);
        image_count++;
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
            getUri = FileProvider.getUriForFile(getActivity(), "com.gachon.i_edu.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getUri);
            startActivityForResult(intent, code);
        }
    }

    private File createImageFile() throws IOException {
        // 파일이름을 세팅 및 저장경로 세팅
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    private void startUpload() {
        if(getUri == null) {
            clickSend(null);
            return;
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        final StorageReference mountainImagesRef = storageReference
                .child("chat/" + "MSG_" + user.getUid() + "_" + System.currentTimeMillis() + "jpg");
        try {
            //로딩창
            customProgressDialog.show();
            //화면터치 방지
            customProgressDialog.setCanceledOnTouchOutside(false);
            //뒤로가기 방지
            customProgressDialog.setCancelable(false);
            UploadTask uploadTask = mountainImagesRef.putFile(getUri);
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
                            clickSend(uri);
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.d("로그", "에러 : " + e.toString());
        }
    }
}