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
import com.gachon.i_edu.activity.PostViewActivity;
import com.gachon.i_edu.activity.UserPageActivity;
import com.gachon.i_edu.info.PostInfo;
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

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {
    private final ArrayList<String> mDataset;

    public static class MemoViewHolder extends RecyclerView.ViewHolder{
        public RelativeLayout relativeLayout;
        public MemoViewHolder(RelativeLayout v){
            super(v);
            relativeLayout = v;
        }
    }

    public MemoAdapter(Activity activity, ArrayList<String> myDataset){
        mDataset = myDataset;
    }

    @NonNull
    @Override
    public MemoAdapter.MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memo, parent, false);
        return new MemoViewHolder(relativeLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull final MemoViewHolder holder, @SuppressLint("RecyclerView") int position){
        RelativeLayout relativeLayout = holder.relativeLayout;
        CheckBox checkBox = relativeLayout.findViewById(R.id.check_complete);
        checkBox.setText(mDataset.get(position));
        ImageView imageView = relativeLayout.findViewById(R.id.btn_delete);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(position);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
                documentReference.update("goalList", mDataset);
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
