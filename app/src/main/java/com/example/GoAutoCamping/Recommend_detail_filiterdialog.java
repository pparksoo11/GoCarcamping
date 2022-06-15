package com.example.GoAutoCamping;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

public class Recommend_detail_filiterdialog extends DialogFragment {

    private Button resetbtn, applybtn;
    private ImageView closeBtn;

    CheckBox mountain, ocean, valley, camping, park, parking, river;
    Chip toilet, shower, store, cooking;
    boolean[] checking = {false, false, false, false, false, false, false, false, false, false, false};
    String[] checkingName = { "mountain", "river", "ocean", "valley", "camping", "park", "parking", "toilet", "shower", "store", "cooking"};

    public interface InputSelected{
        void sendBoolenArray(boolean[] ch, String[] chName);
        void clearAll();
    }

    public InputSelected inputSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recommend_detail_filterdialog, container, false);

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
                checking[0] = mountain.isChecked();
                checking[1] = river.isChecked();
                checking[2] = ocean.isChecked();
                checking[3] = valley.isChecked();
                checking[4] = camping.isChecked();
                checking[5] = park.isChecked();
                checking[6] = parking.isChecked();
                checking[7] = toilet.isChecked();
                checking[8] = shower.isChecked();
                checking[9] = store.isChecked();
                checking[10] = cooking.isChecked();


                inputSelected.sendBoolenArray(checking, checkingName);
                resetFilter();
                getDialog().dismiss();

            }
        });

        return view;
    }

    public void resetFilter(){
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

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            inputSelected = (InputSelected) getTargetFragment();
        }catch (ClassCastException e){
            Log.e("fuck", "onAttach: ClassCastException : " + e.getMessage());;
        }
    }
}
