package com.example.GoAutoCamping;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.security.cert.PolicyNode;

public class Recommend_detail_reviewdialog extends DialogFragment {

    MaterialButton close, add;
    RatingBar star;
    TextInputEditText review;

    public interface SendData{
        void sendData(float rating, String review);
        void clearAll();
    }

    public SendData SendData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recommend_detail_reviewdialog, container, false);



        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("리뷰");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.recommend_detail_reviewdialog, null);

        star = view.findViewById(R.id.starRate);
        review = view.findViewById(R.id.review_text);
        close = view.findViewById(R.id.cancel_review);
        add = view.findViewById(R.id.apply_review);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendData.sendData(star.getRating(), review.getText().toString());
                resetFilter();
                dismiss();
            }
        });

        builder.setView(view);

        return builder.create();

        //getDialog().setTitle("리뷰");
    }

    public void resetFilter(){
        star.setRating(1);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            SendData = (SendData) getTargetFragment();
        }catch (ClassCastException e){
            Log.e("fuck", "onAttach: ClassCastException : " + e.getMessage());;
        }
    }
}
