package com.gachon.i_edu.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.MyPageActivity;
import com.gachon.i_edu.activity.SearchActivity;
import com.gachon.i_edu.activity.UserPageActivity;
import com.gachon.i_edu.activity.WritePostActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PostListFragment extends Fragment {

    private View view;
    private TabLayout tabTitle;
    private Fragment communicationFragment, questionFragment;
    private FragmentManager fragmentManager;
    private static final int fragment_communication = 0;
    private static final int fragment_question = 1;
    private FloatingActionButton addPost;
    private ImageView image_search, image_my_page;
    private RelativeLayout layoutChooseTitle;
    private TextView text_c, text_q;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Firebase);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_post_list, container, false);

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
        switch(v.getId()){
            case R.id.layout_choose_title:
                if(layoutChooseTitle.getVisibility() == View.VISIBLE)
                    layoutChooseTitle.setVisibility(View.GONE);
                break;
            case R.id.btn_board:
                layoutChooseTitle.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_search:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
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
        }
    };

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