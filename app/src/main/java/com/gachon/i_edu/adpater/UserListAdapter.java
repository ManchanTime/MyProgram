package com.gachon.i_edu.adpater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.ChattingActivity;
import com.gachon.i_edu.activity.UserPageActivity;
import com.gachon.i_edu.dialog.ProgressDialog;
import com.gachon.i_edu.info.MemberInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
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

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.zip.Inflater;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {
    private final ArrayList<MemberInfo> mDataset;
    private final Activity activity;
    private FirebaseUser user;
    private ProgressDialog customProgressDialog;
    private final ArrayList<Integer> count = new ArrayList<>();

    public static class UserListViewHolder extends RecyclerView.ViewHolder{
        public RelativeLayout relativeLayout;
        public UserListViewHolder(RelativeLayout v){
            super(v);
            relativeLayout = v;
        }
    }

    public UserListAdapter(Activity activity, ArrayList<MemberInfo> myDataset){
        mDataset = myDataset;
        this.activity = activity;
        user = FirebaseAuth.getInstance().getCurrentUser();
        customProgressDialog = new ProgressDialog(activity);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @NonNull
    @Override
    public UserListAdapter.UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        final UserListViewHolder userListViewHolder = new UserListViewHolder(relativeLayout);
        relativeLayout.setOnClickListener((v) -> {
            //로딩창
            customProgressDialog.show();
            //화면터치 방지
            customProgressDialog.setCanceledOnTouchOutside(false);
            //뒤로가기 방지
            customProgressDialog.setCancelable(false);
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("users").document(user.getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Intent intent = new Intent(activity, ChattingActivity.class);
                            String name = task.getResult().getData().get("name").toString();
                            if(task.getResult().getData().get("photoUri") != null) {
                                String photoUrl = task.getResult().getData().get("photoUri").toString();
                                intent.putExtra("photoUrl", photoUrl);
                            }
                            intent.putExtra("object", mDataset.get(userListViewHolder.getAdapterPosition()));
                            intent.putExtra("name", name);
                            activity.startActivity(intent);
                            customProgressDialog.cancel();
                        }
                    });
        });
        return userListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListViewHolder holder, @SuppressLint("RecyclerView") int position){
        RelativeLayout relativeLayout = holder.relativeLayout;
        ImageView imageView = relativeLayout.findViewById(R.id.image_profile);
        if(mDataset.get(position).getPhotoUri() != null){
            Glide.with(activity).load(mDataset.get(position).getPhotoUri()).override(200).into(imageView);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {;
                Bundle exportBundle;
                Intent intent = new Intent(activity, UserPageActivity.class);
                exportBundle = new Bundle();
                exportBundle.putString("uid", mDataset.get(position).getUid());
                intent.putExtras(exportBundle);
                activity.startActivity(intent);
            }
        });
        TextView textName = relativeLayout.findViewById(R.id.text_name);
        TextView textTier = relativeLayout.findViewById(R.id.text_tier);
        TextView textContent = relativeLayout.findViewById(R.id.text_content);
        textName.setText(mDataset.get(position).getName());
        textTier.setText(mDataset.get(position).getLevel());
        textContent.setText(mDataset.get(position).getGoal());
        TextView textCount = relativeLayout.findViewById(R.id.read_count);
        checkCount(position, textCount);
    }

    @Override
    public int getItemCount(){
        return mDataset.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void checkCount(int position, TextView textCount){
        String chatName;
        if(user.getUid().compareTo(mDataset.get(position).getUid()) > 0){
            chatName = "Chat_" + user.getUid() + "_" + mDataset.get(position).getUid();
        }else
            chatName = "Chat_" + mDataset.get(position).getUid() + "_" + user.getUid();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference chatRef = firebaseFirestore.collection(chatName);
        chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                CollectionReference collectionReference = firebaseFirestore.collection(chatName);
                collectionReference.whereEqualTo("read", false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            int c = 0;
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                if(!documentSnapshot.getData().get("userId").equals(user.getUid())){
                                    c++;
                                }
                            }
                            if(c == 0){
                                textCount.setVisibility(View.GONE);
                            }else {
                                textCount.setVisibility(View.VISIBLE);
                                textCount.setText(c + "");
                            }
                            if(count.size() > position)
                                count.set(position, c);
                            else
                                count.add(c);
                            LayoutInflater layoutInflater = activity.getLayoutInflater();
                            View layout = layoutInflater.inflate(R.layout.activity_main,
                                    (ViewGroup) activity.findViewById(R.id.container2));
                            TextView text = layout.findViewById(R.id.text_count);
                            int sum = 0;
                            for(int t : count){
                                sum += t;
                            }
                            if(sum == 0)
                                text.setVisibility(View.GONE);
                            else{
                                text.setVisibility(View.VISIBLE);
                                text.setText(sum+"");
                            }
                        }
                    }
                });
            }
        });
    }
}
