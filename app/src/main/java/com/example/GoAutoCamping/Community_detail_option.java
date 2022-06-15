package com.example.GoAutoCamping;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Community_detail_option extends BottomSheetDialogFragment implements View.OnClickListener {
    public static Community_detail_option getInstance() { return new Community_detail_option(); }

    private LinearLayout btnUpdate;
    private LinearLayout btnDelete;
    private LinearLayout btnClose;
    private LinearLayout btnDeclear;
    String address, content, id, image, uploadTime, nickName, profile, postId, rec2, home, word;
    Float star;
    int like, pos;
    ArrayList<String> likeUser;

    private FirebaseAuth user;
    private FirebaseFirestore Firestore;
    private String email, userNickName;
    private FirebaseDatabase firebaseDatabase;

    boolean check = false; //게시물 삭제 시 체크

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.community_detail_option, container,false);

        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnClose = view.findViewById(R.id.btnClose);
        btnDeclear = view.findViewById(R.id.btnDeclear);

        btnUpdate.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnDeclear.setOnClickListener(this);

        //번들 받아오는 부분
        Bundle bundle = getArguments();
        if(bundle.getString("rec2") != null) {
            rec2 = bundle.getString("rec2");
        }
        else if(bundle.getString("home") != null) {
            home = bundle.getString("home");
        }
        else if(bundle.getString("word") != null) {
            word = bundle.getString("word");
        }

        address = bundle.getString("address");
        content = bundle.getString("content");
        id = bundle.getString("id");
        image = bundle.getString("image");
        uploadTime = bundle.getString("uploadTime");
        nickName = bundle.getString("nickName");
        profile = bundle.getString("profile");
        star = bundle.getFloat("star");
        like = bundle.getInt("like");
        likeUser = bundle.getStringArrayList("likeUser");
        postId = bundle.getString("postId");
        pos = bundle.getInt("pos");

        return view;
    }

    public void onClick(View view) {
        final DocumentReference documentReferenceUser = Firestore.collection("users").document(email);

        switch (view.getId()) {
            //TODO 게시물 수정
            case R.id.btnUpdate:
                Intent intent = new Intent(getActivity(),Community_add.class);
                intent.putExtra("update", "update");
                intent.putExtra("address", address);
                intent.putExtra("content", content);
                intent.putExtra("id", id);
                intent.putExtra("image", image);
                intent.putExtra("uploadTime", uploadTime);
                intent.putExtra("nickName", nickName);
                intent.putExtra("profile", profile);
                intent.putExtra("star", star);
                intent.putExtra("like", like);
                intent.putStringArrayListExtra("likeUser", likeUser);
                intent.putExtra("postId", postId);
                intent.putExtra("pos",pos);
                if (rec2 != null)
                    intent.putExtra("rec2", rec2);
                else if (home != null)
                    intent.putExtra("home", home);
                else if (word != null)
                    intent.putExtra("word", word);

                startActivityForResult(intent, 1);
                break;
            //게시물 삭제
            case R.id.btnDelete:
                MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                dialog.title(null, "게시물 삭제");
                dialog.message(null, "정말로 게시물을 삭제하시겠습니까?", null);
                dialog.positiveButton(null, "네", materialDialog -> {
                    //게시글 삭제
                    Firestore.collection("communication").document(postId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            documentReferenceUser.update("userPosts", FieldValue.arrayRemove(postId));

                            Toast.makeText(getContext(),"게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            dismiss();
                            Fragment fragment = new Community();

                            Bundle bundle = new Bundle();
                            bundle.putString("update", "update");

                            fragment.setArguments(bundle);

                            check = true;

                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Community(), "comm").addToBackStack(null).commit();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("에러", "Error : " + e);
                        }
                    });

                    firebaseDatabase.getReference().child("Community").child(postId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override public void onSuccess(Void aVoid) {
                            Log.d("릴탐 게시물 삭제", "성공");
                        } }).addOnFailureListener(new OnFailureListener() {
                        @Override public void onFailure(@NonNull Exception e) {
                            System.out.println("error: "+e.getMessage());
                        }
                    });



                    return null;
                });
                dialog.negativeButton(null, "아니요", materialDialog -> {
                    dialog.dismiss();
                    return null;
                });
                dialog.show();
                break;
            case R.id.btnClose:
                dismiss();
                break;

            //게시물 신고
            case R.id.btnDeclear:
                Bundle bundle = new Bundle();
                bundle.putString("postId", postId);
                if (rec2 != null)
                    bundle.putString("rec2", rec2);
                else if (home != null)
                    bundle.putString("home", home);

                Community_declear community_declear = new Community_declear();
                community_declear.setArguments(bundle);
                community_declear.setTargetFragment(getParentFragment(), 1);
                community_declear.show(getActivity().getSupportFragmentManager(), "declear");
                dismiss();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("확인용", "다이얼로그 닫히고 호출되나");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    //게시물 수정 후 페이지 재 로드
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                if (data.getStringExtra("rec2") != null) {
                    ((Community_detail)getParentFragment()).loadRecommendCommunity();
                }

                else if (data.getStringExtra("home") != null) {
                    ((Community_detail)getParentFragment()).loadHome();
                }

                else if (data.getStringExtra("address") != null) {
                    ((Community_detail)getParentFragment()).loadSearch();
                }
                else {
                    ((Community_detail) getParentFragment()).load();
                }
                dismiss();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        FragmentActivity activity = getActivity();
        //게시물 삭제면 바텀네비 보여주고 아니면 숨기기
        if(check)
            ((MainActivity)getActivity()).hideBottomNavi(false);
        else
            ((MainActivity)activity).hideBottomNavi(true);

        ((MainActivity)getActivity()).onBackPressed();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        user = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        FirebaseUser currentUser = user.getCurrentUser();
        if(currentUser != null){
            email = currentUser.getEmail();
            Log.d("email", email);

            //사용자 정보가져오기
            DocumentReference docRef = Firestore.collection("users").document(email);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //사용자 닉네임 가져오기
                    UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);
                    userNickName = userDTO.getUserNickname();

                    //글작성자와 현재 사용자 비교
                    if (userNickName.equals(nickName)) {
                        btnDeclear.setVisibility(View.GONE);
                    } else {
                        btnUpdate.setVisibility(View.GONE);
                        btnDelete.setVisibility(View.GONE);
                    }

                    Log.d("email", userNickName);
                }
            });
        }
    }
}
