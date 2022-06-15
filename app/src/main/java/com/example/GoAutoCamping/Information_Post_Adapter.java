package com.example.GoAutoCamping;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Information_Post_Adapter extends RecyclerView.Adapter<Information_Post_Adapter.MainHolder> {

    Context context;
    ArrayList<CommunityDTO> dtos;
    MainHolder mainHolder;

    //인터페이스
    public interface OnItemClickEventListener {
        void onItemClick(View a_view, int a_position, List<CommunityDTO> communityDTOS);
    }

    //인터페이스변수
    private OnItemClickEventListener mItemClickListener;

    //리스너명
    public void setOnItemClickListener(OnItemClickEventListener a_listener) {
        mItemClickListener = a_listener;
    }

    public Information_Post_Adapter(Context context, ArrayList<CommunityDTO> dtos) {
        this.context = context;
        this.dtos = dtos;
    }

    public class MainHolder extends RecyclerView.ViewHolder{
        public TextView name, date, likeNum, commentNum;
        public ImageView image;

        public MainHolder(View view, final OnItemClickEventListener listener){
            super(view);
            this.name = view.findViewById(R.id.post_lv_place);
            this.date = view.findViewById(R.id.post_lv_date);
            this.likeNum = view.findViewById(R.id.post_lv_likeNum);
            this.commentNum = view.findViewById(R.id.post_lv_commentNum);
            this.image = view.findViewById(R.id.post_lv_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onItemClick(v, position, dtos);
                    }

                }
            });
        }
    }

    @NonNull
    @Override
    public MainHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View holderview = LayoutInflater.from(parent.getContext()).inflate(R.layout.information_post_list, parent, false);
        mainHolder = new Information_Post_Adapter.MainHolder(holderview, mItemClickListener);

        return mainHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull Information_Post_Adapter.MainHolder holder, int position) {
        mainHolder.name.setText(dtos.get(position).getCommunityAddress());

        SimpleDateFormat sfd = new SimpleDateFormat("yyyy.MM.dd");

        mainHolder.date.setText(sfd.format(dtos.get(position).getCommunityUploadTime().toDate()));
        mainHolder.likeNum.setText(dtos.get(position).getCommunityLikeUser().size() + "");

        //댓글 갯수
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = rootRef.child("Community").child(dtos.get(position).getCommunityId());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mainHolder.commentNum.setText(String.format("%d" ,dataSnapshot.getChildrenCount() ));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                mainHolder.commentNum.setText("0");
            }
        };
        ref.addListenerForSingleValueEvent(valueEventListener);


        GradientDrawable drawable = (GradientDrawable)context.getDrawable(R.drawable.community_edge);

        //이미지 넣기
        mainHolder.image.setBackground(drawable);
        mainHolder.image.setClipToOutline(true);

        Glide.with(context)
                .load(dtos.get(position).getCommunityImage())
                .into(mainHolder.image);
    }

    @Override
    public int getItemCount() {
        return dtos.size();
    }
}
