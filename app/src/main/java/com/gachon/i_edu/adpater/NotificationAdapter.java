package com.gachon.i_edu.adpater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.ImageActivity;
import com.gachon.i_edu.activity.NotificationListActivity;
import com.gachon.i_edu.activity.PostViewActivity;
import com.gachon.i_edu.activity.ReReplyActivity;
import com.gachon.i_edu.activity.UserPageActivity;
import com.gachon.i_edu.info.NotificationInfo;
import com.gachon.i_edu.info.PostInfo;
import com.gachon.i_edu.info.ReplyInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final ArrayList<NotificationInfo> mDataset;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private DocumentReference documentReference;
    private String userName = "";
    private final Activity activity;

    public static class NotificationViewHolder extends RecyclerView.ViewHolder{
        public RelativeLayout relativeLayout;
        public NotificationViewHolder(RelativeLayout v){
            super(v);
            relativeLayout = v;
        }
    }

    public NotificationAdapter(Activity activity, ArrayList<NotificationInfo> myDataset){
        this.activity = activity;
        mDataset = myDataset;
        firebaseFirestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        final NotificationAdapter.NotificationViewHolder replyListViewHolder = new NotificationAdapter.NotificationViewHolder(relativeLayout);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean check = (boolean) mDataset.get(replyListViewHolder.getAdapterPosition()).getFlag();
                String postId = mDataset.get(replyListViewHolder.getAdapterPosition()).getPostId();
                Log.e("test", postId);
                if (check) {
                    DocumentReference documentReference = firebaseFirestore.collection("posts").document(postId);
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                //대댓글
                                PostInfo postInfo = new PostInfo(
                                        documentSnapshot.getData().get("id").toString(),
                                        documentSnapshot.getData().get("publisher").toString(),
                                        documentSnapshot.getData().get("field").toString(),
                                        documentSnapshot.getData().get("subject").toString(),
                                        documentSnapshot.getData().get("title").toString(),
                                        (ArrayList<String>) documentSnapshot.getData().get("main_content"),
                                        (ArrayList<String>) documentSnapshot.getData().get("sub_content"),
                                        new Date(documentSnapshot.getDate("createdAt").getTime()),
                                        Long.valueOf(String.valueOf(documentSnapshot.getData().get("like_count"))),
                                        Long.valueOf(String.valueOf(documentSnapshot.getData().get("reply_count")))
                                );
                                Intent intent = new Intent(activity, PostViewActivity.class);
                                intent.putExtra("object", postInfo);
                                activity.startActivity(intent);

                            }
                        }
                    });
                }else{
                    DocumentReference documentReference = firebaseFirestore.collection("replies").document(postId);
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                //댓글
                                ReplyInfo replyInfo = new ReplyInfo(
                                        documentSnapshot.getData().get("id").toString(),
                                        documentSnapshot.getData().get("postId").toString(),
                                        documentSnapshot.getData().get("postPublisher").toString(),
                                        documentSnapshot.getData().get("publisher").toString(),
                                        (ArrayList<String>) documentSnapshot.getData().get("content"),
                                        new Date(documentSnapshot.getDate("createdAt").getTime()),
                                        (ArrayList<String>) documentSnapshot.getData().get("like_list"),
                                        Long.valueOf(String.valueOf(documentSnapshot.getData().get("like_count"))),
                                        Long.valueOf(String.valueOf(documentSnapshot.getData().get("reply_count"))),
                                        (boolean) documentSnapshot.getData().get("flag"),
                                        (boolean) documentSnapshot.getData().get("read")
                                );
                                Intent intent = new Intent(activity, ReReplyActivity.class);
                                intent.putExtra("object", replyInfo);
                                activity.startActivity(intent);
                            }
                        }
                    });
                }
            }
        });
        return replyListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder holder, @SuppressLint("RecyclerView") int position){
        RelativeLayout relativeLayout = holder.relativeLayout;
        TextView contents = relativeLayout.findViewById(R.id.text_content);
        TextView time= relativeLayout.findViewById(R.id.text_time);
        String userId = mDataset.get(position).getPushPublisher();
        Log.e("test",userId);

        documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                userName = task.getResult().getData().get("name").toString();
                documentReference = firebaseFirestore.collection("users").document(userId);
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            String name = task.getResult().getData().get("name").toString();
                            contents.setText(name + "님이 " + userName + "님의 게시글에 댓글을 남기셨습니다.");
                            Date date = mDataset.get(position).getTime();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm");
                            //원하는 데이터 포맷 지정
                            String strNowDate = simpleDateFormat.format(date);
                            time.setText(strNowDate);
                        }
                    }
                });
            }
        });
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

    public void removeItem(int position){
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }
}
