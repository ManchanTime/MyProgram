package com.gachon.i_edu.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gachon.i_edu.R;
import com.gachon.i_edu.adpater.UserListAdapter;
import com.gachon.i_edu.info.MemberInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ChatListFragment extends Fragment {

    private View view;
    private TabLayout tabs;
    private FragmentManager fragmentManager;
    private Fragment personalListFragment, entireListFragment;
    private static final int fragment_personal_list = 1, fragment_entire_list = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_Firebase);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        view = localInflater.inflate(R.layout.fragment_chat_list, container, false);

        tabs = view.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("개인 채팅"));
        tabs.addTab(tabs.newTab().setText("전체 채팅"));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if(position == 0){
                    fragmentView(fragment_personal_list);
                }
                else{
                    fragmentView(fragment_entire_list);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        personalListFragment = new PersonalChatFragment();
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.chat_fragment_container,personalListFragment).commit();
        return view;
    }

    public void fragmentView(int fragment){
        switch (fragment) {
            case 1:
                if(personalListFragment == null){
                    personalListFragment = new PersonalChatFragment();
                    fragmentManager.beginTransaction().add(R.id.chat_fragment_container, personalListFragment).commit();
                }
                if(personalListFragment != null){
                    fragmentManager.beginTransaction().show(personalListFragment).commit();
                }
                if(entireListFragment != null){
                    fragmentManager.beginTransaction().hide(entireListFragment).commit();
                }
                break;
            case 2:
                if(entireListFragment == null){
                    entireListFragment = new EntireChatFragment();
                    fragmentManager.beginTransaction().add(R.id.chat_fragment_container, entireListFragment).commit();
                }
                if(personalListFragment != null){
                    fragmentManager.beginTransaction().hide(personalListFragment).commit();
                }
                if(entireListFragment != null){
                    fragmentManager.beginTransaction().show(entireListFragment).commit();
                }
        }
    }
}