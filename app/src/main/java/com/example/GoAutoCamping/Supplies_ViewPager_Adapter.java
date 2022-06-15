package com.example.GoAutoCamping;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class Supplies_ViewPager_Adapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> pages;

    //뷰페이저 어뎁터. 프래그먼트로 이루어진 ArrayList에 프래그먼트 추가하여 뷰페이저 생성
    public Supplies_ViewPager_Adapter(@NonNull FragmentManager fm) {
        super(fm);
        pages = new ArrayList<Fragment>();
        pages.add(new Supplies_Light());
        pages.add(new Supplies_Living());
        pages.add(new Supplies_Cooking());
        pages.add(new Supplies_Other());

    }

    @NonNull
    @Override   //페이지 설정
    public Fragment getItem(int position) { return pages.get(position); }

    @Override   //페이지 갯수 리턴
    public int getCount() { return pages.size(); }
}
