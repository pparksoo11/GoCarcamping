package com.example.GoAutoCamping;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Community_declear extends DialogFragment {

    MaterialButton btnYes, btnNo;
    RadioGroup radioGroup;

    //게시물 이름
    String postId, rec2, home;

    String email;

    public interface Cleared{
        void clearAll(boolean checking);
    }

    public Cleared cleared;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("신고 사유");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.community_declear, null);
        builder.setView(view);

        btnYes = view.findViewById(R.id.btnYes);
        btnNo = view.findViewById(R.id.btnNo);

        Bundle mArgs = getArguments();
        postId = mArgs.getString("postId");

        if(mArgs.getString("rec2") != null)
            rec2 = mArgs.getString("rec2");
        else if(mArgs.getString("home") != null)
            home = mArgs.getString("home");


        //신고하기
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore Firestore = FirebaseFirestore.getInstance();

                final DocumentReference documentReference = Firestore.collection("communication").document(postId);

                //신고를 누른 사용자인지 검사해주기
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("communityDeclear");
                        //이미 신고를 한 경우
                        if(group.contains(email)){
                            dismiss();
                            Toast.makeText(getContext(), "신고 접수 내역이 존재합니다.", Toast.LENGTH_SHORT).show();
                        }
                        //신고를 새로 작성하는 경우
                        else{
                            documentReference.update("communityDeclear", FieldValue.arrayUnion(email));
                            dismiss();
                            Toast.makeText(getContext(), "신고가 정상적으로 접수되었습니다.", Toast.LENGTH_SHORT).show();
                            Fragment fragment = new Community();

                            Bundle bundle = new Bundle();
                            bundle.putString("update", "update");

                            fragment.setArguments(bundle);

                            ((MainActivity)getActivity()).hideBottomNavi(false);

                            if(home != null){
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Home()).addToBackStack(null).commit();
                            }
                            else if(rec2 != null) {
                                dismiss();
                                cleared.clearAll(true);
                            }
                            else
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Community(), "comm").addToBackStack(null).commit();

                            //신고가 다섯개라면 자동 삭제 처리
                            if(group.size() + 1 == 5) {
                                Firestore.collection("communication").document(postId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });

        //다이어로그 닫기
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        getDialog().getWindow().setLayout(1200, 1380);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //시작 시 사용자 정보 가져오기
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        super.onAttach(context);
        try{
            cleared = (Community_declear.Cleared) getTargetFragment();
        }catch (ClassCastException e){
            Log.e("fuck", "onAttach: ClassCastException : " + e.getMessage());;
        }

        FirebaseFirestore Firestore = FirebaseFirestore.getInstance();
        FirebaseAuth user = FirebaseAuth.getInstance();

        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
        }
    }
}
