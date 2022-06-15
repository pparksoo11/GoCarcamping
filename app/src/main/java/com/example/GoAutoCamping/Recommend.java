package com.example.GoAutoCamping;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Recommend extends Fragment{

    Button btnGyeonggi, btnSeoul, btnGSND, btnGSBD, btnGWD, btnCCND, btnCCBD, btnJLND, btnJLBD, btnJeju;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recommend, container, false);
        btnGyeonggi = view.findViewById(R.id.btnGGD);
        btnSeoul = view.findViewById(R.id.btnSeoul);
        btnGSND = view.findViewById(R.id.btnGSND);
        btnGSBD = view.findViewById(R.id.btnGSBD);
        btnGWD = view.findViewById(R.id.btnGWD);
        btnCCND = view.findViewById(R.id.btnCCND);
        btnCCBD = view.findViewById(R.id.btnCCBD);
        btnJLND = view.findViewById(R.id.btnJLND);
        btnJLBD = view.findViewById(R.id.btnJLBD);
        btnJeju = view.findViewById(R.id.btnJeju);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        //transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_right, R.anim.enter_from_right, R.anim.exit_from_right);
        transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_from_right);

        btnGyeonggi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_ggd()).addToBackStack(null).commit();
            }
        });

        btnSeoul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_seoul()).addToBackStack(null).commit();
            }
        });

        btnJeju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_jeju()).addToBackStack(null).commit();
            }
        });

        btnGWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_gwd()).addToBackStack(null).commit();
            }
        });

        btnGSBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_gsbd()).addToBackStack(null).commit();
            }
        });

        btnGSND.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_gsnd()).addToBackStack(null).commit();
            }
        });

        btnCCND.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_ccnd()).addToBackStack(null).commit();
            }
        });

        btnCCBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_ccbd()).addToBackStack(null).commit();
            }
        });

        btnJLND.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_jlnd()).addToBackStack(null).commit();
            }
        });

        btnJLBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transaction.replace(R.id.main_frame, new Recommend_jlbd()).addToBackStack(null).commit();
            }
        });

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if(activity!=null){
            ((MainActivity)activity).setBackBtn(0,false);
        }
    }
}