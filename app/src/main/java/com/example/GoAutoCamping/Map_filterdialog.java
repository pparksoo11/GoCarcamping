package com.example.GoAutoCamping;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.naver.maps.map.NaverMap;

public class Map_filterdialog extends DialogFragment {

    private Button resetbtn, applybtn;
    private ImageView closeBtn;

    CheckBox mountain, ocean, valley, camping, park, parking, river;
    Chip toilet, shower, store, cooking, seoul, ggd, gwd, gsbd, gsnd, zrbd, zrnd, ccnd,ccbd,zzd;


    boolean[] checking = {false, false, false, false, false, false, false, false, false, false, false};
    String[] checkingName = {"산", "강", "바다", "계곡", "캠핑장", "공원", "주차장", "화장실", "샤워실", "매점", "취사"};
    boolean[] checkingLoc = {false, false, false, false, false, false, false, false, false,false};
    String[] checkingLocName = {"서울", "경기도", "강원도", "충청북도","충청남도", "경상북도", "경상남도", "전라북도", "전라남도", "제주도"};


    public interface InputSelected {
        void sendBoolenArray(boolean[] ch, String[] chName, boolean[] chLoc, String[] chLocName);

        void clearAll();

    }

    public InputSelected inputSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_filterdialog, container, false);

        closeBtn = view.findViewById(R.id.closeFilter);
        resetbtn = view.findViewById(R.id.reset);
        applybtn = view.findViewById(R.id.apply);

        mountain = view.findViewById(R.id.mountain);
        river = view.findViewById(R.id.river);
        ocean = view.findViewById(R.id.ocean);
        valley = view.findViewById(R.id.valley);
        camping = view.findViewById(R.id.campingspot);
        park = view.findViewById(R.id.park);
        parking = view.findViewById(R.id.parkinglot);
        toilet = view.findViewById(R.id.toilet);
        shower = view.findViewById(R.id.shower);
        store = view.findViewById(R.id.store);
        cooking = view.findViewById(R.id.cookingspot);

        seoul = view.findViewById(R.id.seoul);
        ggd = view.findViewById(R.id.ggd);
        gwd = view.findViewById(R.id.gwd);
        gsnd = view.findViewById(R.id.gsnd);
        gsbd = view.findViewById(R.id.gsbd);
        zrnd = view.findViewById(R.id.zrnd);
        zrbd = view.findViewById(R.id.zrbd);
        ccnd = view.findViewById(R.id.ccnd);
        ccbd = view.findViewById(R.id.ccbd);
        zzd = view.findViewById(R.id.zzd);


        //창 닫기
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter();
                getDialog().dismiss();
            }
        });

        resetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter();
                inputSelected.clearAll();
            }
        });

        applybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checking[0] = mountain.isChecked(); //산
                checking[1] = river.isChecked(); //강
                checking[2] = ocean.isChecked(); //바다
                checking[3] = valley.isChecked(); //계곡
                checking[4] = camping.isChecked(); //캠핑장
                checking[5] = park.isChecked(); //공원
                checking[6] = parking.isChecked(); //주차장
                checking[7] = toilet.isChecked(); //화장실
                checking[8] = shower.isChecked(); //샤워실
                checking[9] = store.isChecked(); //매점
                checking[10] = cooking.isChecked(); //취사

                checkingLoc[0] = seoul.isChecked(); //서울
                checkingLoc[1] = ggd.isChecked(); //경기도
                checkingLoc[2] = gwd.isChecked(); //강원도
                checkingLoc[3] = ccbd.isChecked(); //충청북도
                checkingLoc[4] = ccnd.isChecked(); //충청남도
                checkingLoc[5] = gsbd.isChecked();  //경상북도
                checkingLoc[6] = gsnd.isChecked(); //경상남도
                checkingLoc[7] = zrbd.isChecked(); //전라북도
                checkingLoc[8] = zrnd.isChecked(); //전라남도
                checkingLoc[9] = zzd.isChecked();

                inputSelected.sendBoolenArray(checking, checkingName, checkingLoc, checkingLocName);
                resetFilter();
                getDialog().dismiss();

            }
        });

        return view;
    }

    //다이얼로그 모든 체크 초기화
    public void resetFilter() {
        mountain.setChecked(false);
        ocean.setChecked(false);
        valley.setChecked(false);
        camping.setChecked(false);
        park.setChecked(false);
        toilet.setChecked(false);
        shower.setChecked(false);
        store.setChecked(false);
        cooking.setChecked(false);
        parking.setChecked(false);

        seoul.setChecked(false);
        ggd.setChecked(false);
        gwd.setChecked(false);
        ccbd.setChecked(false);
        ccnd.setChecked(false);
        gsbd.setChecked(false);
        gsnd.setChecked(false);
        zrbd.setChecked(false);
        zrnd.setChecked(false);
        zzd.setChecked(false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            inputSelected = (InputSelected) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e("fuck", "onAttach: ClassCastException : " + e.getMessage());
            ;
        }
    }
}
