package com.example.GoAutoCamping;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.view.WindowCallbackWrapper;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Map_searchProgress extends DialogFragment {

    private double Lat = 0, Lng = 0;
    private String tit = null;
    private static double BASEDISTANCE = 1000000000;

    ProgressBar progressBar;
    ImageView carImgNot, cardImg, carImgOk;
    TextView tv, ment;
    CardView cardLayout;
    TextView cardTitle, cardAdd, cardDis;

    //파이어베이스
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    ArrayList<RecommendDTO> dtos;

    View view;
    public Map_searchProgress() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_searchprogress, container, false);

        load();
        setCancelable(false);

        carImgOk = view.findViewById(R.id.carImgOk);
        carImgNot = view.findViewById(R.id.carImgNot);
        progressBar = view.findViewById(R.id.progressBar);
        ment = view.findViewById(R.id.ment);
        ImageView closeBtn = view.findViewById(R.id.closeBtn);

        cardLayout = view.findViewById(R.id.cardLayout);
        tv = view.findViewById(R.id.tv);

        initializeCardView();

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //searchDialog에서 전달해준 bundle 값 넘겨 받음
        //차박 검색 대상 <위도, 경도, 장소명>
        Lat = getArguments().getDouble("Latitude"); //위도
        Lng = getArguments().getDouble("Longitude"); //경도
        tit = getArguments().getString("title"); //장소명


        Log.d("값 넘겨주기", "타이틀" + tit);
        Log.d("값 넘겨주기", "위도" + Lat);
        Log.d("값 넘겨주기", "경도" + Lng);

    }

    public void initializeCardView(){
        cardLayout.setVisibility(View.INVISIBLE);
        cardImg = cardLayout.findViewById(R.id.cardImg);
        cardTitle = cardLayout.findViewById(R.id.cardTitle);
        cardAdd = cardLayout.findViewById(R.id.cardAdd);
        cardDis = cardLayout.findViewById(R.id.cardDis);
    }

    public void load() {
        Firestore = FirebaseFirestore.getInstance();

        dtos = new ArrayList<>();
        //장소 추천 데이터 - 별점 순
        Firestore.collectionGroup("innerPlaces")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for (DocumentSnapshot d : list) {
                        RecommendDTO recommendDTO = d.toObject(RecommendDTO.class);
                        recommendDTO.setRecommendId(d.getId());

                        Log.d("값 가져와짐?", d.getId());

                        dtos.add(recommendDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }
                    findData(tit, Lat, Lng);
                    //thread.start();
                } else {
                    Log.d("값 없음", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "");
            }
        });//firestore loading

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void findData(String tittt, double latt, double lngg) {
        Log.d("progressTest", "findDataFunc" + dtos.size());

        Location locationNow = new Location("now"); //차박 검색 위치
        locationNow.setLatitude(latt);
        locationNow.setLongitude(lngg);

        Location locationFind; //차박 비교 위치
        double distance = 0;
        String shortTitle = "";
        String shortAdd = "";
        String shortImg = "";
        boolean imgFlag = false;

        Location locationmid = new Location("");
        locationmid.setLatitude(dtos.get(0).getRecommendLat());
        locationmid.setLongitude(dtos.get(0).getRecommendLng());
        distance = locationNow.distanceTo(locationmid);

        String title = "";
        //차박 조건 검색
        for (int i = 0; i < dtos.size(); i++) {
            title = dtos.get(i).getRecommendTitle();
            if(tittt.contains(title)){
                Log.d("distance Test", "타이틀 같음");
                imgFlag = true;
            }else{
                locationFind = new Location("");
                locationFind.setLatitude(dtos.get(i).getRecommendLat());
                locationFind.setLongitude(dtos.get(i).getRecommendLng());

                double getDis = locationNow.distanceTo(locationFind); //M단위 /1000 : km 단위
                //차박 검색 위치랑 가장 가까운 차박 위치 가져옴
                if(getDis < distance){
                    distance = getDis;
                    shortTitle = dtos.get(i).getRecommendTitle();
                    shortAdd = dtos.get(i).getRecommendAddress();
                    shortImg = dtos.get(i).getRecommendImage();
                    Log.d("distanceTest", "더 가까운 장소" + shortTitle);
                }else{
                    Log.d("distanceTest","먼 장소");
                }
            }
        }

        if(imgFlag){
            setTrueUiState();
        }else{
            setCardView(distance, shortTitle, shortAdd, shortImg);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setCardView(double shortDis, String shortTitle, String shortAdd, String shortImg){
        ment.setText("차박이 불가능한 장소입니다!");
        tv.setVisibility(View.VISIBLE);
        String mentTxt = "";
        String disTxt = "";
        if(shortDis <= 1000){
            mentTxt = "찾고 있는 장소가 이곳인가요?";
            disTxt = String.valueOf(shortDis) + "m 내";
        }else if(shortDis <= 5000){
            mentTxt = "근처에 차박이 가능한 장소가 있습니다";
            disTxt = String.valueOf(shortDis / 1000) + "km 내";
        }else{
            mentTxt = "추천하는 차박 장소입니다!";
            disTxt = String.valueOf(shortDis / 1000) + "km 내";
        }

        GradientDrawable drawable = (GradientDrawable)getContext().getDrawable(R.drawable.community_edge);
        //이미지 넣기
        cardImg.setBackground(drawable);
        cardImg.setClipToOutline(true);
        Glide.with(getContext())
                .load(shortImg)
                .into(cardImg);
        cardTitle.setText(shortTitle);
        tv.setText(mentTxt);
        cardAdd.setText(shortAdd);
        cardDis.setText(disTxt);
        progressBar.setVisibility(View.GONE);
        carImgOk.setVisibility(View.GONE);
        carImgNot.setVisibility(View.VISIBLE);

        cardLayout.setVisibility(View.VISIBLE);
    }

    public void setTrueUiState() {
        progressBar.setVisibility(View.GONE);
        carImgOk.setVisibility(View.VISIBLE);
        carImgNot.setVisibility(View.GONE);
        cardLayout.setVisibility(View.GONE);
        ment.setText("차박이 가능한 장소입니다!");
    }

    @Override
    public void onStart() {
        super.onStart();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getDialog().getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        Window window = getDialog().getWindow();
        window.setAttributes(lp);
    }
}

