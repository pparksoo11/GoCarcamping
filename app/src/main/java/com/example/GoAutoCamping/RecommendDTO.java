package com.example.GoAutoCamping;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class RecommendDTO {

    private String RecommendAreaCode = "";
    private String RecommendId = "";
    private String RecommendImage = "";
    private String RecommendTitle = "";
    private String RecommendAddress = "";
    private List<Boolean> RecommendFilter;
    private float RecommendStar = 0;
    private double RecommendLat = 0;
    private double RecommendLng = 0;
    private int RecommendLike = 0;
    private List<String> RecommendLikeUser;


    //getter
    public String getRecommendAreaCode() {
        return RecommendAreaCode;
    }

    public String getRecommendId() { return RecommendId; }

    public String getRecommendImage() {
        return RecommendImage;
    }

    public String getRecommendTitle() {
        return RecommendTitle;
    }

    public String getRecommendAddress() {
        return RecommendAddress;
    }

    public List<Boolean> getRecommendFilter() {
        return RecommendFilter;
    }

    public float getRecommendStar() {
        return RecommendStar;
    }

    public double getRecommendLat(){
        return RecommendLat;
    }

    public double getRecommendLng(){
        return RecommendLng;
    }
    public int getRecommendLike(){return RecommendLike;}
    public List<String> getRecommendLikeUser(){return  RecommendLikeUser;}



    //setter
    public void setRecommendAreaCode(String recommendAreaCode) {
        RecommendAreaCode = recommendAreaCode;
    }

    public void setRecommendId(String RecommendId) { this.RecommendId = RecommendId; }

    public void setRecommendImage(String RecommendImage) {
        this.RecommendImage = RecommendImage;
    }

    public void setRecommendTitle(String RecommendTitle) {
        this.RecommendTitle = RecommendTitle;
    }

    public void setRecommendAddress(String RecommendAddress) { this.RecommendAddress = RecommendAddress; }

    public void setRecommendFilter(List<Boolean> recommendFilter) {
        RecommendFilter = recommendFilter;
    }

    public void setRecommendStar(float RecommendStar){
        this.RecommendStar = RecommendStar;
    }
    public void setRecommendLat(double RecommendLat){
        this.RecommendLat = RecommendLat;
    }
    public void setRecommendLng(double RecommendLng){
        this.RecommendLng = RecommendLng;
    }
    public void setRecommendLike(int RecommendLike){this.RecommendLike = RecommendLike;}
    public void setRecommendLikeUser(List<String> RecommendLikeUser){this.RecommendLikeUser = RecommendLikeUser;}


}
