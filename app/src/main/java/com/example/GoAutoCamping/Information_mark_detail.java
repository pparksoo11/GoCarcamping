package com.example.GoAutoCamping;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Information_mark_detail extends Fragment implements Recommend_detail_reviewdialog.SendData{

    Context context;
    FloatingActionButton likebtn;
    ExtendedFloatingActionButton reviewbtn;
    String placeName, placeId;

    CoordinatorLayout coordinatorLayout;

    ImageView imageV, noneImage;
    TextView name_detail, address, ratingText, reviewNumText, noneText;
    RecyclerView recyclerView_filter;
    List<Boolean> RecommendFilter = new ArrayList<>();
    List<Recommend_filterdesDTO> filterdesDTOS;
    ArrayList<String> checkName;
    Recommend_detail_filter_RecyclerAdapter adapter;

    String[] checkingName = { "산", "강", "바다", "계곡", "캠핑장", "공원", "주차장", "화장실", "샤워실", "매점", "취사"};
    ChipGroup chipGroup;

    RecyclerView recyclerView_review;
    Recommend_detail_reviewdialog dlg;
    private Recommend_detail_review_RecyclerAdapter review_adapter;
    private DatabaseReference ReviewsReference;
    Map<String, Recommend_reviewDTO> reviews = new HashMap<>();
    public boolean checkReview = true;

    Float rating = 0f;
    int reviewNum = 0;

    //파이어베이스
    private FirebaseStorage storage;
    private String imageUrl="";
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    //유저 정보
    String userNickName, userProfile;

    @Override
    public void sendData(float rating, String review) {
        // 데이터 보내기
        postComment(review, rating);
    }

    @Override
    public void clearAll() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recommend_detail, container, false);

        likebtn = view.findViewById(R.id.like);
        imageV = view.findViewById(R.id.image_detail);
        name_detail = view.findViewById(R.id.name_detail);
        address = view.findViewById(R.id.address_detail);
        reviewbtn = view.findViewById(R.id.reviewBtn);
        ratingText = view.findViewById(R.id.ratingText);
        reviewNumText = view.findViewById(R.id.reviewNumText);
        coordinatorLayout = view.findViewById(R.id.snackbar_line);
        noneImage = view.findViewById(R.id.image_none);
        noneText = view.findViewById(R.id.text_none);

        chipGroup = view.findViewById(R.id.chip_group);

        recyclerView_filter = view.findViewById(R.id.filterDes_detail);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_filter.setLayoutManager(layoutManager);

        recyclerView_review = view.findViewById(R.id.review_detail);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView_review.setLayoutManager(layoutManager2);


        Bundle bundle = getArguments();

        if(bundle != null){
            placeName = bundle.getString("placeName");
            placeId = bundle.getString("placeId");

            Firestore = FirebaseFirestore.getInstance();

            load();
            reviewAdapter();
            checkReviewUser();
            calRate();
        }

        //즐겨찾기 버튼 클릭
        likebtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                boolean ch = checkLogin();
                if(ch){
                    addlike();
                }
            }
        });

        //리뷰버튼
        dlg = new Recommend_detail_reviewdialog();

        reviewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ch = checkLogin();
                if(ch){
                    if(checkReview){
                        dlg.setTargetFragment(Information_mark_detail.this, 1);
                        dlg.show(getActivity().getSupportFragmentManager(), "tag");
                    }
                    else{
                        Snackbar.make(coordinatorLayout, "이미 리뷰가 존재합니다", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    public void filterload(){

        checkName = new ArrayList<>();
        filterdesDTOS = new ArrayList<>();

        Log.d("필터", "실행됌?" +  RecommendFilter.size());
        for(int i = 0; i < 7; i++){

            if(RecommendFilter.get(i))
                filterdesDTOS.add(new Recommend_filterdesDTO(i));
        }

        //칩 필터
        for(int i = 7; i < RecommendFilter.size(); i++){
            if(RecommendFilter.get(i))
                checkName.add(checkingName[i]);
        }

        adapter = new Recommend_detail_filter_RecyclerAdapter(context, filterdesDTOS);
        recyclerView_filter.setAdapter(adapter);

        addChipView(checkName);
    }

    public void addChipView(ArrayList<String> name) {

        for (int i = 0; i < name.size(); i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.filter_chip_layout, chipGroup, false);
            chip.setText(name.get(i));
            chip.setCloseIconVisible(false);
            chip.setClickable(false);

            chipGroup.addView(chip);
        }
    }

    //장소추천 데이터 가져오기
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        DocumentReference docRef = Firestore.collection("places").document(placeName).collection("innerPlaces").document(placeId);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                RecommendDTO recommendDTO = documentSnapshot.toObject(RecommendDTO.class);

                Glide.with(getContext())
                        .load(recommendDTO.getRecommendImage())
                        .into(imageV);

                name_detail.setText(recommendDTO.getRecommendTitle());

                address.setText(recommendDTO.getRecommendAddress());

                for(int i = 0; i < recommendDTO.getRecommendFilter().size(); i++) {
                    RecommendFilter.add(recommendDTO.getRecommendFilter().get(i));
                }

                filterload();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public boolean checkLogin(){
        boolean ch = false;

        if(email != null){
            ch = true;
            return ch;
        }
        else{
            //다이얼로그 띄워주기
            MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
            dialog.title(null, "로그인 오류");
            dialog.message(null, "로그인이 필요한 작업입니다. \n로그인 해주세요.", null);
            //dialog.icon(null, getResources().getDrawable(R.drawable.ic_baseline_report_24));
            dialog.positiveButton(null, "네", materialDialog -> {
                dialog.dismiss();
                return null;
            });
            dialog.show();
        }
        return ch;

    }

    //좋아요 누른 사용자 검사
    public void checklike(){
        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("places").document(placeName).collection("innerPlaces").document(placeId);

        //좋아요를 누른 사용자인지 검사해주기
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("RecommendLikeUser");

                if(group.contains(email)){
                    likebtn.setImageResource(R.drawable.like_full);
                }
                else{
                    likebtn.setImageResource(R.drawable.like);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    //좋아요 추가
    public void addlike(){
        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("places").document(placeName).collection("innerPlaces").document(placeId);
        final DocumentReference documentReferenceUser = Firestore.collection("users").document(email);

        //즐겨찾기 목록에 추가 - 유저
        documentReferenceUser.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("userFavorite");

                if(group.contains(placeId)){
                    documentReferenceUser.update("userFavorite", FieldValue.arrayRemove(placeId));
                }
                else{
                    documentReferenceUser.update("userFavorite", FieldValue.arrayUnion(placeId));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        //좋아요 사용자 목록에 추가 - 장소
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> group = (ArrayList<String>) documentSnapshot.get("RecommendLikeUser");

                if(group.contains(email)){
                    documentReference.update("RecommendLikeUser", FieldValue.arrayRemove(email));
                    likebtn.setImageResource(R.drawable.like);

                }
                else{
                    documentReference.update("RecommendLikeUser", FieldValue.arrayUnion(email));
                    likebtn.setImageResource(R.drawable.like_full);

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        //좋아요 여부에 따라 좋아요 수 조정해주기
        Firestore.runTransaction(new Transaction.Function<Double>() {
            @Nullable
            @Override
            public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(documentReference);
                ArrayList<String> group = (ArrayList<String>) snapshot.get("RecommendLikeUser");

                Double likenum = Double.valueOf(group.size());

                if(group.contains(email) && likenum > 0){
                    likenum = likenum - 1;
                    transaction.update(documentReference, "RecommendLike", likenum);
                }
                else{
                    likenum = likenum + 1;
                    transaction.update(documentReference, "RecommendLike", likenum);
                }

                return likenum;
            }
        }).addOnSuccessListener(new OnSuccessListener<Double>() {
            @Override
            public void onSuccess(Double integer) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public void calRate(){

        Firestore = FirebaseFirestore.getInstance();

        final DocumentReference documentReference = Firestore.collection("places").document(placeName).collection("innerPlaces").document(placeId);

        ReviewsReference =  FirebaseDatabase.getInstance().getReference().child("Places").child(placeId);
        Query reviewQuery = ReviewsReference.orderByChild("recommendReviewStar");

        reviewQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Float rate = 0f;
                int rateNum = 0;

                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Recommend_reviewDTO reviewDTO = postSnapshot.getValue(Recommend_reviewDTO.class);
                    rate += reviewDTO.getRecommendReviewStar();
                    rateNum++;
                }

                rating = rate;
                reviewNum = rateNum;

                String r = String.format("%.1f", (rating / reviewNum));
                if(r.equals("NaN")){
                    ratingText.setText("0.0");
                    documentReference.update("RecommendStar", 0f);
                    noneImage.setVisibility(View.VISIBLE);
                    noneText.setVisibility(View.VISIBLE);
                }else{
                    ratingText.setText(r);
                    documentReference.update("RecommendStar", rating / reviewNum);
                    noneImage.setVisibility(View.INVISIBLE);
                    noneText.setVisibility(View.INVISIBLE);
                }
                reviewNumText.setText("총 " + reviewNum + "명이 평가했습니다");

                Log.d("레이팅바", "총별점 : " + rating);
                Log.d("레이팅바", "갯수 : " + reviewNum);
                Log.d("레이팅바", "평균별점 : " + rating / reviewNum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //리뷰를 작성한 사용자인지 검사
    public void checkReviewUser(){

        ReviewsReference =  FirebaseDatabase.getInstance().getReference().child("Places").child(placeId);
        Query reviewQuery = ReviewsReference.orderByChild("recommendReviewStar");

        reviewQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Recommend_reviewDTO reviewDTO = postSnapshot.getValue(Recommend_reviewDTO.class);
                    if(reviewDTO.getRecommendReviewId().equals(email))
                        checkReview = false;
                    else{
                        checkReview = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void reviewAdapter(){
        ReviewsReference = FirebaseDatabase.getInstance().getReference().child("Places").child(placeId);
        review_adapter = new Recommend_detail_review_RecyclerAdapter(context, ReviewsReference);

        review_adapter.setOnItemLongClickListener(new Recommend_detail_review_RecyclerAdapter.OnItemLongClickEventListener() {
            @Override
            public void onItemLongClick(View a_view, int a_position,List<String> mReviewIds, List<Recommend_reviewDTO> reviewDTOS) {
                Recommend_reviewDTO recommend_reviewDTO = reviewDTOS.get(a_position);
                String reviewID = mReviewIds.get(a_position);

                //등록되어있는 메일이 같으면 삭제하기
                if(recommend_reviewDTO.getRecommendReviewId().equals(email)){
                    MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "리뷰 삭제");
                    dialog.message(null, "리뷰를 삭제하시겠습니까?", null);
                    dialog.positiveButton(null, "삭제", materialDialog -> {
                        Snackbar.make(coordinatorLayout, "리뷰가 삭제되었습니다!", Snackbar.LENGTH_SHORT).show();
                        onDeleteContent(reviewID);

                        return null;
                    });
                    dialog.negativeButton(null, "아니요", materialDialog -> {
                        dialog.dismiss();
                        return null;
                    });
                    dialog.show();
                }
                else{
                    Snackbar.make(coordinatorLayout, "리뷰 작성자가 아닙니다", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView_review.setAdapter(review_adapter);
    }

    //댓글 달기
    private void postComment(String reviewText, float reviewRating) {
        // 리뷰 객체 만들기
        Recommend_reviewDTO review = new Recommend_reviewDTO(email, userProfile, userNickName, reviewText, reviewRating);

        // 리뷰 추가하기
        ReviewsReference.push().setValue(review);


    }

    //댓글 삭제
    private void onDeleteContent(String item)
    {
        ReviewsReference.child(item).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        checkReview = true;
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) { }
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null){
            email = currentUser.getEmail();

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
