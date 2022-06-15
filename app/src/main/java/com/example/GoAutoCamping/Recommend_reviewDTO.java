package com.example.GoAutoCamping;

public class Recommend_reviewDTO {

    private String RecommendReviewId;
    private String RecommendReviewProfile;
    private String RecommendNickName;
    private String RecommendReviewComment;
    private float RecommendReviewStar;

    public Recommend_reviewDTO(){

    }

    public Recommend_reviewDTO(String RecommendReviewId, String RecommendReviewProfile, String RecommendNickName,
                               String RecommendReviewComment, float RecommendReviewStar) {

        setRecommendReviewId(RecommendReviewId);
        setRecommendReviewProfile(RecommendReviewProfile);
        setRecommendNickName(RecommendNickName);
        setRecommendReviewComment(RecommendReviewComment);
        setRecommendReviewStar(RecommendReviewStar);
    }

    //getter
    public String getRecommendReviewId() { return RecommendReviewId; }

    public String getRecommendReviewProfile() { return RecommendReviewProfile; }

    public String getRecommendNickName() {
        return RecommendNickName;
    }

    public String getRecommendReviewComment() {
        return RecommendReviewComment;
    }

    public float getRecommendReviewStar() {
        return RecommendReviewStar;
    }


    //setter
    public void setRecommendReviewId(String recommendReviewId) { RecommendReviewId = recommendReviewId; }

    public void setRecommendReviewProfile(String recommendReviewProfile) { RecommendReviewProfile = recommendReviewProfile; }

    public void setRecommendNickName(String recommendNickName) { RecommendNickName = recommendNickName; }

    public void setRecommendReviewComment(String recommendReviewComment) { RecommendReviewComment = recommendReviewComment; }

    public void setRecommendReviewStar(float recommendReviewStar) { RecommendReviewStar = recommendReviewStar; }


}
