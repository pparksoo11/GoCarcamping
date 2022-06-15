package com.example.GoAutoCamping;

public class Map_placedata {

    private String name; //장소이름
    private String add; //장소 주소
    private double lat; //장소 경도
    private double lan; //장소 위도
    private float rate; //별점
    private int num; //숫자
    private int like = 0;
    private String recPlaceName;
    private String recPlaceId;
    private String image = "";


    public Map_placedata(String name, String add, double lat, double lng, float rate, String recPlaceName, String recPlaceId, int like, String image){
        this.name = name;
        this.add = add;
        this.lat = lat;
        this.lan = lng;
        this.rate = rate;
        this.recPlaceName = recPlaceName;
        this.recPlaceId = recPlaceId;
        this.like = like;
        this.image = image;
    }


    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getAdd(){
        return add;
    }
    public void setAdd(String add){
        this.add = add;
    }

    public double getLat(){
        return lat;
    }
    public void setLat(float lat){
        this.lat = lat;
    }

    public double getLan(){
        return lan;
    }
    public void setLan(float lan){
        this.lan = lan;
    }

    public float getRate(){return rate;}
    public void setRate(float rate){this.rate = rate;}

    public int getNum(){return num;}
    public void setNum(int num){this.num = num;}

    public int getLike(){return like;}
    public void setLike(int like){this.like = like;}

    public String getRecPlaceName(){return recPlaceName;}
    public void setRecPlaceName(String recPlaceName){this.recPlaceName = recPlaceName;}

    public String getRecPlaceId(){return recPlaceId;}
    public void setRecPlaceId(String recPlaceId){this.recPlaceId = recPlaceId;}

    public String getImage(){return image;}
    public void setImage(String image){this.image = image;}

}
