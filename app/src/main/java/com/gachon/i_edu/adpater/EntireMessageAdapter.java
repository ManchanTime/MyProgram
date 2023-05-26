package com.gachon.i_edu.adpater;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gachon.i_edu.R;
import com.gachon.i_edu.activity.ImageActivity;
import com.gachon.i_edu.info.ChatInfo;
import com.gachon.i_edu.info.MemberInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EntireMessageAdapter extends RecyclerView.Adapter<EntireMessageAdapter.VH> {

    Context context;
    ArrayList<ChatInfo> messageItems;
    MemberInfo other;
    String chatName;

    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    final int TYPE_MY=0;
    final int TYPE_OTHER=1;

    public EntireMessageAdapter(Context context, ArrayList<ChatInfo> messageItems) {
        this.context = context;
        this.messageItems = messageItems;
    }

    //리사이클러뷰의 아이템뷰가 경우에 따라 다른 모양으로 보여야 할 때 사용하는 콜백 메소드가 있다 : getItemViewType
    //이 메소드에서 해당 position에 따른 식별값(ViewType 번호)를 정하여 리턴하면
    //그 값이 onCreateViewHolder() 메소드의 두번째 파라미터에 전달됨
    //onCreateViewHolder() 메소드 안에서 그 값에 따라 다른 xml 문서를 inflate 하면된다
    @Override
    public int getItemViewType(int position) {
        if(messageItems.get(position).getUserId().equals(firebaseUser.getUid())) {
            //내가 쓴 글
            return TYPE_MY;
        } else {
            return TYPE_OTHER;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //두 레이아웃 중 뭘 넣어야할지 몰라 우선 null 참조
        //파이어베이스에 저장된 name이 내 static name에 있는 것과 같으면 내거 아님 상대방거임
        //두번째 파라미터 int viewType을 사용해서 분기처리 해보자
        //타입은 낸 맘대로 정할 수 있음
        View itemView = null;
        if(viewType == TYPE_MY) {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_chat_my, parent, false);
        }
        else {
            itemView = LayoutInflater.from(context).inflate(R.layout.item_chat_other, parent, false);
        }

        //카톡 날짜 구분선도 이 타입으로 구분한것임

        return new VH(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChatInfo item = messageItems.get(position);
        firebaseFirestore.collection("users").document(item.getUserId())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        holder.tvName.setText(task.getResult().getData().get("name").toString());
                        Glide.with(context).load(item.getProfileUrl()).into(holder.profileImage);
                        String message = item.getMessage();
                        if(Patterns.WEB_URL.matcher(message).matches()){
                            Glide.with(context).load(message).into(holder.imageContent);
                            holder.layout_image_take.setVisibility(View.VISIBLE);
                            holder.imageContent.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(context, ImageActivity.class);
                                    intent.putExtra("image", item.getMessage());
                                    context.startActivity(intent);
                                }
                            });
                        }else{
                            holder.tvMsg.setVisibility(View.VISIBLE);
                            holder.tvMsg.setText(item.getMessage());
                        }
                        holder.tvTime.setText(item.getTime());
                        holder.tvCount.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    class VH extends RecyclerView.ViewHolder {
        //ViewHolder를 2개 만들어 사용하기도함 [MyVH, OtherVH]
        ImageView profileImage;
        TextView tvName;
        TextView tvMsg;
        TextView tvTime;
        TextView tvCount;
        ImageView imageContent;
        CardView layout_image_take;

        public VH(@NonNull View itemView) {
            super(itemView);
            //xml 의 id가 같아야 함
            imageContent = itemView.findViewById(R.id.image_content);
            profileImage = itemView.findViewById(R.id.image_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMsg = itemView.findViewById(R.id.tv_msg);
            tvTime = itemView.findViewById(R.id.tv_time);
            layout_image_take = itemView.findViewById(R.id.layout_image_take);
            tvCount = itemView.findViewById(R.id.tv_count);
        }
    }
}
