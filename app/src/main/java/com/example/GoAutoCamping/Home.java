package com.example.GoAutoCamping;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Home extends Fragment {

    public static final int THREAD_HANDLER_SUCCESS_INFO = 1;
    Home_foreCastManager mForeCast;
    Home mThis;

    TextView myGps; //현재 위치
    View view;
    private Home_gpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    double latitude, longitude; //위도, 경도

    //날씨 정보 표시
    TextView tv_name, tv_date, tv_temp, tv_temp1, tv_temp2, tv_temp3, tv_temp4, tv_date1, tv_date2, tv_date3, tv_date4;
    ImageView imageV1, imageV2, imageV3, imageV4, imageV5;
    String lon = "127.0"; // 좌표 설정
    String lat = "37.583328";  // 좌표 설정

    RecyclerView recyclerView, recyclerView2, recyclerView3;
    Home_Adapter adapter;
    Home_Adapter2 adapter2, adapter3;
    List<Home_model> models;
    List<Home_model2> models2, models3;
    Context context;

    String address;

    String gpsAddress;

    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    boolean checking;
    int point; //가까운여행지

    ArrayList<CommunityDTO> dtos;
    ArrayList<SuppliesDTO> dtos2;
    CommunityDTO communityDTO;
    ArrayList<SuppliesDTO> suppliesDTOS2;
    ArrayList<homeDTO> homeImage;

    String category;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home, container, false);
        myGps = view.findViewById(R.id.myGps);
        tv_name = view.findViewById(R.id.tv_name);
        tv_date = view.findViewById(R.id.tv_date);
        tv_temp = view.findViewById(R.id.tv_temp);
        tv_temp1 = view.findViewById(R.id.tv_temp1);
        tv_temp2 = view.findViewById(R.id.tv_temp2);
        tv_temp3 = view.findViewById(R.id.tv_temp3);
        tv_temp4 = view.findViewById(R.id.tv_temp4);
        imageV1 = view.findViewById(R.id.imageV1);
        imageV2 = view.findViewById(R.id.imageV2);
        imageV3 = view.findViewById(R.id.imageV3);
        imageV4 = view.findViewById(R.id.imageV4);
        imageV5 = view.findViewById(R.id.imageV5);
        tv_date1 = view.findViewById(R.id.tv_date1);
        tv_date2 = view.findViewById(R.id.tv_date2);
        tv_date3 = view.findViewById(R.id.tv_date3);
        tv_date4 = view.findViewById(R.id.tv_date4);

        Bundle bundle = getArguments();

        if (bundle != null) {
            Log.d("번들", bundle.getInt("pop")+"");
            if(bundle.getInt("pop") == 0) {
                popup_main pm = new popup_main();
                pm.show(requireActivity().getSupportFragmentManager(), "tag");
            }
        }

        Log.d("체크", checking+"");
        if(!checking)
            Initialize("서울특별시");

        //gps버튼 클릭 시 내 위치정보 받아오기
        myGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsTracker = new Home_gpsTracker(getContext());

                //위도와 경도를 얻어옴(GPS 기능 이용)
                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();

                if (!checkLocationServicesStatus()) {
                    showDialogForLocationServiceSetting();
                }else {
                    checkRunTimePermission();
                }

                address = getCurrentAddress(latitude, longitude);
                myGps.setText(address);

                //db에 지역정보 추가
                addLocation();

                lat = Double.toString(latitude);
                lon = Double.toString(longitude);

                //주소 미발견이면 서울로 그대로 날씨 보여줌
                if(address.equals("주소 미발견") || address.equals("지오코더 서비스 사용불가") || address.equals("잘못된 GPS 좌표")) {
                    ;
                }
                else {
                    Initialize(address);
                    plusModel();
                }
            }
        });

        //리사이클러뷰
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView2 = view.findViewById(R.id.recyclerView2);
        recyclerView3 = view.findViewById(R.id.recyclerView3);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        LinearLayoutManager layoutManager3 = new LinearLayoutManager(getActivity());

        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager3.setOrientation(LinearLayoutManager.HORIZONTAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerView3.setLayoutManager(layoutManager3);

        Log.d("위치", myGps.getText().toString());

        //가까운 여행지 - 위치정보에 따라 유동적인 알고리즘
        plusModel();

        //용품 추천
        loadSupplies();

        //인기 게시물
        loadCommunity();

        return view;
    }

    //가까운 여행지 가져오기
    public void plusModel(){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        homeImage = new ArrayList<>();

        //용품 데이터
        Firestore.collection("home").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        homeDTO homeDTO = d.toObject(homeDTO.class);
                        homeImage.add(homeDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }

                    models = new ArrayList<>();

                    Log.d("gps2", myGps.getText().toString());
                    Log.d("포함",myGps.getText().toString().contains("인천")+"");
                    if (myGps.getText().toString().contains("서울") || myGps.getText().toString().contains("경기도") || myGps.getText().toString().contains("강원도") || myGps.getText().toString().contains("인천")) {
                        point = 0;
                        models.add(new Home_model(homeImage.get(9).getHomeImage(), "서울"));
                        models.add(new Home_model(homeImage.get(2).getHomeImage(), "경기도"));
                        models.add(new Home_model(homeImage.get(5).getHomeImage(), "강원도"));
                        models.add(new Home_model(homeImage.get(0).getHomeImage(), "충청북도"));
                        models.add(new Home_model(homeImage.get(1).getHomeImage(), "충청남도"));
                    } else if (myGps.getText().toString().contains("충청북도") || myGps.getText().toString().contains("충청남도") || myGps.getText().toString().contains("경상북도") || myGps.getText().toString().contains("대전") || myGps.getText().toString().contains("울산") || myGps.getText().toString().contains("대구")) {
                        point = 1;
                        models.add(new Home_model(homeImage.get(0).getHomeImage(), "충청북도"));
                        models.add(new Home_model(homeImage.get(1).getHomeImage(), "충청남도"));
                        models.add(new Home_model(homeImage.get(3).getHomeImage(), "경상북도"));
                        models.add(new Home_model(homeImage.get(7).getHomeImage(), "전라북도"));
                        models.add(new Home_model(homeImage.get(8).getHomeImage(), "전라남도"));
                    } else if (myGps.getText().toString().contains("전라북도") || myGps.getText().toString().contains("전라남도") || myGps.getText().toString().contains("경상남도") || myGps.getText().toString().contains("광주") || myGps.getText().toString().contains("부산")) {
                        point = 2;
                        models.add(new Home_model(homeImage.get(7).getHomeImage(), "전라북도"));
                        models.add(new Home_model(homeImage.get(8).getHomeImage(), "전라남도"));
                        models.add(new Home_model(homeImage.get(4).getHomeImage(), "경상남도"));
                        models.add(new Home_model(homeImage.get(3).getHomeImage(), "경상북도"));
                        models.add(new Home_model(homeImage.get(6).getHomeImage(), "제주도"));
                    }
                    else {
                        point = 3;
                        models.add(new Home_model(homeImage.get(9).getHomeImage(), "서울"));
                        models.add(new Home_model(homeImage.get(2).getHomeImage(), "경기도"));
                        models.add(new Home_model(homeImage.get(5).getHomeImage(), "강원도"));
                        models.add(new Home_model(homeImage.get(4).getHomeImage(), "경상남도"));
                        models.add(new Home_model(homeImage.get(6).getHomeImage(), "제주도"));
                    }

                    adapter = new Home_Adapter(models, context);

                    recyclerView.setAdapter(adapter);

                    if(point == 0) {
                        adapter.setOnItemClicklistener(new Home_OnItemClickListener() {
                            @Override
                            public void onItemClick(Home_Adapter.ItemViewHolder holder, View view, int pos) {
                                switch (pos) {
                                    case 0: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_seoul()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 1: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ggd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 2: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gwd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //충청북도
                                    case 3: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ccbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //충청남도
                                    case 4: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ccnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                }
                            }
                        });
                    }

                    else if(point == 1) {
                        adapter.setOnItemClicklistener(new Home_OnItemClickListener() {
                            @Override
                            public void onItemClick(Home_Adapter.ItemViewHolder holder, View view, int pos) {
                                switch (pos) {
                                    //충북
                                    case 0: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ccbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //충남
                                    case 1: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ccnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //경북
                                    case 2: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gsbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //전북
                                    case 3: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jlbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //전남
                                    case 4: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jlnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                }
                            }
                        });
                    }
                    else if(point == 2) {
                        adapter.setOnItemClicklistener(new Home_OnItemClickListener() {
                            @Override
                            public void onItemClick(Home_Adapter.ItemViewHolder holder, View view, int pos) {
                                switch (pos) {
                                    //전북
                                    case 0: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jlbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //전남
                                    case 1: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jlnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //경남
                                    case 2: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gsnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //경북
                                    case 3: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gsbd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    //제주
                                    case 4: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jeju()).addToBackStack(null).commit();
                                        break;
                                    }
                                }
                            }
                        });
                    }
                    else {
                        adapter.setOnItemClicklistener(new Home_OnItemClickListener() {
                            @Override
                            public void onItemClick(Home_Adapter.ItemViewHolder holder, View view, int pos) {
                                switch (pos) {
                                    case 0: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_seoul()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 1: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_ggd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 2: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gwd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 3: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_gsnd()).addToBackStack(null).commit();
                                        break;
                                    }
                                    case 4: {
                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend_jeju()).addToBackStack(null).commit();
                                        break;
                                    }
                                }
                            }
                        });
                    }
                }
                else {
                    Log.d("값 없음", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "");
            }
        });
    }

    //용품 데이터 가져오기
    public void loadSupplies(){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos2 = new ArrayList<>();

        suppliesDTOS2 = new ArrayList<>();

        models2 = new ArrayList<>();

        //light
        Firestore.collection("supplies").document("category_light").collection("posts")
                .orderBy("post_like", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        SuppliesDTO suppliesDTO = d.toObject(SuppliesDTO.class);
                        suppliesDTO.setPost_id(d.getId());
                        dtos2.add(suppliesDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }

                    models2.add(new Home_model2(dtos2.get(0).getPost_Image(), dtos2.get(0).getPost_name()));
                    suppliesDTOS2.add(dtos2.get(0));
                    dtos2.clear();

                    Firestore.collection("supplies").document("category_cooking").collection("posts")
                            .orderBy("post_like", Query.Direction.DESCENDING)
                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                    {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(!queryDocumentSnapshots.isEmpty()){

                                //도큐먼트 리스트 생성
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                                for(DocumentSnapshot d : list){
                                    SuppliesDTO suppliesDTO = d.toObject(SuppliesDTO.class);
                                    suppliesDTO.setPost_id(d.getId());
                                    dtos2.add(suppliesDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                                }

                                models2.add(new Home_model2(dtos2.get(0).getPost_Image(), dtos2.get(0).getPost_name()));
                                models2.add(new Home_model2(dtos2.get(1).getPost_Image(), dtos2.get(1).getPost_name()));
                                suppliesDTOS2.add(dtos2.get(0));
                                suppliesDTOS2.add(dtos2.get(1));
                                dtos2.clear();

                                Firestore.collection("supplies").document("category_living").collection("posts")
                                        .orderBy("post_like", Query.Direction.DESCENDING)
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                                {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if(!queryDocumentSnapshots.isEmpty()){

                                            //도큐먼트 리스트 생성
                                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                            //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                                            for(DocumentSnapshot d : list){
                                                SuppliesDTO suppliesDTO = d.toObject(SuppliesDTO.class);
                                                suppliesDTO.setPost_id(d.getId());
                                                dtos2.add(suppliesDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                                            }

                                            models2.add(new Home_model2(dtos2.get(0).getPost_Image(), dtos2.get(0).getPost_name()));
                                            suppliesDTOS2.add(dtos2.get(0));
                                            dtos2.clear();

                                            Firestore.collection("supplies").document("category_etc").collection("posts")
                                                    .orderBy("post_like", Query.Direction.DESCENDING)
                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                                            {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    if(!queryDocumentSnapshots.isEmpty()){

                                                        //도큐먼트 리스트 생성
                                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                                        //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                                                        for(DocumentSnapshot d : list){
                                                            SuppliesDTO suppliesDTO = d.toObject(SuppliesDTO.class);
                                                            suppliesDTO.setPost_id(d.getId());
                                                            dtos2.add(suppliesDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                                                        }

                                                        models2.add(new Home_model2(dtos2.get(0).getPost_Image(), dtos2.get(0).getPost_name()));
                                                        suppliesDTOS2.add(dtos2.get(0));

                                                        //어탭터 생성 및 setAdapter
                                                        adapter2 = new Home_Adapter2(models2, context);
                                                        recyclerView2.setAdapter(adapter2);

                                                        //클릭 시 화면전환이벤트 처리
                                                        adapter2.setOnItemClicklistener(new Home_OnItemClickListener2() {
                                                            @Override
                                                            public void onItemClick(Home_Adapter2.ItemViewHolder holder, View view, int pos) {
                                                                switch (pos) {
                                                                    case 0 : {
                                                                        category = "category_light";
                                                                        break;
                                                                    }
                                                                    case 1 : {
                                                                        category = "category_cooking";
                                                                        break;
                                                                    }
                                                                    case 2 : {
                                                                        category = "category_cooking";
                                                                        break;
                                                                    }
                                                                    case 3 : {
                                                                        category = "category_living";
                                                                        break;
                                                                    }
                                                                    case 4 : {
                                                                        category = "category_etc";
                                                                        break;
                                                                    }

                                                                }

                                                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                                                Fragment fragment = new Supplies_Detail();

                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("supplyKind", category);
                                                                bundle.putString("postId", suppliesDTOS2.get(pos).getPost_id());

                                                                fragment.setArguments(bundle);
                                                                transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
                                                            }
                                                        });
                                                    }
                                                    else {
                                                        Log.d("값 없음", "");
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("실패함", "");
                                                }
                                            });
                                        }
                                        else {
                                            Log.d("값 없음", "");
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("실패함", "");
                                    }
                                });
                            }
                            else {
                                Log.d("값 없음", "");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("실패함", "");
                        }
                    });
                }
                else {
                    Log.d("값 없음", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "");
            }
        });
    }

    //커뮤니티 데이터 가져오기
    public void loadCommunity(){
        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        //커뮤니티의 모든 데이터 가져오기 - onComplete가 안먹어서 Success로 바꿨음 그에 따라 아래 방법도 바뀜
        Firestore.collection("communication").orderBy("communityLike", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    Log.d("실행중2", "실행중2");
                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        communityDTO = d.toObject(CommunityDTO.class);
                        communityDTO.setCommunityId(d.getId());
                        Log.d("실행중", "실행중");
                        dtos.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }

                    models3 = new ArrayList<>();
                    //TODO 나중에 이부분 5로 바꿔서 5개보여주기
                    for(int i=0; i<5; i++) {
                        models3.add(new Home_model2(dtos.get(i).getCommunityImage(), dtos.get(i).getCommunityAddress()));
                    }

                    //어탭터 생성 및 setAdapter
                    adapter3 = new Home_Adapter2(models3, context);
                    recyclerView3.setAdapter(adapter3);

                    adapter3.setOnItemClicklistener(new Home_OnItemClickListener2() {
                        @Override
                        public void onItemClick(Home_Adapter2.ItemViewHolder holder, View view, int pos) {
                            openCommuDetail(pos);
                        }
                    });
                }
                else {
                    Log.d("값 없음", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "");
            }
        });
    }

    public void openCommuDetail(int pos) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment fragment = new Community_detail(pos);

        Bundle bundle = new Bundle();
        bundle.putString("postId", dtos.get(pos).getCommunityId());
        bundle.putString("home", "home");

        fragment.setArguments(bundle);
        transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result ) {
                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getContext(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }
    public String getCurrentAddress( double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude,7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(getContext(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getContext(), "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getContext(), "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }
    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
        dialog.title(null, "GPS 활성화");
        dialog.message(null, "GPS 기능을 활성화하시겠습니까?", null);
        dialog.positiveButton(null, "활성화", materialDialog -> {
            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            return null;
        });
        dialog.negativeButton(null, "아니요", materialDialog -> {
            dialog.dismiss();
            return null;
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //날씨
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void Initialize(String address)
    {
        String ad = address;
        mThis = this;
        mForeCast = new Home_foreCastManager(lon, lat, context, mThis, ad);
        mForeCast.run();
    }

    public void addLocation() {
        if(myGps.getText().equals("주소 미발견")) {

        }
        //주소 미발견일 때는 입력 안해줌.
        else {
            LocationDTO locationDTO = new LocationDTO();

            locationDTO.setUserId(email);
            locationDTO.setUserAddress(address);
            locationDTO.setLon(longitude);
            locationDTO.setLat(latitude);

            if(email != null) {
                Firestore.collection("Location").document(email).set(locationDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if(activity!=null){
            ((MainActivity)activity).setBackBtn(0,false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();

        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();

            Firestore = FirebaseFirestore.getInstance();

            Firestore.collection("Location").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        Log.d("실행중2", "실행중2");
                        //도큐먼트 리스트 생성
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                        //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                        for (DocumentSnapshot d : list) {
                            Log.d("아이디", d.getId());
                            if(d.getId().equals(email)) {
                                checking = true;
                                Log.d("아이디1",checking+"");
                                Firestore.collection("Location").document(email).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        LocationDTO locationDTO = documentSnapshot.toObject(LocationDTO.class);
                                        gpsAddress = locationDTO.getUserAddress();
                                        Log.d("주소명명",gpsAddress);
                                        myGps.setText(gpsAddress);
                                        plusModel();
                                        String ad = locationDTO.getUserAddress();
                                        mForeCast = new Home_foreCastManager(Double.toString(locationDTO.getLon()), Double.toString(locationDTO.getLat()), context, mThis, ad);
                                        mForeCast.run();
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;

        mThis = this;

    }
}