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
import android.view.inputmethod.InputMethodManager;
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

public class Community_detail extends Fragment implements Community_declear.Cleared{

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

    ArrayList<CommunityDTO> dtos2;

    CommunityDTO communityDTO;

    String rec2, home;

    ArrayList<CommunityDTO> communityData;

    String word, address, content;

    //포지션값 전달받음
    Community_detail(int position) {
        this.position = position;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //하단바 숨기기
        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(true);

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
            home = bundle.getString("home");
            String rec = bundle.getString("rec");
            rec2 = bundle.getString("지역명");
            word = bundle.getString("word");
            address = bundle.getString("address");
            content = bundle.getString("content");

            //홈화면에서 연결
            if(home != null) {
                loadHome();
                postId = bundle.getString("postId");
            }

            //장소추천에서 연결
            else if (rec != null) {
                loadRecommendCommunity();
                postId = bundle.getString("postId");
            }

            //검색 디테일 연결
            else if (address != null) {
                loadSearch();
                postId = bundle.getString("postId");
            }

            //게시물 수정
            else if (update != null) {
                load();
                postId = bundle.getString("postId");
            }
            //일반 클릭
            else {
                load();
                postId = bundle.getString("postId");
            }
        }

        Log.d("커뮤니티이름", postId);

        if(email == null)
            btnOption.setVisibility(View.GONE);

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
                if(email != null) {
                    postComment();
                }
                else {
                    //다이얼로그 띄워주기
                    MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "로그인 오류");
                    dialog.message(null, "로그인이 필요한 작업입니다. \n로그인 해주세요.", null);
                    //dialog.icon(null, getResources().getDrawable(R.drawable.ic_baseline_report_24));
                    dialog.positiveButton(null, "확인", materialDialog -> {
                        dialog.dismiss();
                        return null;
                    });
                    dialog.show();
                }
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
                if(email != null) {
                    addlike();
                }
                else {
                    //다이얼로그 띄워주기
                    MaterialDialog dialog = new MaterialDialog(getContext(), MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "로그인 오류");
                    dialog.message(null, "로그인이 필요한 작업입니다. \n로그인 해주세요.", null);
                    //dialog.icon(null, getResources().getDrawable(R.drawable.ic_baseline_report_24));
                    dialog.positiveButton(null, "확인", materialDialog -> {
                        dialog.dismiss();
                        return null;
                    });
                    dialog.show();
                    btnGood.setImageResource(R.drawable.like);
                }
            }
        });

        //옵션 바텀시트
        btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //값 전달
                Bundle bundle = new Bundle();

                if (rec2 != null) {
                    bundle.putString("rec2", rec2);
                }
                else if (home != null) {
                    bundle.putString("home", home);
                }
                else if (word != null) {
                    bundle.putString("word", word);
                }

                bundle.putString("address", dtos.get(position).getCommunityAddress());
                bundle.putString("content", dtos.get(position).getCommunityContent());
                bundle.putString("id", dtos.get(position).getCommunityId());
                bundle.putString("image", dtos.get(position).getCommunityImage());
                bundle.putInt("like", dtos.get(position).getCommunityLike());
                bundle.putStringArrayList("likeUser", dtos.get(position).getCommunityLikeUser());
                bundle.putString("uploadTime", dtos.get(position).getCommunityUploadTime().toString());
                bundle.putFloat("star", dtos.get(position).getCommunityStar());
                bundle.putString("nickName", dtos.get(position).getCommunityUserNickName());
                bundle.putString("profile", dtos.get(position).getCommunityUserProfile());
                bundle.putString("postId", postId);
                bundle.putInt("pos", position);


                Community_detail_option bottomSheetDialog = Community_detail_option.getInstance();
                bottomSheetDialog.setArguments(bundle);
                bottomSheetDialog.show(getChildFragmentManager(), "bottomSheet");
            }
        });

        //load();



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

    @Override
    public void clearAll(boolean checking) {
        if(checking){
            getActivity().getSupportFragmentManager().popBackStack();
        }
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
    public void loadSearch(){
        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        communityData = new ArrayList<>();

        //커뮤니티의 모든 데이터 가져오기 - onComplete가 안먹어서 Success로 바꿨음 그에 따라 아래 방법도 바뀜
        Firestore.collection("communication").orderBy("communityUploadTime", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    Log.d("실행중2", "실행중2");
                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        communityDTO = d.toObject(CommunityDTO.class);
                        communityDTO.setCommunityId(d.getId());
                        Log.d("실행중", "실행중");
                        communityData.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }
                    //TODO 나중에 이부분 5로 바꿔서 5개보여주기
                    for (int j = 0; j<communityData.size(); j++) {
                        if(communityData.get(j).getCommunityAddress().contains(word) || communityData.get(j).getCommunityContent().contains(word)) {
                            dtos.add(communityData.get(j));
                        }
                    }

                    //디비에서 가져온 정보를 보여줌
                    userN.setText(dtos.get(position).getCommunityUserNickName());
                    textZone.setText(dtos.get(position).getCommunityAddress());
                    Glide.with(view.getContext())
                            .load(dtos.get(position).getCommunityUserProfile())
                            .into(profileImg);
                    Glide.with(view.getContext())
                            .load(dtos.get(position).getCommunityImage())
                            .into(image1);
                    textContent.setText(dtos.get(position).getCommunityContent());
                    textUserId.setText(dtos.get(position).getCommunityUserNickName());
                    starRate.setRating(dtos.get(position).getCommunityStar());
                    textLikeNum.setText(dtos.get(position).getCommunityLike() + "");
                }
                else {
                    Log.d("값 없음", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "");
            }
        });
    }

    //커뮤니티 데이터 가져오기
    public void loadRecommendCommunity(){
        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        dtos2 = new ArrayList<>();

        //커뮤니티의 모든 데이터 가져오기 - onComplete가 안먹어서 Success로 바꿨음 그에 따라 아래 방법도 바뀜
        Firestore.collection("communication").orderBy("communityLike", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    Log.d("실행중2", "실행중2");
                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        communityDTO = d.toObject(CommunityDTO.class);
                        communityDTO.setCommunityId(d.getId());
                        Log.d("실행중", "실행중");
                        dtos2.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }
                    //TODO 나중에 이부분 5로 바꿔서 5개보여주기
                    for (int j = 0; j<dtos2.size(); j++) {
                        if(dtos2.get(j).getCommunityAddress2().contains(rec2)) {
                            dtos.add(dtos2.get(j));
                        }
                    }

                    //디비에서 가져온 정보를 보여줌
                    userN.setText(dtos.get(position).getCommunityUserNickName());
                    textZone.setText(dtos.get(position).getCommunityAddress());
                    Glide.with(view.getContext())
                            .load(dtos.get(position).getCommunityUserProfile())
                            .into(profileImg);
                    Glide.with(view.getContext())
                            .load(dtos.get(position).getCommunityImage())
                            .into(image1);
                    textContent.setText(dtos.get(position).getCommunityContent());
                    textUserId.setText(dtos.get(position).getCommunityUserNickName());
                    starRate.setRating(dtos.get(position).getCommunityStar());
                    textLikeNum.setText(dtos.get(position).getCommunityLike() + "");
                }
                else {
                    Log.d("값 없음", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "");
            }
        });
    }

    //커뮤니티 데이터 가져오기
    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        //커뮤니티의 모든 데이터 가져오기 - onComplete가 안먹어서 Success로 바꿨음 그에 따라 아래 방법도 바뀜
        Firestore.collection("communication").orderBy("communityUploadTime", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        dtos.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }

                    //디비에서 가져온 정보를 보여줌
                    userN.setText(dtos.get(position).getCommunityUserNickName());
                    textZone.setText(dtos.get(position).getCommunityAddress());
                    Glide.with(view.getContext())
                            .load(dtos.get(position).getCommunityUserProfile())
                            .into(profileImg);
                    Glide.with(view.getContext())
                            .load(dtos.get(position).getCommunityImage())
                            .into(image1);
                    textContent.setText(dtos.get(position).getCommunityContent());
                    textUserId.setText(dtos.get(position).getCommunityUserNickName());
                    starRate.setRating(dtos.get(position).getCommunityStar());
                    textLikeNum.setText(dtos.get(position).getCommunityLike() + "");
                }
                else {
                    Log.d("값 없음", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "");
            }
        });
    }

    //커뮤니티 데이터 가져오기 (홈에서 연결)
    public void loadHome(){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        //커뮤니티의 모든 데이터 가져오기 - onComplete가 안먹어서 Success로 바꿨음 그에 따라 아래 방법도 바뀜
        Firestore.collection("communication").orderBy("communityLike", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        dtos.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }

                    //디비에서 가져온 정보를 보여줌
                    userN.setText(dtos.get(position).getCommunityUserNickName());
                    textZone.setText(dtos.get(position).getCommunityAddress());
                    Glide.with(view.getContext())
                            .load(dtos.get(position).getCommunityUserProfile())
                            .into(profileImg);
                    Glide.with(view.getContext())
                            .load(dtos.get(position).getCommunityImage())
                            .into(image1);
                    textContent.setText(dtos.get(position).getCommunityContent());
                    textUserId.setText(dtos.get(position).getCommunityUserNickName());
                    starRate.setRating(dtos.get(position).getCommunityStar());
                    textLikeNum.setText(dtos.get(position).getCommunityLike() + "");
                }
                else {
                    Log.d("값 없음", "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("실패함", "");
            }
        });
    }
    //댓글 달기
    private void postComment() {

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(commentEdit.getWindowToken(), 0);//소프트 키 자동 내림

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

        Log.d("디테일 다시 호출됌", "디테일 호출");

        FragmentActivity activity = getActivity();
        if(activity!=null){
            ((MainActivity)activity).setBackBtn(1,true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(rec2 != null) {
            ;
        }
        else {
            FragmentActivity activity = getActivity();
            ((MainActivity) activity).hideBottomNavi(false);
        }
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

