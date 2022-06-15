package com.example.GoAutoCamping;

public class Recommend_hotDTO {

    private String RecommendImage;
    private String RecommendTitle = "";

    public Recommend_hotDTO(String recommendImage, String recommendTitle) {
        RecommendImage = recommendImage;
        RecommendTitle = recommendTitle;
    }

    public String  getRecommendImage() {
        return RecommendImage;
    }

    public String getRecommendTitle() {
        return RecommendTitle;
    }

    public void setRecommendImage(String RecommendImage) {
        this.RecommendImage = RecommendImage;
    }

    public void setRecommendTitle(String RecommendTitle) {
        this.RecommendTitle = RecommendTitle;
    }

}
