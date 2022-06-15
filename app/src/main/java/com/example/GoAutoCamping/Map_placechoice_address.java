package com.example.GoAutoCamping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.util.List;


public class Map_placechoice_address extends BottomSheetDialogFragment {

    private Activity activity;
    TextView bsTv;
    String addressName = null;
    //Map_placeChoice로 값 전달하기 위해 인터페이스설정
    public interface AddressListener{
        void sendAddress(String inAddress, double inLat, double inLng);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_placechoice_address, container,false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText bsEdit = view.findViewById(R.id.bsEdit);
        ImageButton bsBtn = view.findViewById(R.id.button);
        bsTv = view.findViewById(R.id.bsTv);

        bsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addressName = bsEdit.getText().toString();
                getLatLng(addressName);
            }
        });


        return view;
    }

    //입력한 주소값이 존재하는지 검사
    public void getLatLng(String address) {
        Log.d("locationTest", address);
        Geocoder geo = new Geocoder(getActivity());
        List<Address> add = null;
        Location location = null;

        try {
            add = geo.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("location test", "주소 변환 오류");
            bsTv.setText("정확한 주소로 기입했는지 확인해주세요");
            bsTv.setVisibility(View.VISIBLE);
        }

        if (add != null) {
            if (add.size() == 0) {
                Log.d("location test", "주소 값 알 수 없음");
                bsTv.setText("정확한 주소로 기입했는지 확인해주세요");
                bsTv.setVisibility(View.VISIBLE);
            }
            else {
                double lat = add.get(0).getLatitude();
                double lng = add.get(0).getLongitude();

                for (int i = 0; i < add.size(); i++) {
                    Address latlng = add.get(i);
                    lat = latlng.getLatitude(); //위도가져오기
                    lng = latlng.getLongitude(); //경도 가져오기
                }


                //주소값 정확하면 바로 전달
                ((AddressListener)activity).sendAddress(addressName, lat, lng);
                dismiss();

            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof Activity){
            this.activity = (Activity)context;
        }
    }

}
