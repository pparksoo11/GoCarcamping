package com.example.GoAutoCamping;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class Home_foreCastManager extends Thread{

    String lon,lat;
    String dailyTemp, iconName,timezone, zone, date, week, address;
    ArrayList<ContentValues> mTotalValue;
    ContentValues mContent;
    Context mContext;
    Home home;
    RequestQueue queue;
    SimpleDateFormat format2 = new SimpleDateFormat( "yyyy년 MM월 dd일");
    Bitmap bitmap;
    DayOfWeek dayOfWeek;

    Date time = new Date();

    String time1 = format2.format(time);

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getDay(int i, String pattern) {

        //연동 정보 가져오기
        DateFormat dtf = new SimpleDateFormat(pattern);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, i);
        date = dtf.format(cal.getTime());
        String year = date.substring(0,4);
        String month = date.substring(5,7);
        String date2 = date.substring(8,10);

        LocalDate date3 = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(date2));
        dayOfWeek = date3.getDayOfWeek();

        //요일일
        week = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN);
    }

    public Home_foreCastManager(String lon, String lat, Context mContext, Home home, String ad)
    {
        this.lon = lon ; this.lat = lat;
        this.mContext = mContext;
        this.home = home;
        address = ad;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void GetOpenWeather(String lon, String lat)
    {

        mTotalValue = new ArrayList<ContentValues>();
        mContent = new ContentValues(); //데이터 넣기

        String key = "0c3e9509c2ecbaa1768de8ff17616424";

        String url = "http://api.openweathermap.org/data/2.5/onecall?"+
                "&APPID=" + key +
                "&lat="+lat+
                "&lon="+lon+
                "&mode=json" +
                "&units=metric"+
                "&lang=kr"+
                "&cnt=" + 5    ;

        getDay(0, "yyyy-MM-dd");

        //오늘 날짜
        home.tv_date.setText(time1 + " " + week);

        //openweather api 에서 필요한 날씨 정보를 json을 통해 갖고옴
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(JSONObject response) {
                try{

                    for(int i = 0; i < 5; i++){
                        JSONArray array = response.getJSONArray("daily");
                        JSONObject object = array.getJSONObject(i);
                        JSONObject temperature = object.getJSONObject("temp");
                        JSONArray weather = object.getJSONArray("weather");
                        JSONObject wobj = weather.getJSONObject(0);

                        //지역이름
                        timezone = response.getString("timezone");
                        int idx = timezone.indexOf("/");
                        zone = timezone.substring(idx+1);

                        //아이콘
                        iconName = String.valueOf(wobj.getString("icon"));

                        setImage();
                        getDay(i, "yyyy-MM-dd");

                        //아래 현재기온
                        dailyTemp = String.valueOf(temperature.getInt("day"));

                        switch (i){
                            case 0 : {
                                //gps 버튼을 누르지 않은 default 값
                                if(address.equals("서울특별시")) {
                                    home.tv_name.setText(address);
                                }
                                //gps 버튼을 눌렀을 때
                                else {
                                    String[] ad = address.split(" ");
                                    //국내라면 해당 도시 출력
                                    if(ad[0].equals("대한민국")) {
                                        address = ad[1];
                                        home.tv_name.setText(address);
                                    }
                                    //해외라면 timezone 출력
                                    else {
                                        home.tv_name.setText(zone);
                                    }
                                }

                                home.tv_temp.setText(dailyTemp + " °C");
                                home.imageV1.setImageBitmap(bitmap);
                                break;
                            }

                            //하루뒤
                            case 1 : {
                                home.tv_temp1.setText(dailyTemp + " °C");
                                home.imageV2.setImageBitmap(bitmap);
                                home.tv_date1.setText(week);
                                break;
                            }
                            //이틀뒤
                            case 2 : {
                                home.tv_temp2.setText(dailyTemp + " °C");
                                home.imageV3.setImageBitmap(bitmap);
                                home.tv_date2.setText(week);
                                break;
                            }
                            //삼일뒤
                            case 3 : {
                                home.tv_temp3.setText(dailyTemp + " °C");
                                home.imageV4.setImageBitmap(bitmap);
                                home.tv_date3.setText(week);
                                break;
                            }
                            //사일뒤
                            case 4 : {
                                home.tv_temp4.setText(dailyTemp + " °C");
                                home.imageV5.setImageBitmap(bitmap);
                                home.tv_date4.setText(week);
                                break;
                            }
                        }

                    }

                }catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        );

        queue = Volley.newRequestQueue(mContext);
        queue.add(jor);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        super.run();

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        GetOpenWeather(lon,lat);
        //mContext.handler.sendEmptyMessage(mContext.THREAD_HANDLER_SUCCESS_INFO);
        //Thread 작업 종료, UI 작업을 위해 MainHandler에 Message보냄    }
    }

    //날씨 아이콘 작업
    public void setImage() {
        switch (iconName) {
            case "01d": {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.w01d);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                break;
            }

            case "02d": {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.w02d);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                break;
            }

            case "03d": {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.w03d);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                break;
            }

            case "04d": {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.w04d);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                break;
            }

            case "09d": {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.w09d);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                break;
            }

            case "10d": {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.w10d);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                break;
            }

            case "11d": {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.w11d);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                break;
            }

            case "13d": {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.w13d);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                break;
            }

            case "50d": {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.w50d);
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                break;
            }

        }
    }
}
