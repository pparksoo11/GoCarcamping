package com.example.GoAutoCamping;

public class Recommend_filterdesDTO {

    private int RecommendFilterType = 0;
    private int RecommendFilterImage;
    private String RecommendFilterName;
    private String RecommendFilterDes;

    int[] filterImage = {R.drawable.place_mountain, R.drawable.place_river, R.drawable.place_ocean, R.drawable.place_valley,
                            R.drawable.place_campingcar, R.drawable.place_park, R.drawable.place_parkinglot};
    String[] filterName = { "산", "강", "바다", "계곡", "캠핑장", "공원", "주차장"};
    String[] filterDes = { "상쾌한 산 공기와 함께 차박을 즐길 수 있어요", "강을 보면서 차박을 즐겨보세요", "바다를 보면서 차박을 즐겨보세요",
                            "시원한 계곡과 함께 차박을 즐길 수 있어요", "캠핑 시설이 갖춰져 있어 차박하기에 좋아요",
                            "도심과 자연을 동시에 즐겨보세요", "주차 걱정 없이 차박을 즐겨보세요" };

    public Recommend_filterdesDTO(int recommendFilterType) {
        RecommendFilterType = recommendFilterType;

        setRecommendFilterName(filterName[RecommendFilterType]);
        setRecommendFilterDes(filterDes[RecommendFilterType]);
        setRecommendFilterImage(filterImage[RecommendFilterType]);
    }

    //getter
    public int getRecommendFilterImage() {
        return RecommendFilterImage;
    }

    public int getRecommendFilterType() {
        return RecommendFilterType;
    }

    public String getRecommendFilterName() {
        return RecommendFilterName;
    }

    public String getRecommendFilterDes() {
        return RecommendFilterDes;
    }

    //setters
    public void setRecommendFilterImage(int recommendFilterImage) {
        RecommendFilterImage = recommendFilterImage;
    }

    public void setRecommendFilterType(int recommendFilterType) {
        RecommendFilterType = recommendFilterType;
    }

    public void setRecommendFilterName(String recommendFilterName) {
        RecommendFilterName = recommendFilterName;
    }

    public void setRecommendFilterDes(String recommendFilterDes) {
        RecommendFilterDes = recommendFilterDes;
    }
}
