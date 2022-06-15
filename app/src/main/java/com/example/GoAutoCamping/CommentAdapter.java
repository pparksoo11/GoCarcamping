package com.example.GoAutoCamping;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
/*
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.databinding.FragmentPostDetailBinding;
import com.google.firebase.quickstart.database.java.models.Comment;
import com.google.firebase.quickstart.database.java.models.Post;
import com.google.firebase.quickstart.database.java.models.User;
import com.google.firebase.quickstart.database.java.viewholder.CommentViewHolder;

 */

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private List<String> mCommentIds = new ArrayList<>();
    private List<CommentDTO> mComments = new ArrayList<>();

    //인터페이스
    public interface OnItemLongClickEventListener {
        void onItemLongClick(View a_view, int a_position, List<String> commentIds, List<CommentDTO> commentDTOS);
    }

    //인터페이스변수
    private OnItemLongClickEventListener mItemLongClickListener;

    //리스너명
    public void setOnItemLongClickListener(OnItemLongClickEventListener a_listener) {
        mItemLongClickListener = a_listener;
    }

    public CommentAdapter(final Context context, DatabaseReference ref) {
        mContext = context;
        mDatabaseReference = ref;

        // Create child event listener
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("TAG", "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                CommentDTO comment = dataSnapshot.getValue(CommentDTO.class);

                // Update RecyclerView
                mCommentIds.add(dataSnapshot.getKey());
                mComments.add(comment);
                notifyItemInserted(mComments.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d("TAG", "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                CommentDTO newComment = dataSnapshot.getValue(CommentDTO.class);
                String commentKey = dataSnapshot.getKey();

                int commentIndex = mCommentIds.indexOf(commentKey);
                if (commentIndex > -1) {
                    // Replace with the new data
                    mComments.set(commentIndex, newComment);

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

                int commentIndex = mCommentIds.indexOf(commentKey);
                if (commentIndex > -1) {
                    // Remove data from the list
                    mCommentIds.remove(commentIndex);
                    mComments.remove(commentIndex);

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
                CommentDTO movedComment = dataSnapshot.getValue(CommentDTO.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "postComments:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        ref.addChildEventListener(childEventListener);

        // Store reference to listener so it can be removed on app stop
        mChildEventListener = childEventListener;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.comment, parent, false);
        return new CommentViewHolder(view, mItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        CommentDTO comment = mComments.get(position);
        
        //작성자 닉네임
        holder.authorView.setText(comment.userNickName_comment);

        //작성자 프로필
        Glide.with(mContext)
                .load(comment.userProfile_comment)
                .into(holder.profileView);

        //작성글
        holder.bodyView.setText(comment.commentText_comment);
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView authorView;
        public TextView bodyView;
        public ImageView profileView;


        public CommentViewHolder(View itemView, final OnItemLongClickEventListener listener) {
            super(itemView);
            authorView = itemView.findViewById(R.id.commentAuthor);
            bodyView = itemView.findViewById(R.id.commentBody);
            profileView = itemView.findViewById(R.id.commentPhoto);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onItemLongClick(v, position, mCommentIds, mComments);
                    }

                    return false;
                }
            });
        }
    }

}





