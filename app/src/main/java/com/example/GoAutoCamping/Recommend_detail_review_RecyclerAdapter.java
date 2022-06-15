package com.example.GoAutoCamping;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class Recommend_detail_review_RecyclerAdapter extends RecyclerView.Adapter<Recommend_detail_review_RecyclerAdapter.ItemViewHolder>{

    private Context context;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private List<String> mReviewIds = new ArrayList<>();
    List<Recommend_reviewDTO> reviewDTOS = new ArrayList<>();

    //인터페이스
    public interface OnItemLongClickEventListener {
        void onItemLongClick(View a_view, int a_position, List<String> mReviewIds, List<Recommend_reviewDTO> reviewDTOS);
    }

    //인터페이스변수
    private OnItemLongClickEventListener mItemLongClickListener;

    //리스너명
    public void setOnItemLongClickListener(OnItemLongClickEventListener a_listener) {
        mItemLongClickListener = a_listener;
    }

    public Recommend_detail_review_RecyclerAdapter(final Context context, DatabaseReference ref) {
        this.context = context;
        mDatabaseReference = ref;

        // Create child event listener
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("TAG", "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                Recommend_reviewDTO review = dataSnapshot.getValue(Recommend_reviewDTO.class);

                // Update RecyclerView
                mReviewIds.add(dataSnapshot.getKey());
                reviewDTOS.add(review);
                notifyItemInserted(reviewDTOS.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("TAG", "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                Recommend_reviewDTO newReview = dataSnapshot.getValue(Recommend_reviewDTO.class);
                String commentKey = dataSnapshot.getKey();

                int commentIndex = mReviewIds.indexOf(commentKey);
                if (commentIndex > -1) {
                    // Replace with the new data
                    reviewDTOS.set(commentIndex, newReview);

                    // Update the RecyclerView
                    notifyItemChanged(commentIndex);
                } else {
                    Log.w("TAG", "onChildChanged:unknown_child:" + commentKey);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("TAG", "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();

                int commentIndex = mReviewIds.indexOf(commentKey);
                if (commentIndex > -1) {
                    // Remove data from the list
                    mReviewIds.remove(commentIndex);
                    reviewDTOS.remove(commentIndex);

                    // Update the RecyclerView
                    notifyItemRemoved(commentIndex);
                } else {
                    Log.w("TAG", "onChildRemoved:unknown_child:" + commentKey);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("TAG", "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                Recommend_reviewDTO movedComment = dataSnapshot.getValue(Recommend_reviewDTO.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "postComments:onCancelled", databaseError.toException());
                Toast.makeText(context, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        ref.addChildEventListener(childEventListener);

        // Store reference to listener so it can be removed on app stop
        mChildEventListener = childEventListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recommend_detail_review, parent, false);
        return new ItemViewHolder(view, mItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Recommend_reviewDTO reviewDTO = reviewDTOS.get(position);

        holder.name.setText(reviewDTO.getRecommendNickName());
        holder.rate.setRating(reviewDTO.getRecommendReviewStar());
        holder.review.setText(reviewDTO.getRecommendReviewComment());

        //작성자 프로필
        Glide.with(context)
                .load(reviewDTO.getRecommendReviewProfile())
                .into(holder.image);

    }

    @Override
    public int getItemCount() {
        return reviewDTOS.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private ImageView image;
        private TextView name;
        private RatingBar rate;
        private TextView review;

        ItemViewHolder(View itemView, final OnItemLongClickEventListener listener){
            super (itemView);
            image = itemView.findViewById(R.id.reviewPhoto);
            name = itemView.findViewById(R.id.reviewAuthor);
            rate = itemView.findViewById(R.id.reviewStarRate);
            review = itemView.findViewById(R.id.reviewBody);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onItemLongClick(v, position, mReviewIds, reviewDTOS);
                    }

                    return false;
                }
            });

        }
    }
}
