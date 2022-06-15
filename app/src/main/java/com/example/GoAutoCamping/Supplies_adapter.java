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

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Supplies_adapter extends ArrayAdapter<SuppliesDTO> {

    Context context;
    String supplyKind;


    public Supplies_adapter(Context _context, int name, ArrayList<SuppliesDTO> objects) {
        super(_context, 0, objects);

        context = _context;

        if(name==1)
            supplyKind = "category_light";
        else if(name==2)
            supplyKind = "category_living";
        else if(name==3)
            supplyKind = "category_cooking";
        else
            supplyKind = "category_etc";

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = View.inflate(context, R.layout.supplies_grid, null);
        TextView textView = v.findViewById(R.id.userNickName_supply);
        ImageView imgV = v.findViewById(R.id.imgV);
        TextView likeNum = v.findViewById(R.id.likeNum_supply);
        TextView commentNum = v.findViewById(R.id.commentNum_supply);

        SuppliesDTO dto = getItem(position);

        //댓글 갯수
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = rootRef.child("Supply").child(supplyKind).child(dto.getPost_id());
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

        GradientDrawable drawable = (GradientDrawable) imgV.getContext().getDrawable(R.drawable.community_edge);

        imgV.setBackground(drawable);
        imgV.setClipToOutline(true);

        Glide.with(getContext())
                .load(dto.getPost_Image())
                .into(imgV);

        textView.setText(dto.getPost_name());
        likeNum.setText(Integer.toString(dto.getPost_like()));

        return v;
    }


}
