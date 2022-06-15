package com.example.GoAutoCamping;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Information_post_detail extends Fragment{

    View view;
    private CommentAdapter mAdapter;
    private DatabaseReference mCommentsReference;

    ImageView btnGood;
    ImageView image1, btnOption;
    EditText commentEdit;
    MaterialButton postComment;
    CircleImageView profileImg;
    TextView userN, textZone, textUserId, textContent, textLikeNum;
    RatingBar starRate;

    //파이어베이스
    private FirebaseStorage storage;
    private String imageUrl="";
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    //유저 정보
    String userNickName, userProfile;
    String postId;

    int position;

    CoordinatorLayout coordinatorLayout;
    RecyclerView recyclerView;

    //커뮤니티 데이터형 어레이리스트 변수
    ArrayList<CommunityDTO> dtos;




    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //하단바 숨기기

        view = inflater.inflate(R.layout.community_detail, container, false);

        user = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        btnGood = view.findViewById(R.id.btnGood);
        image1 = view.findViewById(R.id.image1);
        profileImg = view.findViewById(R.id.profileImg);
        userN = view.findViewById(R.id.userN);
        textZone = view.findViewById(R.id.textZone);
        starRate = view.findViewById(R.id.starRate);
        textUserId = view.findViewById(R.id.textUserId);
        textContent = view.findViewById(R.id.textContent);
        btnOption = view.findViewById(R.id.btnOption);
        textLikeNum = view.findViewById(R.id.textLikeNum);

        coordinatorLayout = view.findViewById(R.id.snackbar_line);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView = view.findViewById(R.id.comment_recycler);
        commentEdit = view.findViewById(R.id.fieldCommentText);
        postComment = view.findViewById(R.id.buttonPostComment);

        //번들 받아오는 부분
        Bundle bundle = getArguments();

        if(bundle != null){
            String update = bundle.getString("update");
            if (update != null) {
                load();
                postId = bundle.getString("postId");
            }
            else {
                postId = bundle.getString("postId");
            }
        }

        //댓글 파이어베이스
        mCommentsReference = FirebaseDatabase.getInstance().getReference().child("Community").child(postId);

        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new CommentAdapter(getContext(), mCommentsReference);

        //댓글 삭제
        mAdapter.setOnItemLongClickListener(new CommentAdapter.OnItemLongClickEventListener() {
            @Override
            public void onItemLongClick(View a_view, int a_position, List<String> commentIds, List<CommentDTO> commentDTOS) {
                CommentDTO commentDTO = commentDTOS.get(a_position);
                String commentId = commentIds.get(a_position);

                //등록되어있는 메일이 같으면 삭제하기
                if(commentDTO.userId_comment.equals(email)){
                    MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "댓글 삭제");
                    dialog.message(null, "댓글을 삭제하시겠습니까?", null);
                    dialog.positiveButton(null, "삭제", materialDialog -> {
                        Snackbar.make(coordinatorLayout, "댓글이 삭제되었습니다!", Snackbar.LENGTH_SHORT).show();
                        onDeleteContent(commentId);
                        return null;
                    });
                    dialog.negativeButton(null, "아니요", materialDialog -> {
                        dialog.dismiss();
                        return null;
                    });
                    dialog.show();
                }
                else{
                    Snackbar.make(coordinatorLayout, "댓글 작성자가 아닙니다", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        //댓글 달기
        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        recyclerView.setAdapter(mAdapter);

        GradientDrawable drawable = (GradientDrawable)getContext().getDrawable(R.drawable.community_edge);

        image1.setBackground(drawable);
        image1.setClipToOutline(true);

        //좋아요 연동
        btnGood.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                addlike();
            }
        });

        //옵션 바텀시트
        btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //값 전달
                Bundle bundle = new Bundle();
                bundle.putString("address", dtos.get(0).getCommunityAddress());
                bundle.putString("content", dtos.get(0).getCommunityContent());
                bundle.putString("id", dtos.get(0).getCommunityId());
                bundle.putString("image", dtos.get(0).getCommunityImage());
                bundle.putInt("like", dtos.get(0).getCommunityLike());
                bundle.putStringArrayList("likeUser", dtos.get(0).getCommunityLikeUser());
                bundle.putString("uploadTime", dtos.get(0).getCommunityUploadTime().toString());
                bundle.putFloat("star", dtos.get(0).getCommunityStar());
                bundle.putString("nickName", dtos.get(0).getCommunityUserNickName());
                bundle.putString("profile", dtos.get(0).getCommunityUserProfile());
                bundle.putString("postId", postId);
                bundle.putInt("pos", position);

                Information_post_detail_option bottomSheetDialog = Information_post_detail_option.getInstance();
                bottomSheetDialog.setArguments(bundle);
                bottomSheetDialog.show(getChildFragmentManager(), "bottomSheet");

            }
        });

        load();

        return view;
    }

    public void checklike(){
        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("communication").document(postId);

        //좋아요를 누른 사용자인지 검사해주기
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("communityLikeUser");

                if(group.contains(email)){
                    btnGood.setImageResource(R.drawable.like_full);
                }
                else{
                    btnGood.setImageResource(R.drawable.like);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }



    //중복 클릭 막기
    public abstract class OnSingleClickListener implements View.OnClickListener{
        //중복 클릭 방지 시간 설정 ( 해당 시간 이후에 다시 클릭 가능 )
        private static final long MIN_CLICK_INTERVAL = 1000; //1sec
        private long mLastClickTime = 0;
        public abstract void onSingleClick(View v);
        @Override public final void onClick(View v) {
            long currentClickTime = SystemClock.uptimeMillis();
            long elapsedTime = currentClickTime - mLastClickTime;
            mLastClickTime = currentClickTime;
            // 중복클릭 아닌 경우
            if (elapsedTime > MIN_CLICK_INTERVAL) {
                onSingleClick(v);
            }
        }
    }

    //좋아요 추가
    public void addlike(){
        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("communication").document(postId);

        //좋아요를 누른 사용자인지 검사해주기
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("communityLikeUser");

                if(group.contains(email)){
                    documentReference.update("communityLikeUser", FieldValue.arrayRemove(email));
                    btnGood.setImageResource(R.drawable.like);
                }
                else{
                    documentReference.update("communityLikeUser", FieldValue.arrayUnion(email));
                    btnGood.setImageResource(R.drawable.like_full);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        //좋아요 여부에 따라 좋아요 수 조정해주기 ( 속도가 너무 느림 )
        Firestore.runTransaction(new Transaction.Function<Double>() {
            @Nullable
            @Override
            public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(documentReference);
                ArrayList<String> group = (ArrayList<String>) snapshot.get("communityLikeUser");

                Double likenum = Double.valueOf(group.size());

                if(group.contains(email) && likenum > 0){
                    likenum = likenum - 1;
                    transaction.update(documentReference, "communityLike", likenum);
                }
                else{
                    likenum = likenum + 1;
                    transaction.update(documentReference, "communityLike", likenum);
                }

                return likenum;
            }
        }).addOnSuccessListener(new OnSuccessListener<Double>() {
            @Override
            public void onSuccess(Double integer) {
                int i = integer.intValue();
                textLikeNum.setText(i + "");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    //커뮤니티 데이터 가져오기
    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        Firestore.collection("communication").document(postId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                CommunityDTO communityDTO = documentSnapshot.toObject(CommunityDTO.class);

                dtos.add(communityDTO);

                userN.setText(communityDTO.getCommunityUserNickName());
                textZone.setText(communityDTO.getCommunityAddress());
                Glide.with(view.getContext())
                        .load(communityDTO.getCommunityUserProfile())
                        .into(profileImg);
                Glide.with(view.getContext())
                        .load(communityDTO.getCommunityImage())
                        .into(image1);
                textContent.setText(communityDTO.getCommunityContent());
                textUserId.setText(communityDTO.getCommunityUserNickName());
                starRate.setRating(communityDTO.getCommunityStar());
                textLikeNum.setText(communityDTO.getCommunityLike() + "");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "커뮤니티 디테일 로드 실패");
            }
        });
    }

    //댓글 달기
    private void postComment() {

        // 댓글 객체 만들기
        String commentText = commentEdit.getText().toString();
        CommentDTO comment = new CommentDTO(email, userProfile, userNickName, commentText);

        // 댓글 추가하기
        mCommentsReference.push().setValue(comment);

        // 텍스트 필드 비워주기
        commentEdit.setText(null);
    }

    //댓글 삭제
    private void onDeleteContent(String item)
    {
        mCommentsReference.child(item).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) { }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) { }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        checklike();
    }

    //시작 시 사용자 정보 가져오기
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();

            //사용자 정보가져오기
            DocumentReference docRef = Firestore.collection("users").document(email);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //사용자 닉네임 가져오기
                    UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);
                    userNickName = userDTO.getUserNickname();
                    userProfile = userDTO.getUserProfile();
                }
            });
        }
    }
}
