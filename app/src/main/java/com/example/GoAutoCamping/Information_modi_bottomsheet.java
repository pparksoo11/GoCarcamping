package com.example.GoAutoCamping;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Date;

public class Information_modi_bottomsheet extends BottomSheetDialogFragment {

    View view;
    private Activity activity;
    private BottomSheetListener mListener;
    private Button hideBtn, confirmBtn;
    int y = 0, m = 0, d = 0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.information_modi_birth_bottomsheet, container, false);

        DatePicker dp = view.findViewById(R.id.datePicker);

        //오늘 날짜 전으로만 선택가능
        long now = System.currentTimeMillis();
        dp.setMaxDate(now);

        //닫기 버튼
        hideBtn = view.findViewById(R.id.hideBtn);
        hideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //확인 버튼
        confirmBtn = view.findViewById(R.id.confirmBtn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BottomSheetListener)activity).setDate(dp.getYear(),dp.getMonth()+1, dp.getDayOfMonth());
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            // 사용될 activity 에 context 정보 가져오는 부분
            this.activity = (Activity) context;
        }
    }

    public interface BottomSheetListener {
        void setDate(int year, int month, int date);
    }
}
