package com.example.GoAutoCamping;

public class Map_placesChoiceData {

    private String add;
    private  String roadAdd;
    private String title;


    //번지주소
    public String getAdd(){return add;}
    public void setAdd(String add){this.add = add;}

    //도로명주소
    public String getRoadAdd(){return roadAdd;}
    public void setRoadAdd(String roadAdd){this.roadAdd = roadAdd;}

    //이름
    public String getTitle(){return title;}
    public void setTitle(String title){this.title = title;}

    public Map_placesChoiceData(String add, String roadAdd, String title){
        this.title = title;
        this.add=add;
        this.roadAdd = roadAdd;
    }
}
