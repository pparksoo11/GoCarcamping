package com.example.GoAutoCamping;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class Information_Mark_RecylerAdapter extends RecyclerView.Adapter<Information_Mark_RecylerAdapter.MainHolder> {

    Context context;
    private List<RecommendDTO> dtos = new ArrayList<>();
    MainHolder mainHolder;
    Information_Mark information_mark;

    //파이어베이스
    private FirebaseStorage storage;
    private String imageUrl="";
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    //인터페이스
    public interface OnItemClickEventListener {
        void onItemClick(View a_view, int a_position, List<RecommendDTO> commentDTOS);
    }

    //인터페이스변수
    private OnItemClickEventListener mItemClickListener;

    //리스너명
    public void setOnItemClickListener(OnItemClickEventListener a_listener) {
        mItemClickListener = a_listener;
    }

    public Information_Mark_RecylerAdapter(Context context, List<RecommendDTO> dtos, Information_Mark information_mark){
        this.context = context;
        this.dtos = dtos;
        this.information_mark = information_mark;
    }

    public MainHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View holderview = LayoutInflater.from(parent.getContext()).inflate(R.layout.information_mark_list, parent, false);
        mainHolder = new MainHolder(holderview, mItemClickListener);

        return mainHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onBindViewHolder(MainHolder mainHolder, int i){
        int position = i;
        mainHolder.name.setText(this.dtos.get(position).getRecommendTitle());
        mainHolder.add.setText(this.dtos.get(position).getRecommendAddress());
        mainHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData(dtos.get(position));
                removeItem(position);
                // information_mark.markNum.setText( getItemCount() + "건");
            }
        });

        GradientDrawable drawable = (GradientDrawable)context.getDrawable(R.drawable.community_edge);

        //이미지 넣기
        mainHolder.image.setBackground(drawable);
        mainHolder.image.setClipToOutline(true);

        Glide.with(context)
                .load(dtos.get(i).getRecommendImage())
                .into(mainHolder.image);
    }

    public int getItemCount(){
        return dtos.size();
    }

    public class MainHolder extends RecyclerView.ViewHolder{
        public TextView name, add;
        public ImageView image, delete;

        public MainHolder(View view, final OnItemClickEventListener listener){
            super(view);

            this.name = view.findViewById(R.id.mark_lv_place);
            this.add = view.findViewById(R.id.mark_lv_add);
            this.image = view.findViewById(R.id.mark_lv_image);
            this.delete = view.findViewById(R.id.mark_lv_delete);

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


    public void deleteData(RecommendDTO dto){

        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null)
            email = currentUser.getEmail();

        final DocumentReference documentReference = Firestore.collection("places").document(dto.getRecommendAreaCode()).collection("innerPlaces").document(dto.getRecommendId());
        final DocumentReference documentReferenceUser = Firestore.collection("users").document(email);

        //즐겨찾기 목록에 추가 - 유저
        documentReferenceUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                documentReferenceUser.update("userFavorite", FieldValue.arrayRemove(dto.getRecommendId()));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        //좋아요 사용자 목록에 추가 - 장소
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("RecommendLikeUser");
                documentReference.update("RecommendLikeUser", FieldValue.arrayRemove(email));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        //좋아요 여부에 따라 좋아요 수 조정해주기
        Firestore.runTransaction(new Transaction.Function<Double>() {
            @Nullable
            @Override
            public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(documentReference);
                ArrayList<String> group = (ArrayList<String>) snapshot.get("RecommendLikeUser");

                Double likenum = Double.valueOf(group.size());

                likenum = likenum - 1;
                transaction.update(documentReference, "RecommendLike", likenum);


                return likenum;
            }
        }).addOnSuccessListener(new OnSuccessListener<Double>() {
            @Override
            public void onSuccess(Double integer) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public void removeItem(int position){
        dtos.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }
}
