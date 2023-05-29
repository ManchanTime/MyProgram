package com.gachon.i_edu.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.ContentsActivity;
import com.gachon.i_edu.activity.MainActivity;
import com.gachon.i_edu.adpater.HotAdapter;
import com.gachon.i_edu.adpater.MemoAdapter;
import com.gachon.i_edu.adpater.TextViewPagerAdapter;
import com.gachon.i_edu.adpater.UserPostAdapter;
import com.gachon.i_edu.info.PostInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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

import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;



public class MainFragment extends Fragment implements OnMapReadyCallback {
    private TextViewPagerAdapter pagerAdapter;
    private ViewGroup rootView;
    MainActivity activity;
    private ViewPager viewPager;
    private TextView textGoal;
    private HotAdapter hotAdapter;
    private final ArrayList<PostInfo> postList = new ArrayList<>();
    private ArrayList<String> goalList = new ArrayList<>();
    private MemoAdapter memoAdapter;
    private GoogleMap mMap;
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //private TextViewPagerAdapter pagerAdapter;

    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000; // time in milliseconds between successive task executions.

    @Override
    public void onResume(){
        super.onResume();
        setOn();
        setGoal();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                textGoal.setText(task.getResult().getData().get("goal").toString());
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup fragment_container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, fragment_container, false);
        final ScrollView svRestoDetail = rootView.findViewById(R.id.scrollview);
//        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//        ImageView ivMapTransparent = (ImageView) rootView.findViewById(R.id.ivMapTransparent);
//        ivMapTransparent.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int action = event.getAction();
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN:
//                    case MotionEvent.ACTION_MOVE:
//                        // Disallow ScrollView to intercept touch events.
//                        svRestoDetail.requestDisallowInterceptTouchEvent(true);
//                        // Disable touch on transparent view
//                        return false;
//                    case MotionEvent.ACTION_UP:
//                        // Allow ScrollView to intercept touch events.
//                        svRestoDetail.requestDisallowInterceptTouchEvent(false);
//                        return true;
//                    default:
//                        return true;
//                }
//            }
//        });

        Intent intent = new Intent(Intent.ACTION_VIEW);
        viewPager = rootView.findViewById(R.id.viewPager2);
        pagerAdapter = new TextViewPagerAdapter(rootView.getContext());
        viewPager.setAdapter(pagerAdapter);

        RecyclerView recycler_hot = rootView.findViewById(R.id.recycler_hot);
        hotAdapter = new HotAdapter(getActivity(), postList);
        hotAdapter.setHasStableIds(true);
        recycler_hot.setHasFixedSize(true);
        recycler_hot.setItemViewCacheSize(100);
        recycler_hot.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_hot.setAdapter(hotAdapter);

        RecyclerView recyclerViewGoal = rootView.findViewById(R.id.recycler_goal);
        memoAdapter = new MemoAdapter(getActivity(), goalList);
        memoAdapter.setHasStableIds(true);
        recyclerViewGoal.setHasFixedSize(true);
        recyclerViewGoal.setItemViewCacheSize(100);
        recyclerViewGoal.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewGoal.setAdapter(memoAdapter);

        ImageView btnPlus = rootView.findViewById(R.id.btn_plus);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = rootView.findViewById(R.id.edit_text);
                String goal = editText.getText().toString();
                if(!goal.equals("")) {
                    goalList.add(goal);
                    editText.setText("");
                    DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
                    documentReference.update("goalList", goalList);
                    memoAdapter.notifyDataSetChanged();
                }
            }
        });

        ImageView imageView;
        ImageView imageView2;
        Button contents, answer;
        RadioButton r_btn1, r_btn2, r_btn3;
        RadioGroup radioGroup;
        textGoal = rootView.findViewById(R.id.text_goal);

        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            @Override
            public void run() {
                if (currentPage == 3) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);


        Button btn_link = rootView.findViewById(R.id.btn_link);
        Button btn_link2 = rootView.findViewById(R.id.btn_link2);

        contents = rootView.findViewById(R.id.contents);
        contents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ContentsActivity.class);
                startActivity(intent);
            }
        });

        imageView = rootView.findViewById(R.id.youtube);
        String imageStr = "https://img.youtube.com/vi/FLXFbDudeBs/default.jpg";
        Glide.with(this).load(imageStr).into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=FLXFbDudeBs"));
                startActivity(intent);
            }
        });

        btn_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=FLXFbDudeBs"));
                startActivity(intent);
            }
        });


        imageView2 = rootView.findViewById(R.id.youtube2);
        String imageStr2 = "https://img.youtube.com/vi/wDfqXR_5yyQ/default.jpg";
        Glide.with(this).load(imageStr2).into(imageView2);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=wDfqXR_5yyQ"));
                startActivity(intent);
            }
        });

        btn_link2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=wDfqXR_5yyQ"));
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

//        LatLng SEOUL = new LatLng(37.556, 126.97);
//
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(SEOUL);
//        markerOptions.title("서울");
//        markerOptions.snippet("한국 수도");
//
//        mMap.addMarker(markerOptions);
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 10));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    public void setOn(){
        CollectionReference collectionReference = firebaseFirestore.collection("posts");
        collectionReference.orderBy("like_count", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            postList.clear();
                            for(QueryDocumentSnapshot document : task.getResult()){
                                postList.add(new PostInfo(
                                                document.getData().get("id").toString(),
                                                document.getData().get("publisher").toString(),
                                                document.getData().get("field").toString(),
                                                document.getData().get("subject").toString(),
                                                document.getData().get("title").toString(),
                                                (ArrayList<String>)document.getData().get("main_content"),
                                                (ArrayList<String>)document.getData().get("sub_content"),
                                                new Date(document.getDate("createdAt").getTime()),
                                                Long.valueOf(String.valueOf(document.getData().get("like_count"))),
                                                Long.valueOf(String.valueOf(document.getData().get("reply_count")))
                                        )
                                );
                            }
                            //리사이클러 뷰 초기화
                            hotAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    public void setGoal(){
        DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getData().get("goalList") != null){
                        ArrayList<String> tmp = (ArrayList<String>) task.getResult().getData().get("goalList");
                        goalList.addAll(tmp);
                        memoAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
