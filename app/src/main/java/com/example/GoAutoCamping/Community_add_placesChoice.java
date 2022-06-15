package com.example.GoAutoCamping;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Community_add_placesChoice extends AppCompatActivity {

    EditText edit;
    Button placeAddBtn;

    RecyclerView rcyview;

    private String key = "PFNngBkh1Vt91yhDrUtn";
    private String Url = "https://geolocation.apigw.ntruss.com/geolocation/v2";

    private ArrayList<Map_placesChoiceData> list = new ArrayList<>();
    private Community_add_placesChoiceAdapter adapter = new Community_add_placesChoiceAdapter();

    String data;
    private float lat = 0, lng = 0;

    byte[] arr;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_placechoice);


        Toolbar toolbar = findViewById(R.id.placetoolBar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        edit = findViewById(R.id.edit);
        rcyview = findViewById(R.id.itemListView);
        placeAddBtn = findViewById(R.id.placeAddBtn);

        placeAddBtn.setVisibility(View.INVISIBLE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Community_add_placesChoice.this);
        rcyview.setLayoutManager(linearLayoutManager);
        rcyview.setAdapter(adapter);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //검색창 돋보기
        ImageButton button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = edit.getText().toString();
                loadingData(text);
                edit.setText("");
                imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
            }
        });

        //Intent inIntent = getIntent();
        //arr = inIntent.getByteArrayExtra("image");

        //검색창
        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        String text = edit.getText().toString();
                        loadingData(text); //검색어
                        edit.setText("");
                        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                        break;
                    default:
                        return false;
                }

                return true;
            }
        });

        //주소 목록
        adapter.setOnItemClickListener(new Community_add_placesChoiceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                String add = list.get(pos).getAdd();
                String title = list.get(pos).getTitle();
                getLatLng(add);

                sendIntent(add, title, lat, lng);
            }
        });

    }

    //선택한 add 화면으로 정보 넘겨줌
    public void sendIntent(String add, String ttitle, float latitude, float longitude){

        Intent inIntent = new Intent(Community_add_placesChoice.this, Community_add.class);
        inIntent.putExtra("Add2", add);
        inIntent.putExtra("Lat2", latitude);
        inIntent.putExtra("Lng2", longitude);
        inIntent.putExtra("title", ttitle.trim());
        //inIntent.putExtra("image", arr);

        setResult(10, inIntent);

        finish();
    }

    //네이버 api 주소 값 불러옴
    void loadingData(String text) {
        String keyword = text;
        Log.d("api test", "" + keyword);
        String cliendtId = "AV_CZ6YtcmCRLqNFYsdr";
        String clientSecret = "QbfH1iSYN9";
        if (!list.isEmpty()) adapter.removeItem();
        String search = null;
        try {
            search = URLEncoder.encode(keyword, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패", e);
        }

        int display = 5; //불러오는 값의 개수 최대 5
        String apiURL = "https://openapi.naver.com/v1/search/local.json?query=" + search + "&display=" + display + "&";
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", cliendtId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            int responseCode = con.getResponseCode();

            //네이버 api 접근 권한 받아옴
            BufferedReader br;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            StringBuffer sb = new StringBuffer();
            String line;

            //네이버 api 검색 결과 읽어옴
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            data = sb.toString();

            //읽어온 정보를 json으로 파싱
            JSONObject obj = new JSONObject(data);
            JSONArray jsonobj = (JSONArray) obj.get("items");

            String midTit = null, midAdd = null, midRAdd = null;


            for (int i = 0; i < jsonobj.length(); i++) {
                JSONObject temp = jsonobj.getJSONObject(i);
                midTit = temp.getString("title");
                //ㅇㄱ?
                midTit = midTit.replace("<br>", " ");
                midTit = midTit.replace("<b>", " ");
                midTit = midTit.replace("</b>", "");
                list.add(new Map_placesChoiceData(temp.getString("address"), temp.getString("roadAddress"), midTit)); //어댑터에 추가
            }


            adapter.setchoiceList(list);

            if(list.isEmpty()){
                Toast.makeText(this.getApplicationContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
            }

            br.close();
            con.disconnect();

        } catch (MalformedURLException e) {
            throw new RuntimeException("URL 연결이 잘못되었습니다.", e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패되었습니다.", e);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    //선택한 정보 주소명을 위경도로 변환 (변환할때도로명 x, 구번지 주소로 해야함)
    public void getLatLng(String address) {
        Log.d("locationTest", address);
        Geocoder geo = new Geocoder(getApplicationContext());
        List<Address> add = null;
        Location location = null;

        try {
            add = geo.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("location test", "주소 변환 오류");
        }

        if (add != null) {
            if (add.size() == 0) Log.d("location test", "주소 값 알 수 없음");
            else {
                lat = (float)add.get(0).getLatitude();
                lng = (float)add.get(0).getLongitude();

                for (int i = 0; i < add.size(); i++) {
                    Address latlng = add.get(i);
                    lat = (float)latlng.getLatitude(); //위도가져오기
                    lng = (float)latlng.getLongitude(); //경도 가져오기
                }
            }
        }
    }

    //툴바 뒤로가기
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
