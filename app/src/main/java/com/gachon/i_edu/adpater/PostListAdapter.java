package com.gachon.i_edu.adpater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.activity.UserPageActivity;
import com.gachon.i_edu.info.PostInfo;
import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.ImageActivity;
import com.gachon.i_edu.activity.PostViewActivity;
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

public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.PostListViewHolder> {
    private ArrayList<PostInfo> mDataset;
    private final Activity activity;
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser user;
    private ArrayList<String> likeList;

    //전달 데이터
    private String tier;
    private String uri;


    public static class PostListViewHolder extends RecyclerView.ViewHolder{
        public CardView cardView;
        public PostListViewHolder(CardView v){
            super(v);
            cardView = v;
        }
    }

    public PostListAdapter(Activity activity, ArrayList<PostInfo> myDataset){
        mDataset = myDataset;
        this.activity = activity;
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostListAdapter.PostListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        final PostListViewHolder postListViewHolder = new PostListViewHolder(cardView);

        cardView.setOnClickListener((v) -> {
            Intent intent = new Intent(activity, PostViewActivity.class);
            intent.putExtra("like_list",likeList);
            intent.putExtra("object",mDataset.get(postListViewHolder.getAdapterPosition()));
            if(mDataset.get(postListViewHolder.getAdapterPosition()) == null){
                Toast.makeText(activity, "존재하지 않는 포스트입니다.", Toast.LENGTH_SHORT).show();
            }
            else
                activity.startActivity(intent);
        });
        return postListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PostListViewHolder holder, @SuppressLint("RecyclerView") int position){
        CardView cardView = holder.cardView;

        String postId = mDataset.get(position).getId();
        //티어
        TextView tierTextView = cardView.findViewById(R.id.text_tier);

        //작성 유저 프로필
        TextView nickTextView = cardView.findViewById(R.id.text_user);
        ImageView profileImage = cardView.findViewById(R.id.image_profile);
        TextView fieldView = cardView.findViewById(R.id.text_subject);
        if(mDataset.get(position).getSubject() == null)
            fieldView.setText(mDataset.get(position).getField());
        else
            fieldView.setText(mDataset.get(position).getField() + " " + mDataset.get(position).getSubject());

        //좋아요 댓글수ㅣ
        documentReference = firebaseFirestore.collection("users").document(user.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    likeList = (ArrayList<String>) task.getResult().getData().get("like_post");
                    //좋아요 수 + 댓글 수
                    TextView countLike = cardView.findViewById(R.id.text_count_like);
                    TextView countReply = cardView.findViewById(R.id.text_info);
                    countReply.setText(mDataset.get(position).getReply_count() + "");
                    ImageView likeImage = cardView.findViewById(R.id.image_like);
                    setStringData(postId,likeImage);
                    countLike.setText(mDataset.get(position).getLike_count()+"");
                    likeImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkLike(setStringData(postId, likeImage), likeImage, countLike, position);
                        }
                    });
                }
            }
        });

        //유저 프로필
        documentReference = firebaseFirestore.collection("users").document(mDataset.get(position).getPublisher());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    nickTextView.setText(documentSnapshot.getData().get("name").toString());
                    if(documentSnapshot.getData().get("photoUri") != null) {
                        uri = documentSnapshot.getData().get("photoUri").toString();
                        Glide.with(activity).load(uri).into(profileImage);
                    }
                    tier = documentSnapshot.getData().get("level").toString();
                    tierTextView.setText(tier);
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle exportBundle;
                Intent intent = new Intent(activity, UserPageActivity.class);
                exportBundle = new Bundle();
                exportBundle.putString("uid", mDataset.get(position).getPublisher());
                intent.putExtras(exportBundle);
                activity.startActivity(intent);
            }
        });

        //제목
        TextView titleTextView = cardView.findViewById(R.id.text_title);
        titleTextView.setText(mDataset.get(position).getTitle());


        //작성 날짜
        TextView createdAtTextView = cardView.findViewById(R.id.text_createdAt);
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
        ArrayList<String> contentsList = mDataset.get(position).getMain_content();
        LinearLayout baseLayout = cardView.findViewById(R.id.layout_base);
        LinearLayout contentsLayout = baseLayout.findViewById(R.id.layout_contents);
        LinearLayout pictureLayout = contentsLayout.findViewById(R.id.layout_pictures);
        TextView contentTextView = contentsLayout.findViewById(R.id.text_content);
        contentTextView.setText(contentsList.get(0));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,600);
        layoutParams.weight = 1f;
        layoutParams.rightMargin=20;
        int contentsSize = contentsList.size();
        pictureLayout.removeAllViews();
        if(pictureLayout.getChildCount() == 0){
            if (contentsSize > 1) {
                for (int i = 1; i < contentsSize; i++) {
                    String contents = contentsList.get(i);
                    if (Patterns.WEB_URL.matcher(contents).matches()) {
                        ImageView imageView = new ImageView(activity);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        pictureLayout.addView(imageView);
                        Glide.with(activity).load(contents).override(1000).into(imageView);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(activity, ImageActivity.class);
                                intent.putExtra("image", contents);
                                activity.startActivity(intent);
                            }
                        });
                    }
                }
            }
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

    //좋아요했는지 체크
    public void checkLike(boolean flag, ImageView image_like, TextView countLike, int position) {
        image_like.setEnabled(false);
        documentReference = firebaseFirestore.collection("posts").document(mDataset.get(position).getId());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Long like_count = Long.parseLong(task.getResult().getData().get("like_count").toString());
                    if (!flag) {
                        image_like.setImageResource(R.drawable.like);
                        countLike.setText((like_count+1) + "");
                        likeList.add(mDataset.get(position).getId());
                        like_count++;
                    } else {
                        image_like.setImageResource(R.drawable.unlike);
                        countLike.setText((like_count-1) + "");
                        likeList.remove(mDataset.get(position).getId());
                        like_count--;
                    }
                    documentReference.update("like_count", like_count).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            documentReference = firebaseFirestore.collection("users").document(user.getUid());
                            documentReference.update("like_post", likeList).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    image_like.setEnabled(true);
                                }
                            });
                        }
                    });
                }
            }
        });
    }
    //좋아요 체크
    public boolean setStringData(String postId, ImageView image_like){
        if(!likeList.contains(postId)){
            image_like.setImageResource(R.drawable.unlike);
            return false;
        }else{
            image_like.setImageResource(R.drawable.like);
            return true;
        }
    }
}
