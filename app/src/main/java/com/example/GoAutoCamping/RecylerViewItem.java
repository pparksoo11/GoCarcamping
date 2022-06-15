package com.example.GoAutoCamping;

public class RecylerViewItem {
    private String placeName;
    private String placeAdd;
    private int placeStar;

    //장소명
    public String getName(){
        return placeName;
    }
    public void setName(String _placeName){
        placeName = _placeName;
    }

    //장소 주소
    public String getAdd(){
        return placeAdd;
    }
    public void setAdd(String _placeAdd){
        placeAdd = _placeAdd;
    }

    //장소 별점점
    public int getStar(){
        return placeStar;
    }
    public void setStar(int _placeStar){
        placeStar = _placeStar;
    }
}
