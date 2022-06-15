package com.example.GoAutoCamping;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatRatingBar;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

public class Recommend_detail_adapter extends ArrayAdapter<RecommendDTO> {


    Context context;

    public Recommend_detail_adapter(@NonNull Context context, @NonNull List<RecommendDTO> objects) {
        super(context, 0, objects);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        //리스트뷰 화면 구성
        View v = View.inflate(getContext(), R.layout.recommend_spot_list, null);

        RecommendDTO dto =  getItem(position);

        ImageView img = v.findViewById(R.id.img_spotlist);
        TextView name = v.findViewById(R.id.spotname);
        TextView address = v.findViewById(R.id.address);
        AppCompatRatingBar rating = v.findViewById(R.id.starRate);

        GradientDrawable drawable = (GradientDrawable)getContext().getDrawable(R.drawable.community_edge);

        //이미지 넣기
        img.setBackground(drawable);
        img.setClipToOutline(true);

        Glide.with(v.getContext())
                .load(dto.getRecommendImage())
                .into(img);

        //장소 이름 넣기
        name.setText(dto.getRecommendTitle());

        //장소 주소 넣기
        address.setText(dto.getRecommendAddress());

        //장소 별점
        rating.setRating(dto.getRecommendStar());

        return v;
    }


}
