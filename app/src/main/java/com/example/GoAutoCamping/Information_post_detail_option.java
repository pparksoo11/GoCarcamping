package com.example.GoAutoCamping;

import android.app.Activity;
import android.content.Context;
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

public class Information_post_detail_option extends BottomSheetDialogFragment implements View.OnClickListener{

    public static Information_post_detail_option getInstance() { return new Information_post_detail_option(); }

    private LinearLayout btnUpdate;
    private LinearLayout btnDelete;
    private LinearLayout btnClose;
    private LinearLayout btnDeclear;
    String address, content, id, image, uploadTime, nickName, profile, postId;
    Float star;
    int like, pos;
    ArrayList<String> likeUser;

    private FirebaseAuth user;
    private FirebaseFirestore Firestore;
    private String email, userNickName;
    private FirebaseDatabase firebaseDatabase;


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

                            getParentFragment().getActivity().getSupportFragmentManager().popBackStack();
                            ((Information_post)getActivity()).reloadAfterDelete();

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
                Toast.makeText(getContext(),"Close",Toast.LENGTH_SHORT).show();
                dismiss();

                break;
            case R.id.btnDeclear:
                Bundle bundle = new Bundle();
                bundle.putString("postId", postId);

                Community_declear community_declear = new Community_declear();
                community_declear.setArguments(bundle);
                community_declear.show(getActivity().getSupportFragmentManager(), "declear");
                dismiss();
                break;
        }
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
                ((Information_post_detail)getParentFragment()).load();
                dismiss();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
                    if(userNickName.equals(nickName)) {
                        btnDeclear.setVisibility(View.GONE);
                    }
                    else {
                        btnUpdate.setVisibility(View.GONE);
                        btnDelete.setVisibility(View.GONE);
                    }

                    Log.d("email", userNickName);
                }
            });
        }
    }
}
