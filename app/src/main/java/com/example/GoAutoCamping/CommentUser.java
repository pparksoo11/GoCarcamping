package com.example.GoAutoCamping;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class CommentUser {

    public String username;
    public String email;

    public CommentUser() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public CommentUser(String username, String email) {
        this.username = username;
        this.email = email;
    }

}
