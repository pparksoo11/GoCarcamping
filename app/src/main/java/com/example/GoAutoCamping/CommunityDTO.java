package com.example.GoAutoCamping;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class CommunityDTO {

    private String communityId;
    private String communityImage;
    private String communityContent;
    private String communityAddress;
    private String communityUserNickName;
    private int communityLike;
    private float communityStar;
    private String communityUserProfile;
    private Timestamp communityUploadTime;
    private ArrayList<String> communityLikeUser;
    private ArrayList<String> communityDeclear;
    private String communityAddress2;

    public String getCommunityAddress2() {
        return communityAddress2;
    }

    public ArrayList<String> getCommunityDeclear() {
        return communityDeclear;
    }

    public ArrayList<String> getCommunityLikeUser() {
        return communityLikeUser;
    }

    public String getCommunityId() {
        return communityId;
    }

    public Timestamp getCommunityUploadTime() {
        return communityUploadTime;
    }

    public String getCommunityAddress() {
        return communityAddress;
    }

    public String getCommunityContent() {
        return communityContent;
    }

    public String getCommunityImage() {
        return communityImage;
    }

    public String getCommunityUserNickName() {
        return communityUserNickName;
    }

    public String getCommunityUserProfile() {
        return communityUserProfile;
    }

    public int getCommunityLike() {
        return communityLike;
    }

    public float getCommunityStar() {
        return communityStar;
    }


    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public void setCommunityAddress(String communityAddress) {
        this.communityAddress = communityAddress;
    }

    public void setCommunityContent(String communityContent) {
        this.communityContent = communityContent;
    }

    public void setCommunityImage(String communityImage) {
        this.communityImage = communityImage;
    }

    public void setCommunityLike(int communityLike) {
        this.communityLike = communityLike;
    }

    public void setCommunityStar(float communityStar) {
        this.communityStar = communityStar;
    }

    public void setCommunityUserNickName(String communityUserNickName) {
        this.communityUserNickName = communityUserNickName;
    }

    public void setCommunityUserProfile(String communityUserProfile) {
        this.communityUserProfile = communityUserProfile;
    }

    public void setCommunityUploadTime(Timestamp communityUploadTime) {
        this.communityUploadTime = communityUploadTime;
    }

    public void setCommunityLikeUser(ArrayList<String> communityLikeUser) {
        this.communityLikeUser = communityLikeUser;
    }

    public void setCommunityDeclear(ArrayList<String> communityDeclear) {
        this.communityDeclear = communityDeclear;
    }

    public void setCommunityAddress2(String communityAddress2) {
        this.communityAddress2 = communityAddress2;
    }
}