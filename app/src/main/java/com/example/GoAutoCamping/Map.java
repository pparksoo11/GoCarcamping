package com.example.GoAutoCamping;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.MapView;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Map extends Fragment implements Map_filterdialog.InputSelected, OnMapReadyCallback {
    final int CAMERA_ZOOM_LEVEL = 13;
    final int MARKER_SIZE = 80;
    final double NORTH_LATITUDE = 38.58742;
    final double SOUTH_LATITUDE = 33.112585;
    final double WEST_LONGITUDE = 124.608107;
    final double EAST_LONGITUDE = 131.872743;
    //implements OnMapReadyCallback,Map_filterdialog.InputSelected
    Map_RecyclerAdapter adapter;
    RecyclerView mapRecyclerView;
    FloatingActionButton mapFab, filterFab, mainFab, locationFab;
    Button listBtn, searchMidBtn;
    Boolean enterFlag = false, openFlag = false; //mapFlag : 지도 상태 btnFlag : 필터 버튼 상태
    View list_bs = null, coordinatorLayout = null;
    BottomSheetBehavior list_behav = null;
    CardView cardview;
    EditText searchView;
    Bundle searchArg;
    ConstraintLayout fl;
    FrameLayout sear, totCard;
    ImageButton sBtn;
    String word, recPlaceName, recPlaceId;
    List<Map_placedata> placedata = new ArrayList<>();
    List<Map_placedata> filterData = new ArrayList<>();
    List<Map_placedata> localData = new ArrayList<>();

    MapView sView = null;
    NaverMap naverMap;

    ArrayList<Marker> markerList = new ArrayList<>();
    int maxHeight = 0, minHeight = 0, mapFlag = 0;

    //필터
    public boolean[] checked = {false, false, false, false, false, false, false, false, false, false, false};
    boolean[] checkedLoc = {false, false, false, false, false, false, false, false, false, false};
    String[] checkingName = {"산", "강", "바다", "계곡", "캠핑장", "공원", "주차장", "화장실", "샤워실", "매점", "취사"};
    String[] checkedLocName = {"서울", "경기도", "강원도", "충청북도", "충청남도", "경상북도", "경상남도", "전라북도", "전라남도", "제주도"};

    HorizontalScrollView chipLayout, bschipLayout;
    ChipGroup chipGroup, bschipGroup;
    Context context;

    //파이어베이스
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    ArrayList<RecommendDTO> dtos;
    ArrayList<String> filter_name = new ArrayList<>();
    ArrayList<String> local_name = new ArrayList<>();
    ArrayList<Double> local_lat = new ArrayList<>();
    ArrayList<Double> local_lng = new ArrayList<>();
    Marker detailMarker, locMarker = new Marker(), detMarker = new Marker();

    //위도 경도 받아오기
    private Home_gpsTracker gpsTracker;
    double latitude = 37.58482502367129, longitude = 126.92520885572567; //위도, 경도

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    // private FusedLocationSource mLocationSource;

    public static Map newInstance() {
        return new Map();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //하단바 숨기기
        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(false);

        coordinatorLayout = inflater.inflate(R.layout.map, container, false);
        fl = coordinatorLayout.findViewById(R.id.mapLayout);
        sear = coordinatorLayout.findViewById(R.id.searLayout);

        load();//load(); //파이어베이스 장소 정보 불러옴

        //지도
        sView = coordinatorLayout.findViewById(R.id.map);
        sView.onCreate(savedInstanceState);
        //mLocationSource = new FusedLocationSource(this,PERMISSIONS_REQUEST_CODE);
        mapFlag = 0;
        sView.getMapAsync(this::onMapReady);
        getCurrentLocation();
        //바텀시트
        minHeight = ((MainActivity) getActivity()).pointY; //메인 액티비티 바텀 네비게이션 높이 값
        list_bs = coordinatorLayout.findViewById(R.id.list_bottomSheet); //목록
        initializeListBottomSheet(); //바텀시트
        initializeCardView(); //카드뷰
        settingFab(); //메뉴


        sBtn = coordinatorLayout.findViewById(R.id.sBtn);
        sBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                word = searchView.getText().toString();
                int count = chipGroup.getChildCount();

                if (word.isEmpty() && count == 0) {
                    // 검색어 x 지역 x 장소 x
                } else {
                    searchWord(word, checkedLoc, checked);

                }
                searchView.setText("");
            }
        });

        searchView = coordinatorLayout.findViewById(R.id.searchView);
        //todo : 키코드 수정 필요s
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    word = searchView.getText().toString();
                    int count = chipGroup.getChildCount();

                    if (word.isEmpty() && count == 0) {
                        // 검색어 x 지역 x 장소 x
                    } else {
                        searchWord(word, checkedLoc, checked);
                    }
                    searchView.setText("");
                }
                return true;
            }
        });

        return coordinatorLayout;
    }


    //카드뷰(장소 디테일)
    private void initializeCardView() {
        cardview = coordinatorLayout.findViewById(R.id.card_view);
        totCard = coordinatorLayout.findViewById(R.id.totCardView);
        cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //디테일 장소 화면으로 이동
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment fragment = new Recommend_detail();

                enterFlag = true;
                Bundle bbundle = new Bundle();
                bbundle.putString("placeName", recPlaceName);
                bbundle.putString("placeId", recPlaceId);

                fragment.setArguments(bbundle);
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.enter_from_right, R.anim.enter_from_right, R.anim.exit_from_right);
                transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
            }
        });

        //리스트로 돌아가는 버튼
        listBtn = coordinatorLayout.findViewById(R.id.listBtn);
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (placedata == null || placedata.isEmpty() || placedata.size() == 0) {

                } else {
                    //검색
                    totCard.setVisibility(View.GONE);
                    mapFlag = 1;
                    sView.getMapAsync(Map.this::onMapReady);
                    totBsState(true);
                }
            }
        });
        //검색 editText 띄어줌
        searchMidBtn = coordinatorLayout.findViewById(R.id.searchMidBtn);
        searchMidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totCard.setVisibility(View.GONE);
                list_behav.setState(BottomSheetBehavior.STATE_HIDDEN);
                fabVisibility(true);
                mapFlag = 2;
                sView.getMapAsync(Map.this);
                sear.clearAnimation();

            }
        });
    }

    private void settingFab() {

        if(!placedata.isEmpty()){
            fabVisibility(false);
        }

        filterFab = coordinatorLayout.findViewById(R.id.filterFab); //필터검색버튼
        locationFab = coordinatorLayout.findViewById(R.id.locaFab); //현위치 표시 버튼
        mapFab = coordinatorLayout.findViewById(R.id.mapFab); //차박 가능 검색 버튼
        mainFab = coordinatorLayout.findViewById(R.id.mainFab); //메뉴 버튼 -> 애니메이션 연결
        mainFab.bringToFront();
        mainFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!openFlag) { //닫혀있을때
                    fabListener(false);
                    openFlag = true;
                } else { // 열려있을때
                    fabListener(true);
                    openFlag = false;
                }
            }
        });

        Map_searchDialog msd = new Map_searchDialog();

        //차박 가능 검색 다이얼로그 (->Map_searchDialog)
        mapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabListener(openFlag);
                mapFlag = 0;
                searchArg = new Bundle();
                searchArg.putDouble("currentLatitude", latitude);
                searchArg.putDouble("currentLongitude", longitude);

                sView.getMapAsync(Map.this);
                msd.setArguments(searchArg);
                msd.show(requireActivity().getSupportFragmentManager(), "tag");
            }
        });

        //현위치 기능
        locationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabListener(openFlag);
                getCurrentLocation();
            }
        });

        //필터 칩
        chipLayout = coordinatorLayout.findViewById(R.id.chip_Layout);
        chipGroup = coordinatorLayout.findViewById(R.id.chip_group);
        //필터 다이얼로그
        Map_filterdialog dlg = new Map_filterdialog();
        //필터 보여주는 기능(->Recommend_detail_filterdialog)
        filterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainFab.bringToFront();
                fabListener(openFlag);
                dlg.setTargetFragment(Map.this, 1);
                dlg.show(getActivity().getSupportFragmentManager(), "tag");
            }
        });


    }

    public void getCurrentLocation() {
        gpsTracker = new Home_gpsTracker(getContext());

        //위도와 경도를 얻어옴(GPS 기능 이용)
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
            Log.d("위도", latitude + "");
            Log.d("경도", longitude + "");

            if(latitude > NORTH_LATITUDE || latitude < SOUTH_LATITUDE || longitude < WEST_LONGITUDE || longitude > EAST_LONGITUDE) {
                //경도값 초과, 위도값 초과
                Toast.makeText(this.getContext(), " 위치 오류로 기본 위치로 지정 됩니다 ", Toast.LENGTH_SHORT).show(); //todo: 스낵바로 변경
                latitude = 37.58482502367129;
                longitude = 126.92520885572567;
            }
            mapFlag = 0;
            sView.getMapAsync(this::onMapReady);
        }
    }

    public void searchTest() {

        boolean firebaseFlag = false;// false : 지역 필터만 존재  true : 장소, 시설 필터 존재
        if (chipGroup.getChildCount() > 0) {
            for (int i = 0; i < checked.length; i++) {
                //시설, 지역 배열이 하나라도 존재하는지 검사
                if (checked[i]) firebaseFlag = true;
            }
        }
        if (firebaseFlag) {
            //장소, 시설 필터 존재
            for (int i = 0; i < checked.length; i++)
                Log.d("filterTest", "테스트 " + i + "번째" + checked[i]);
            //filterLoad(checked);

        }
        //장소, 시설 필터 존재하지 않음
    }


    //장소추천 데이터 가져오기
    public void load() {
        Firestore = FirebaseFirestore.getInstance();

        // if (!dtos.isEmpty()) dtos.clear();
        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        //장소 추천 데이터 - 별점 순
        Firestore.collectionGroup("innerPlaces")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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

                    // if(!mapDtos.isEmpty()) mapDtos.clear();
                    Log.d("filterTest", "db크기 load 함수" + dtos.size());
                } else {
                    Log.d("값 없음", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "");
            }
        });
        Log.d("filterTest", "db크기 load 함수2  " + dtos.size());


    }

    @Override
    public void clearAll() {
        clearChip();
    }

    //chip boolean, name 정보 받아옴(<- filterdialog)
    @Override
    public void sendBoolenArray(boolean[] ch, String[] chName, boolean[] chLoc, String[] chLocName) {
        //ch, chName : 시설 필터 불린, 이름
        //chLoc, chLocName : 전국팔도 필터 불림, 이름
        ArrayList<String> checkName = new ArrayList<String>();

        for (int i = 0; i < ch.length; i++) {
            checked[i] = ch[i];
        }
        for (int i = 0; i < chLoc.length; i++) {
            checkedLoc[i] = chLoc[i];
        }

        for (int i = 0; i < checked.length; i++) {
            Log.d("살려주세요", i + "번째 " + checked[i]);

            if (checked[i])
                checkName.add(chName[i]);
        }
        for (int i = 0; i < checkedLoc.length; i++) {
            if (checkedLoc[i]) checkName.add(checkedLocName[i]);
        }

        clearChip();
        if (checkChipGroup())
            clearChip();

        addChipView(checkName);
    }


    //칩
    public boolean checkChipGroup() {
        boolean tf = false;
        int i = chipGroup.getChildCount();
        Log.d("칩그룹에 있습니까?", i + "개 있음");

        clearChip();
        if (i == 0)
            tf = true;

        return tf;

    }

    public void addChipView(ArrayList<String> name) {
        for (int i = 0; i < name.size(); i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.filter_chip_layout, chipGroup, false);
            chip.setText(name.get(i));

            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeChip(chip);
                }
            });

            chipGroup.addView(chip);

            searchTest();
        }
    }

    //칩 초기화
    public void clearChip() {
        int count = chipGroup.getChildCount();

        Log.d("몇개?", chipGroup.getChildCount() + "게");

        for (int i = count - 1; i > -1; i--) {
            Log.d("몇번 돕니까?", i + "번");
            Chip chip = (Chip) chipGroup.getChildAt(i);
            removeChip(chip);
        }
    }

    //칩 삭제
    public void removeChip(Chip chip) {
        if (chip == null) return;

        for (int i = 0; i < checkingName.length; i++) {
            if (checkingName[i].equals(chip.getText()))
                checked[i] = false;
        }
        for (int i = 0; i < checkedLocName.length; i++) {
            if (checkedLocName[i].equals(chip.getText()))
                checkedLoc[i] = false;
        }

        chipGroup.removeView(chip);

        searchTest();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;
    }

    //지역 이름 db저장 명으로 변환
    public String changeLocationName(String locationCode) {
        String areacode = "";
        switch (locationCode) {
            case "서울":
                areacode = "place_seoul";
                break;
            case "경기도":
                areacode = "place_ggd";
                break;
            case "강원도":
                areacode = "place_gwd";
                break;
            case "충청남도":
                areacode = "place_ccnd";
                break;
            case "충청북도":
                areacode = "place_ccbd";
                break;
            case "경상남도":
                areacode = "place_gsnd";
                break;
            case "경상북도":
                areacode = "place_gsbd";
                break;
            case "전라남도":
                areacode = "place_jlnd";
                break;
            case "전라북도":
                areacode = "place_jlbd";
                break;
            case "제주도":
                areacode = "place_zzd";
                break;
        }

        Log.d("filterTest", "코드 변환" + areacode);
        return areacode;
    }

    //검색어
    public void searchWord(String text, boolean[] locationPick, boolean[] facilityPick) {
        //locationPick 지역 boolean값
        //facilityPick  시설 boolean
        fabVisibility(false);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);//소프트 키 자동 내림

        String w = text; //검색어 입력 단어
        ArrayList<String> location_name = new ArrayList<>(); //true 값인 지역담는 배열
        ArrayList<String> facility_name = new ArrayList<>(); //true 값인 시설 테마 담는 배열
        //------------------------초기화 작업------------------------------------------------------------
        boolean facilityFlag = false;
        if (!placedata.isEmpty()) {
            adapter.removeItem();
            placedata.clear();
            filterData.clear();
        }

        if(!localData.isEmpty()){
            localData.clear();
            local_name.clear();
            local_lat.clear();
            local_lat.clear();
        }


        //그룹필터 초기화
        if (!filter_name.isEmpty()) {
            int count = bschipGroup.getChildCount();

            Log.d("몇개?", chipGroup.getChildCount() + "게");
            filter_name.clear();

            for (int i = count - 1; i > -1; i--) {
                Log.d("몇번 돕니까?", i + "번");
                Chip chip = (Chip) bschipGroup.getChildAt(i);
                bschipGroup.removeView(chip);
            }
        }
        //-------------------------------------------초기화 작업 --------------------------------------
        //-----------------------------------------칩 그룹 추가 작업--------------------------------------
        //지역 필터 칩 그룹추가
        for (int i = 0; i < locationPick.length; i++) {
            if (locationPick[i]) {
                location_name.add(changeLocationName(checkedLocName[i]));
                filter_name.add(checkedLocName[i]);
            }
        }
        //시설 필터 칩 그룹 추가
        for (int i = 0; i < facilityPick.length; i++) {
            if (facilityPick[i]) {
                facilityFlag =true;
                filter_name.add(checkingName[i]);
            }
        }



        String titleName = "", addressName = "", dtosCode = "";

        /*
        if (w.isEmpty() && location_name.size() == 0 && !facilityFlag) {
            //검색어 빈칸 + 필터 그룹이 있는 경우
            for (int i = 0; i < dtos.size(); i++)

                filterData.add(new Map_placedata(dtos.get(i).getRecommendTitle(), dtos.get(i).getRecommendAddress(), dtos.get(i).getRecommendLat(), dtos.get(i).getRecommendLng(), dtos.get(i).getRecommendStar(), dtos.get(i).getRecommendAreaCode(), dtos.get(i).getRecommendId(), dtos.get(i).getRecommendLike(), dtos.get(i).getRecommendImage()));
        } else {*/
        for (int i = 0; i < dtos.size(); i++) {

            titleName = dtos.get(i).getRecommendTitle();
            addressName = dtos.get(i).getRecommendAddress();
            dtosCode = dtos.get(i).getRecommendAreaCode();

            boolean addFlag = false, localFlag= false;

            //검색어 유무
            if(!w.isEmpty()){
                //지역 검색
                if(addressName.contains(w)){
                    //검색어가 지역명인 경우
                    Log.d("addressTest", w);
                    local_name.add(dtos.get(i).getRecommendTitle());
                    local_lat.add(dtos.get(i).getRecommendLat());
                    local_lng.add(dtos.get(i).getRecommendLng());
                    localFlag = true;
                }
                //키워드로 검색
                if(!localFlag && titleName.contains(w)){
                    localFlag = true;
                }
            }

            //지역 필터 검사
            for(int k = 0; k < location_name.size(); k++){
                if(location_name.get(k).equals(dtosCode)){
                    addFlag = true;
                }
            }
            //시설 장소 필터 검사
            if(facilityFlag){
                for(int k = 0; k <facilityPick.length; k++){
                    if(facilityPick[k] == dtos.get(i).getRecommendFilter().get(k)) addFlag = true;
                }
            }

            if (localFlag) {
                //검색어 o, 필터 o
                localData.add(new Map_placedata(dtos.get(i).getRecommendTitle(), dtos.get(i).getRecommendAddress(), dtos.get(i).getRecommendLat(), dtos.get(i).getRecommendLng(), dtos.get(i).getRecommendStar(), dtos.get(i).getRecommendAreaCode(), dtos.get(i).getRecommendId(), dtos.get(i).getRecommendLike(), dtos.get(i).getRecommendImage()));
            }else if(addFlag){
                //검색어x 필터 o
                filterData.add(new Map_placedata(dtos.get(i).getRecommendTitle(), dtos.get(i).getRecommendAddress(), dtos.get(i).getRecommendLat(), dtos.get(i).getRecommendLng(), dtos.get(i).getRecommendStar(), dtos.get(i).getRecommendAreaCode(), dtos.get(i).getRecommendId(), dtos.get(i).getRecommendLike(), dtos.get(i).getRecommendImage()));
            }
        }

        if(!localData.isEmpty()){
            for(int i = 0; i <localData.size(); i++){
                placedata.add(new Map_placedata(localData.get(i).getName(), localData.get(i).getAdd(), localData.get(i).getLat(), localData.get(i).getLan(), localData.get(i).getRate(), localData.get(i).getRecPlaceName(), localData.get(i).getRecPlaceId(), localData.get(i).getNum(), localData.get(i).getImage()));
            }
        }
        if(!filterData.isEmpty()){
            for(int i = 0; i <filterData.size(); i++){
                placedata.add(new Map_placedata(filterData.get(i).getName(), filterData.get(i).getAdd(), filterData.get(i).getLat(), filterData.get(i).getLan(), filterData.get(i).getRate(), filterData.get(i).getRecPlaceName(), filterData.get(i).getRecPlaceId(), filterData.get(i).getNum(), filterData.get(i).getImage()));
            }
        }

        if (filterData.isEmpty()&&localData.isEmpty()) {
            Toast.makeText(this.getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show(); //todo: 스낵바로 변경
        } else {
            Log.d("filterTest", "placeData 사이즈" + placedata.size());
            //검색창 애니메이션
            Animation animation = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.slide_up_animation);
            sear.startAnimation(animation);

            fabVisibility(false); // 메뉴버튼 제어
            totBsState(true); //바텀시트 제어
            list_behav.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

            //지도
            mapFlag = 1; // 검색 버튼 눌렸을때 false : 초반 위치 설정 true : 검색 해당 리스트
            sView.getMapAsync(this); //지도 동기화
            //리스트트
            setListData(); //리트 해당 마커표시
            clearChip();

            if (filter_name.isEmpty()) {
                bschipGroup.setVisibility(View.GONE);
                bschipLayout.setVisibility(View.GONE);
            } else {
                bschipGroup.setVisibility(View.VISIBLE);
                bschipLayout.setVisibility(View.VISIBLE);
                for (int i = 0; i < filter_name.size(); i++) {
                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.map_chip_layout, bschipGroup, false);
                    chip.setText(filter_name.get(i));
                    bschipGroup.addView(chip);
                }
            }
        }

    }


    //목록 바텀시트트
    private void initializeListBottomSheet() {
        list_behav = BottomSheetBehavior.from(list_bs);

        list_behav.setDraggable(false);
        bschipGroup = coordinatorLayout.findViewById(R.id.search_chip_group);
        bschipLayout = coordinatorLayout.findViewById(R.id.search_chip_Layout);
        list_behav.setState(BottomSheetBehavior.STATE_HIDDEN);

        ImageButton openBtn = coordinatorLayout.findViewById(R.id.openBtn);
        ImageButton closeBtn = coordinatorLayout.findViewById(R.id.closeBtn);

        //리사이클러뷰
        mapRecyclerView = coordinatorLayout.findViewById(R.id.mapRecyclerView);
        LinearLayoutManager linearLayoutManager_2 = new LinearLayoutManager(getActivity());
        mapRecyclerView.setLayoutManager(linearLayoutManager_2);

        //바텀시트 높이 측정 //지도 마진 주기 위함
        list_bs.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int getH = getView().getHeight();

                Log.d("height test2", "높이" + getH);
                maxHeight = getH;
                list_bs.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (list_behav.getState()) {
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        list_behav.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        list_behav.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                        break;
                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list_behav.setState(BottomSheetBehavior.STATE_HIDDEN);
                closeBtn.setVisibility(View.INVISIBLE);
                fabVisibility(true);
                clearAll();
                mapFlag = 2;
                sView.getMapAsync(Map.this::onMapReady);
                sear.clearAnimation();
            }
        });

        list_behav.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                //bottomsheet 상태 변경시에 호출
                if (newState == list_behav.STATE_COLLAPSED) {
                    onSlide(list_bs, 0.07f);
                } else if (newState == list_behav.STATE_HALF_EXPANDED) {
                    onSlide(list_bs, 0.5f);
                    closeBtn.setVisibility(View.VISIBLE);
                } else if (newState == list_behav.STATE_HIDDEN) {
                    onSlide(list_bs, -0.0f);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //bottomsheet 스크롤할때
                switch (list_behav.getState()) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        setMapPaddingBottom(slideOffset);
                        openBtn.setRotation(360);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        setMapPaddingBottom(slideOffset);
                        openBtn.setRotation(180);
                        break;

                }
            }
        });

    }


    //지도 높이값
    private void setMapPaddingBottom(Float offset) {
        //From 0.0 min - 1.0 max
        CoordinatorLayout.LayoutParams parmas = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        float maxPaddingBottom = maxHeight;
        if (offset != 0.5f) parmas.setMargins(0, 0, 0, Math.round((offset) * maxPaddingBottom));
        else if (offset == 0.5f)
            parmas.setMargins(0, 0, 0, Math.round((offset) * maxPaddingBottom));
        fl.setLayoutParams(parmas);
    }


    // 목록바텀시트 데이터 반영
    public void setListData() {
        adapter = new Map_RecyclerAdapter(context, placedata, localData.size());
        mapRecyclerView.setAdapter(adapter);
        // sView.getMapAsync(Map.this::onMapReady);

        adapter.setOnItemClickListener(new Map_RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                detailMarker = new Marker();
                detailMarker.setPosition(new LatLng(placedata.get(position).getLat(), placedata.get(position).getLan()));
                detailMarker.setCaptionText(placedata.get(position).getName());
                mapFlag = 3;
                sView.getMapAsync(Map.this::onMapReady);
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onMapReady(NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setMapType(NaverMap.MapType.Basic); //배경지도
        naverMap.setLayerGroupEnabled(naverMap.LAYER_GROUP_BUILDING, false);//건물표시
        final String[] markerTxt = new String[1];

        //naverMap.setLocationSource(mLocationSource);
        //ActivityCompat.requestPermissions(getActivity(),REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE);

        //mapFlag 0 : 현 위치
        //mapFlag 1 : 검색어 반영
        //mapFlag 2 : 지도 초기화
        //mapFlag 3 : 특정 한 개마크, 카메라 포지션 변경
        if (mapFlag == 0) {
            if(locMarker != null){
                locMarker.setMap(null);
            }

            locMarker.setPosition(new LatLng(latitude,longitude));
            locMarker.setIcon(OverlayImage.fromResource(R.drawable.locationicon));
            locMarker.setHeight(MARKER_SIZE + 15);
            locMarker.setWidth(MARKER_SIZE + 15);
            locMarker.setMap(naverMap);

            CameraUpdate cameraUpdate = CameraUpdate.scrollAndZoomTo(locMarker.getPosition(), CAMERA_ZOOM_LEVEL);
            naverMap.moveCamera(cameraUpdate);
        } else if (mapFlag == 1) {
            LatLng northEast; //카메라 bounds 좌표( 남서 ) 최하단 위도, 가장 왼쪽 끝 경도
            LatLng southWest; //카메라 bounds 좌표( 북동 ) 최상단 위도, 가장 오른쪽 끝 위도
            double minLat, minLng, maxLat, maxLng;

            if(!local_name.isEmpty()){
                minLat = local_lat.get(0);
                minLng = local_lng.get(0);

                maxLat = local_lat.get(0);
                maxLng = local_lng.get(0);
            }else{
                minLat = placedata.get(0).getLat(); //최하단 경도
                minLng = placedata.get(0).getLan(); //가장 왼쪽 끝 위도

                maxLat = placedata.get(0).getLat(); //최상단 경도
                maxLng = placedata.get(0).getLan(); //가장 오른쪽 끝 위도
            }


            //마커 저장되어 있는 경우 초기화
            if (markerList != null || markerList.size() != 0) {
                markerList.clear();
            }
            for (int i = 0; i < placedata.size(); i++) {
                Marker marker = new Marker();
                marker.setPosition(new LatLng(placedata.get(i).getLat(), placedata.get(i).getLan()));
                marker.setCaptionText(placedata.get(i).getName());
                marker.setWidth(MARKER_SIZE);
                marker.setHeight(MARKER_SIZE + 40);
                markerList.add(marker);
                if(local_name.isEmpty()) {
                    //경도 색출
                    if (placedata.get(i).getLat() < minLat) {
                        minLat = placedata.get(i).getLat();
                    }
                    if (placedata.get(i).getLat() > maxLat) {
                        maxLat = placedata.get(i).getLat();
                    }
                    //위도 색출
                    if (placedata.get(i).getLan() < minLng) {
                        minLng = placedata.get(i).getLan();
                    }
                    if (placedata.get(i).getLan() > maxLng) {
                        maxLng = placedata.get(i).getLan();
                    }
                }
                marker.setMap(naverMap);
                marker.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        Log.d("map test", "마커 클릭됨");
                        markerTxt[0] = marker.getCaptionText();

                        markerClick(markerTxt[0], marker.getPosition());
                        return false;
                    }
                });

            }
            if(!local_name.isEmpty()){
                for(int i = 0; i < local_name.size(); i++){
                    if (local_lat.get(i) < minLat) {
                        minLat = local_lat.get(i);
                    }
                    if (local_lat.get(i) > maxLat) {
                        maxLat = local_lat.get(i);
                    }
                    //위도 색출
                    if (local_lng.get(i) < minLng) {
                        minLng = local_lng.get(i);
                    }
                    if (local_lng.get(i) > maxLng) {
                        maxLng = local_lng.get(i);
                    }
                }
            }
            northEast = new LatLng(maxLat+.1f, minLng+.1f);
            southWest = new LatLng(minLat-.1f, maxLng-.1f);

            CameraUpdate cameraUpdate = CameraUpdate.fitBounds(new LatLngBounds(southWest, northEast), 10, 20, 10, 20);
            naverMap.moveCamera(cameraUpdate);
        } else if (mapFlag == 2) {
            Log.d("마크 테스트", ""+markerList.size());
            //초기화
            for (int i = 0; i < markerList.size(); i++) {
                markerList.get(i).setMap(null);
            }
            markerList.clear();
            detMarker.setMap(null);
            if(detailMarker != null){
                detailMarker.setMap(null);
            }

        } else if (mapFlag == 3) {
            for (int i = 0; i < markerList.size(); i++) {
                markerList.get(i).setMap(null);
            }
            detailMarker.setWidth(MARKER_SIZE);
            detailMarker.setHeight(MARKER_SIZE + 40);
            detailMarker.setMap(naverMap);
            setBSDetail(detailMarker);
            CameraUpdate cameraUpdate2 = CameraUpdate.scrollAndZoomTo(detailMarker.getPosition(), CAMERA_ZOOM_LEVEL); //네이버 줌 레벨 제일 먼 1 -> 14 제일가까운
            naverMap.moveCamera(cameraUpdate2);

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void markerClick(String text, LatLng pos){
        for (int i = 0; i < markerList.size(); i++) {
            markerList.get(i).setMap(null);
        }

        detMarker.setPosition(pos);
        detMarker.setCaptionText(text);

        setBSDetail(detMarker);
        detMarker.setMap(naverMap);

        CameraUpdate cameraUpdate2 = CameraUpdate.scrollAndZoomTo(pos, CAMERA_ZOOM_LEVEL); //네이버 줌 레벨 제일 먼 1 -> 14 제일가까운
        naverMap.moveCamera(cameraUpdate2);
    }

    //디테일 카드뷰 정보 전달 cardview
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void setBSDetail(Marker marker) {
        list_bs.setVisibility(View.INVISIBLE);
        totCard.setVisibility(View.VISIBLE);
        list_behav.setState(BottomSheetBehavior.STATE_HIDDEN);
        String name = marker.getCaptionText(); //마커 타이틀

        cardview.setVisibility(View.VISIBLE);
        TextView tvDP = coordinatorLayout.findViewById(R.id.detailTitle); //타이틀
        TextView tvDA = coordinatorLayout.findViewById(R.id.detailAdd); //주소
        ImageView ivImg = coordinatorLayout.findViewById(R.id.detailImg); //이미지
        RatingBar rbb = coordinatorLayout.findViewById(R.id.detailStar); //레이팅 바
        TextView tvRate = coordinatorLayout.findViewById(R.id.detailNum);
        TextView tvLike = coordinatorLayout.findViewById(R.id.detailLikeNum);
        GradientDrawable drawable = (GradientDrawable) getContext().getDrawable(R.drawable.community_edge);

        //이미지 넣기
        ivImg.setBackground(drawable);
        ivImg.setClipToOutline(true);

        for (int i = 0; i < placedata.size(); i++) {
            if (placedata.get(i).getName().equals(name)) {
                tvDP.setText(placedata.get(i).getName());
                tvDA.setText(placedata.get(i).getAdd());
                rbb.setRating((float) placedata.get(i).getRate());
                tvRate.setText(String.valueOf((float) placedata.get(i).getRate()));
                tvLike.setText(String.valueOf(placedata.get(i).getLike()));
                Glide.with(getContext())
                        .load(placedata.get(i).getImage())
                        .into(ivImg);

                recPlaceName = placedata.get(i).getRecPlaceName();
                recPlaceId = placedata.get(i).getRecPlaceId();
            }
        }
    }

    public void totBsState(boolean flag) {
        if (flag) {
            //목록 BS == open
            list_bs.setVisibility(View.VISIBLE);
            list_behav.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        } else {
            Log.d("1. test", "totBsState false");
            list_bs.setVisibility(View.INVISIBLE);
        }
    }

    private void fabVisibility(boolean fabFlag) {

        if (fabFlag) {
            filterFab.setVisibility(View.VISIBLE);
            mapFab.setVisibility(View.VISIBLE);
            mainFab.setVisibility(View.VISIBLE);
            locationFab.setVisibility(View.VISIBLE);
        } else {
            filterFab.setVisibility(View.GONE);
            mapFab.setVisibility(View.GONE);
            mainFab.setVisibility(View.GONE);
            locationFab.setVisibility(View.GONE);
        }

    }

    //좌측 하단 fab 메뉴 애니메이션
    private void fabListener(Boolean btnFlag) {
        //btnflag default : false : 닫혀있음 / true : 열려있음
        if (btnFlag) {
            //닫기
            //ObjectAnimator.ofFloat(mainFab, View.ROTATION, 45f, 0f).start();
            ObjectAnimator.ofFloat(locationFab, "translationY", 0f).start();
            ObjectAnimator.ofFloat(filterFab, "translationY", 0f).start();
            ObjectAnimator.ofFloat(mapFab, "translationY", 0f).start();
            mainFab.bringToFront();
        } else {
            //열기
            ObjectAnimator.ofFloat(locationFab, "translationY", -720f).start();
            ObjectAnimator.ofFloat(filterFab, "translationY", -480f).start();
            ObjectAnimator.ofFloat(mapFab, "translationY", -240f).start();
            //ObjectAnimator.ofFloat(mainFab, View.ROTATION, 0f, 45f).start();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //sView.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        sView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        sView.onStop();
    }

    @Override
    public void onSaveInstanceState(@Nullable Bundle outState) {
        super.onSaveInstanceState(outState);
        sView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sView.onDestroy();
    }

    //뒤로가기
    @Override
    public void onResume() {
        super.onResume();
        sView.onResume();
        if(enterFlag) {
            mapFlag = 0;
            sView.getMapAsync(Map.this::onMapReady);
            placedata.clear();
        }

        FragmentActivity activity = getActivity();
        if (activity != null) {
            ((MainActivity) activity).setBackBtn(0, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    //naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                    break;
                }
            }
            if (check_result) {
                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
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

    void checkRunTimePermission() {

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

}

