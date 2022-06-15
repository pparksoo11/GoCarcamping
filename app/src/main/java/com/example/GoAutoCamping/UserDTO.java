package com.example.GoAutoCamping;

import java.util.ArrayList;
import java.util.List;

public class UserDTO {
    private String userProfile;
    private String userNickname;
    private String userName;
    private String userId;
    private String userPasswd;
    private String userPhone;
    private String userBirth;
    private List<String> userFavorite = new ArrayList<>();
    private List<String> userPosts = new ArrayList<>();



    //getter
    public String getUserProfile() {
        return userProfile;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserPasswd() {
        return userPasswd;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getUserBirth() {
        return userBirth;
    }

    public List<String> getUserFavorite() {
        return userFavorite;
    }

    public List<String> getUserPosts() {
        return userPosts;
    }

    //setter
    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserPasswd(String userPasswd) {
        this.userPasswd = userPasswd;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setUserBirth(String userBirth) {
        this.userBirth = userBirth;
    }

    public void setUserFavorite(List<String> userFavorite) {
        this.userFavorite = userFavorite;
    }

    public void setUserPosts(List<String> userPosts) {
        this.userPosts = userPosts;
    }
}
