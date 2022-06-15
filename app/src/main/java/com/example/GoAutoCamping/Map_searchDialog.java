package com.example.GoAutoCamping;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.ZoomControlView;

public class Map_searchDialog extends DialogFragment implements OnMapReadyCallback {
    final int CAMERA_ZOOM_LEVEL = 13;
    final int MARKER_SIZE = 100;

    final double NORTH_LATITUDE = 38.58742;
    final double SOUTH_LATITUDE = 33.112585;
    final double WEST_LONGITUDE = 124.608107;
    final double EAST_LONGITUDE = 131.872743;


    Button addBtn, searchBtn, locationBtn;
    TextView addTV;
    String Add, title;
    double Lat, Lng;
    MapView mapView = null;
    private NaverMap naverMap;
    Boolean flag = false;
    Marker setMarker;
    View view;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private Home_gpsTracker gpsTracker;
    double latitude, longitude; //위도, 경도


    public Map_searchDialog() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.map_searchdialog, container, false);

        Log.d("locyation main test", "if flag 입력");
        setCancelable(false);//바깥터치 막기


        //mapFragment.getMapAsync(this);
        NaverMapOptions options = new NaverMapOptions()
                .locationButtonEnabled(false)
                .tiltGesturesEnabled(false);

        mapView = view.findViewById(R.id.dialogMap);
        mapView.onCreate(savedInstanceState);
        searchBtn = view.findViewById(R.id.searchBtn); //검색 확인
        setScreen();
        ImageView close = view.findViewById(R.id.closeBtn);
        addTV = view.findViewById(R.id.address);
        addBtn = view.findViewById(R.id.addBtn); //재선택
        locationBtn = view.findViewById(R.id.locationBtn); //위치 선택

        //닫기
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        //위치선택
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), Map_placesChoice.class);
                startActivityForResult(intent, 24);
                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.none);
                flag = true;
            }
        });

        //차박검색 (->map_searchProgress 로 전달)
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    Map_searchProgress msp = new Map_searchProgress();

                    if(title==null){
                        title = "위치없음";
                    }
                    Bundle address = new Bundle();
                    address.putDouble("Latitude", Lat);
                    address.putDouble("Longitude", Lng);
                    address.putString("title", title);

                    msp.setTargetFragment(Map_searchDialog.this, 1);
                    msp.setArguments(address);
                    msp.show(requireActivity().getSupportFragmentManager(), "tag");

                    getDialog().dismiss();
                } else {
                    Toast.makeText(getActivity(), " 검색할 위치를 선택해주세요 ", Toast.LENGTH_SHORT).show(); //todo: 스낵바로 변경
                }

            }
        });

        //현위치 검색
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                    if (latitude > NORTH_LATITUDE || latitude < SOUTH_LATITUDE || longitude < WEST_LONGITUDE || longitude > EAST_LONGITUDE) {
                        //경도값 초과, 위도값 초과
                        Toast.makeText(getActivity(), " 현재 위치를 불러올 수 없습니다 ", Toast.LENGTH_SHORT).show(); //todo: 스낵바로 변경

                    } else {

                        Lat = latitude;
                        Lng = longitude;
                        flag = true;
                        mapView.getMapAsync(Map_searchDialog.this::onMapReady);

                        changeBtnColor();
                    }

                }
            }

        });


        return view;
    }

    private void changeBtnColor() {
        searchBtn.setBackgroundResource(R.drawable.map_place_choice_btn);

    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        UiSettings uiSettings = naverMap.getUiSettings();
        //uiSettings.setAllGesturesEnabled(false); //모든 제스처 비활성화
        uiSettings.setZoomGesturesEnabled(false); //줌 제스처 비활성화
        uiSettings.setLocationButtonEnabled(false);


        if (setMarker != null) {
            setMarker.setMap(null);
            setMarker = null;
        }
        Marker marker = new Marker();
        setMarker = new Marker();
        marker.setPosition(new LatLng(Lat, Lng));
        setMarker.setPosition(new LatLng(Lat, Lng));
        marker.setWidth(MARKER_SIZE);
        marker.setHeight(MARKER_SIZE + 40);

        marker.setMap(naverMap);
        CameraUpdate cameraUpdate2 = CameraUpdate.scrollAndZoomTo(marker.getPosition(), CAMERA_ZOOM_LEVEL); //네이버 줌 레벨 제일 먼 1 -> 14 제일가까운
        naverMap.moveCamera(cameraUpdate2);

    }

    void setScreen() {
        Log.d("location", "화면 리뉴얼 ");
        if (addTV != null) {
            addTV.setText(Add);
        }

        mapView.getMapAsync(this);
        if(Lat == 37.58482502367129 && Lng ==126.92520885572567 ){

        }else{
            changeBtnColor();
        }

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Lat = getArguments().getDouble("currentLatitude");
        Lng = getArguments().getDouble("currentLongitude");


        Log.d("값 받아옴", "위경도");
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    //위치검색에서 받은 데이터 값 intent(<-map_placesChoice에서 받음)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 24) {
            if (resultCode == Activity.RESULT_OK) {
                Add = data.getStringExtra("Add2");
                Lat = data.getDoubleExtra("Lat2", 0.0f);
                Lng = data.getDoubleExtra("Lng2", 0.0f);
                title = data.getStringExtra("title");

                Log.d("address test", "받을 때 " + title);
                setScreen();
            }
        } else if (requestCode == GPS_ENABLE_REQUEST_CODE) {
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {
                    Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                    checkRunTimePermission();
                }
            }
        }
    }

    // map에서 bundel 전달 받는 값 삭제
    // dialog 진입시 현재 위치 받아옴
    // 현 위치 x, 주소 x -> 차박 검색 disable
    //
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
