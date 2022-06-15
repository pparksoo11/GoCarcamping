package com.example.GoAutoCamping;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;


public class Supplies extends Fragment {

    Supplies_ViewPager_Adapter adapter;
    TabLayout tabLayout;
    GridView gridView;
    public static String[] values = {"전기파리채", "2", "3", "4", "5", "6", "7", "8"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.supplies, container, false);

        //탭 레이아웃 어댑터 설정
        ViewPager vp = view.findViewById(R.id.viewpager);
        adapter = new Supplies_ViewPager_Adapter(getChildFragmentManager());
        vp.setAdapter(adapter);

        vp.setOffscreenPageLimit(3);

        //연동
        tabLayout = view.findViewById(R.id.tab);
        tabLayout.setupWithViewPager(vp);
        setupTabIcons();

        return view;
    }

    private void setupTabIcons(){
        //홈
        View viewLight = getLayoutInflater().inflate(R.layout.supplies_tap_custom, null);
        TextView txtLight = viewLight.findViewById(R.id.txt_tab);
        txtLight.setText("조명");

        //지난일정
        View viewLiving = getLayoutInflater().inflate(R.layout.supplies_tap_custom, null);
        TextView txtLiving = viewLiving.findViewById(R.id.txt_tab);
        txtLiving.setText("생활");

        //스톱워치
        View viewCooking = getLayoutInflater().inflate(R.layout.supplies_tap_custom, null);
        TextView txtCooking = viewCooking.findViewById(R.id.txt_tab);
        txtCooking.setText("취사");

        View viewOther = getLayoutInflater().inflate(R.layout.supplies_tap_custom, null);
        TextView txtOther = viewOther.findViewById(R.id.txt_tab);
        txtOther.setText("기타");


        //탭 레이아웃에 커스텀뷰 추가
        tabLayout.getTabAt(0).setCustomView(viewLight);
        tabLayout.getTabAt(1).setCustomView(viewLiving);
        tabLayout.getTabAt(2).setCustomView(viewCooking);
        tabLayout.getTabAt(3).setCustomView(viewOther);

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