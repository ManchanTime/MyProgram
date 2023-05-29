package com.gachon.i_edu.adpater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.gachon.i_edu.activity.PostViewActivity;
import com.gachon.i_edu.activity.ReReplyActivity;
import com.gachon.i_edu.activity.UserPageActivity;
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

import java.util.ArrayList;
import java.util.Date;

public class ReReplyAdapter extends RecyclerView.Adapter<ReReplyAdapter.ReReplyListViewHolder>{
    private final ArrayList<ReplyInfo> mDataset;
    private final Activity activity;
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;

    //전달 데이터
    private String tier;
    private String uri;

    public static class ReReplyListViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public ReReplyListViewHolder(CardView v){
            super(v);
            cardView = v;
        }
    }

    public ReReplyAdapter(Activity activity, ArrayList<ReplyInfo> myDataset){
        mDataset = myDataset;
        this.activity = activity;
        user = FirebaseAuth.getInstance().getCurrentUser();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ReReplyAdapter.ReReplyListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reply, parent, false);
        return new ReReplyListViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ReReplyListViewHolder holder, @SuppressLint("RecyclerView") int position){
        CardView cardView = holder.cardView;

        //이름
        TextView textName = cardView.findViewById(R.id.text_name);
        textName.setText(mDataset.get(position).getName());

        //유저 정보
        ImageView imageProfile = cardView.findViewById(R.id.image_profile);
        TextView textTier = cardView.findViewById(R.id.text_tier);
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("users").document(mDataset.get(position).getPublisher());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.getData().get("photoUri") != null) {
                        uri = documentSnapshot.getData().get("photoUri").toString();
                        Glide.with(activity).load(uri).into(imageProfile);
                    }
                    tier = documentSnapshot.getData().get("level").toString();
                    textTier.setText(tier);
                }
            }
        });

        //작성 날짜
        TextView createdAtTextView = cardView.findViewById(R.id.text_time);
        String result = "";
        Long now = System.currentTimeMillis();
        Date date_now = new Date(now);
        Date date = mDataset.get(position).getCreatedAt();
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
        createdAtTextView.setText(result);

        //컨텐츠
        ArrayList<String> contentsList = mDataset.get(position).getContent();
        CardView imageLayout = cardView.findViewById(R.id.layout_image_take);
        ImageView contentPicture = cardView.findViewById(R.id.image_picture);
        TextView contentTextView = cardView.findViewById(R.id.text_content);
        contentPicture.setImageResource(0);
        imageLayout.setVisibility(View.GONE);
        if(contentsList.get(0) != null)
            contentTextView.setText(contentsList.get(0));
        if(contentsList.size() == 2){
            imageLayout.setVisibility(View.VISIBLE);
            Glide.with(activity).load(contentsList.get(1)).override(1000).into(contentPicture);
            contentPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, ImageActivity.class);
                    intent.putExtra("image", contentsList.get(1));
                    activity.startActivity(intent);
                }
            });
        }

        //좋아요 수 댓글 수
        ImageView imageLike;
        ArrayList<String> likeList;
        TextView textLike;
        imageLike = cardView.findViewById(R.id.image_like);
        likeList = mDataset.get(position).getLike_list();
        textLike = cardView.findViewById(R.id.text_count_like);
        textLike.setText(likeList.size() + "");
        imageLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setClick( imageLike, likeList, textLike, position);
            }
        });
        if(likeList.contains(user.getUid())){
            imageLike.setImageResource(R.drawable.like);
        }
        else{
            imageLike.setImageResource(R.drawable.unlike);
        }

        //내가 작성한건지 확인ㅣ
        if(user.getUid().equals(mDataset.get(position).getPublisher())){
            CardView deleteLayout = cardView.findViewById(R.id.buttons_choose_delete);
            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteLayout.setVisibility(View.GONE);
                }
            });
            ImageView imageMenu = cardView.findViewById(R.id.image_menu);
            imageMenu.setVisibility(View.VISIBLE);
            imageMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteLayout.setVisibility(View.VISIBLE);
                    deleteLayout.bringToFront();
                }
            });
            Button delete = deleteLayout.findViewById(R.id.btn_delete_reply);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    documentReference = firebaseFirestore.collection("replies").document(mDataset.get(position).getId());
                    documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            removeItem(position);
                            deleteLayout.setVisibility(View.GONE);
                            Toast.makeText(activity, "삭제 완료", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            Button cancel = deleteLayout.findViewById(R.id.btn_cancel_reply);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteLayout.setVisibility(View.GONE);
                }
            });
        }
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

    private void setClick(ImageView imageLike, ArrayList<String> likeList,TextView textLike, int position){
        if(likeList.contains(user.getUid())){
            imageLike.setImageResource(R.drawable.unlike);
            likeList.remove(user.getUid());
        }else{
            imageLike.setImageResource(R.drawable.like);
            likeList.add(user.getUid());
        }
        documentReference = firebaseFirestore.collection("replies").document(mDataset.get(position).getId());
        documentReference.update(
                "like_list", likeList,
                "like_count", likeList.size()
        );
        textLike.setText(likeList.size()+"");
    }
}
