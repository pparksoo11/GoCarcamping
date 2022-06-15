package com.example.GoAutoCamping;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class CommentDTO {

    public String userId_comment;
    public String userNickName_comment;
    public String userProfile_comment;
    public String commentText_comment;


    public CommentDTO() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public CommentDTO(String uid, String profile, String author, String text) {
        this.userId_comment = uid;
        this.userProfile_comment = profile;
        this.userNickName_comment = author;
        this.commentText_comment = text;
    }

}
