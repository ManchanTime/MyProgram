package com.gachon.i_edu.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.ChattingActivity;
import com.gachon.i_edu.activity.MyPageActivity;
import com.gachon.i_edu.activity.NotificationListActivity;
import com.gachon.i_edu.activity.SearchActivity;
import com.gachon.i_edu.activity.UserPageActivity;
import com.gachon.i_edu.activity.WritePostActivity;
import com.gachon.i_edu.adpater.MessageAdapter;
import com.gachon.i_edu.info.ChatInfo;
import com.gachon.i_edu.info.NotificationInfo;
import com.gachon.i_edu.info.ReplyInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
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

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PostListFragment extends Fragment {

    private View view;
    private TabLayout tabTitle;
    private Fragment communicationFragment, questionFragment;
    private FragmentManager fragmentManager;
    private static final int fragment_communication = 0;
    private static final int fragment_question = 1;
    private FloatingActionButton addPost;
    private ImageView image_search, image_my_page, imageNotification;
    private RelativeLayout layoutChooseTitle;
    private LinearLayout text_c, text_q;
    private TextView textCount;
    private final ArrayList<NotificationInfo> notificationData = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Firebase);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_post_list, container, false);

        setNotification();
        imageNotification = view.findViewById(R.id.btn_notification);
        imageNotification.setOnClickListener(onClickListener);
        image_search = view.findViewById(R.id.btn_search);
        image_my_page = view.findViewById(R.id.btn_my_page);
        image_search.setOnClickListener(onClickListener);
        image_my_page.setOnClickListener(onClickListener);
        layoutChooseTitle = view.findViewById(R.id.layout_choose_title);
        layoutChooseTitle.setOnClickListener(onClickListener);
        addPost = view.findViewById(R.id.btn_board);
        addPost.setOnClickListener(onClickListener);
        text_c = view.findViewById(R.id.btn_communication);
        text_q = view.findViewById(R.id.btn_question);
        text_c.setOnClickListener(onClickListener);
        text_q.setOnClickListener(onClickListener);
        textCount = view.findViewById(R.id.text_count);

        tabTitle = view.findViewById(R.id.tabs_title);
        tabTitle.addTab(tabTitle.newTab().setText("소통하기"));
        tabTitle.addTab(tabTitle.newTab().setText("질문하기"));
        tabTitle.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == 0){
                    fragmentView(fragment_communication);
                }
                else{
                    fragmentView(fragment_question);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        communicationFragment = new CommunicationFragment();
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.post_fragment_container,communicationFragment).commit();
        return view;
    }

    View.OnClickListener onClickListener = (v) -> {
        Intent intent;
        switch(v.getId()){
            case R.id.layout_choose_title:
                if(layoutChooseTitle.getVisibility() == View.VISIBLE)
                    layoutChooseTitle.setVisibility(View.GONE);
                break;
            case R.id.btn_board:
                layoutChooseTitle.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_search:
                intent = new Intent(getActivity(), SearchActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.btn_my_page:
                intent = new Intent(getActivity(), MyPageActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.btn_back:

                break;
            case R.id.btn_communication:
                String field = "소통";
                layoutChooseTitle.setVisibility(View.GONE);
                myStartActivity(WritePostActivity.class, field);
                break;
            case R.id.btn_question:
                layoutChooseTitle.setVisibility(View.GONE);
                field = "질문";
                myStartActivity(WritePostActivity.class, field);
                break;
            case R.id.btn_notification:
                intent = new Intent(getActivity(), NotificationListActivity.class);
                intent.putExtra("object", notificationData);
                getActivity().startActivity(intent);
                break;
        }
    };

    public void setNotification(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference chatRef = firebaseFirestore.collection("replies");
        //채팅방이름으로 된 컬렉션에 저장되어 있는 데이터들 읽어오기
        //chatRef의 데이터가 변경될때마다 반응하는 리스너 달기 : get()은 일회용
        chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() { //데이터가 바뀔떄마다 찍음
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //데이터가 바뀔때마다 그냥 add하면 그 순간의 모든것을 찍어 가져오기 때문에 중복되어버림
                //따라서 변경된 Document만 찾아달라고 해야함
                //1. 바뀐 애들 찾온다 - 왜 리스트인가? 처음 시작할 때 문제가 됨 그래서 여러개라고 생각함
                List<DocumentChange> documentChanges = value.getDocumentChanges();
                int count = 0;
                notificationData.clear();
                for (DocumentChange documentChange : documentChanges) {
                    //2.변경된 문서내역의 데이터를 촬영한 DocumentSnapshot얻어오기
                    DocumentSnapshot snapshot = documentChange.getDocument();
                    //3.Document에 있는 필드값 가져오기
                    Map<String, Object> replies = snapshot.getData();
                    if (replies != null) {
                        boolean read = (boolean) replies.get("read");
                        if (!read) {
                            String postPublisher = replies.get("postPublisher").toString();
                            String publisher = replies.get("publisher").toString();
                            String postId = replies.get("postId").toString();
                            String id = replies.get("id").toString();
                            boolean flag = (boolean) replies.get("flag");
                            Date time = new Date(snapshot.getDate("createdAt").getTime());
                            if (postPublisher.equals(user.getUid())) {
                                count++;
                                notificationData.add(new NotificationInfo(postPublisher, publisher, postId, id, flag, time));
                            }
                        }
                    }
                }
                if(count != 0){
                    textCount.setText(count+"");
                    textCount.setVisibility(View.VISIBLE);
                }else textCount.setVisibility(View.GONE);
            }
        });
    }

    public void myStartActivity(Class c, String field){
        Intent intent = new Intent(getActivity(), c);
        intent.putExtra("field", field);
        startActivity(intent);
    }

    public void fragmentView(int fragment){
        switch (fragment) {
            case 0:
                if(communicationFragment == null){
                    communicationFragment = new CommunicationFragment();
                    fragmentManager.beginTransaction().add(R.id.post_fragment_container, communicationFragment).commit();
                }
                if(communicationFragment != null){
                    fragmentManager.beginTransaction().show(communicationFragment).commit();
                }
                if(questionFragment != null){
                    fragmentManager.beginTransaction().hide(questionFragment).commit();
                }
                break;
            case 1:
                if(questionFragment == null){
                    questionFragment = new QuestionFragment();
                    fragmentManager.beginTransaction().add(R.id.post_fragment_container, questionFragment).commit();
                }
                if(communicationFragment != null){
                    fragmentManager.beginTransaction().hide(communicationFragment).commit();
                }
                if(questionFragment != null){
                    fragmentManager.beginTransaction().show(questionFragment).commit();
                }
        }
    }
}