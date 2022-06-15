package com.example.GoAutoCamping;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Community_Adapter extends ArrayAdapter<CommunityDTO> {

    //생성자 어레이리스트 만들어서 super로 생성자에 넣어주기 - 기존 방식과 동일하나 getItem 등을 직접 오버라이딩 해준 것이 아니라 super를 통해 상속받음
    public Community_Adapter(@NonNull Context context, @NonNull ArrayList<CommunityDTO> objects) {
        super(context, 0, objects);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        //그리드뷰 화면 구성
        View secondV = View.inflate(getContext(), R.layout.community_grid, null);

        CommunityDTO dto = getItem(position);

        ImageView img = secondV.findViewById(R.id.imgV);
        TextView userNickName = secondV.findViewById(R.id.userNickName);
        TextView likeNum = secondV.findViewById(R.id.likeNum);
        TextView commentNum = secondV.findViewById(R.id.commentNum_commu);


        //댓글 갯수
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = rootRef.child("Community").child(dto.getCommunityId());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentNum.setText(String.format("%d" ,dataSnapshot.getChildrenCount() ));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                commentNum.setText("0");
            }
        };
        ref.addListenerForSingleValueEvent(valueEventListener);


        GradientDrawable drawable = (GradientDrawable)getContext().getDrawable(R.drawable.community_edge);

        //이미지 넣기
        img.setBackground(drawable);
        img.setClipToOutline(true);

        Glide.with(secondV.getContext())
                .load(dto.getCommunityImage())
                .into(img);

        //주소 넣기
        userNickName.setText(dto.getCommunityAddress());

        //좋아요수 넣기
        likeNum.setText(dto.getCommunityLike()+"");

        return secondV;
    }

}
