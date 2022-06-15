package com.example.GoAutoCamping;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class Information_Mark extends AppCompatActivity {

    Information_Mark_RecylerAdapter adapter;
    RecyclerView recyclerView;
    ImageView noneImage;
    TextView noneText, noneTextTitle, toolbarText;

    Toolbar toolbar;

    ArrayList<RecommendDTO> recommendDTOS;
    List<String> dtos = new ArrayList<>();
    Information_Mark mThis;

    //파이어베이스
    private FirebaseAuth user;
    private FirebaseStorage storage;
    private String imageUrl = "";
    private FirebaseFirestore Firestore;
    private String email;

    boolean markbool = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_mark);

        //툴바생성
        toolbar = findViewById(R.id.infoTB);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //현재 사용자 정보
        user = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();


        toolbarText = findViewById(R.id.markText);
        noneTextTitle = findViewById(R.id.textTitle_none);
        noneText = findViewById(R.id.text_none);
        noneImage = findViewById(R.id.image_none);

        mThis = this;

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    public void load(){

        recommendDTOS = new ArrayList<>();

        Firestore.collectionGroup("innerPlaces")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                //도큐먼트 리스트 생성
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                for(DocumentSnapshot d : list){
                    if(dtos.contains(d.getId())){
                        RecommendDTO recommendDTO = d.toObject(RecommendDTO.class);
                        recommendDTO.setRecommendId(d.getId());

                        recommendDTOS.add(recommendDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }

                }

                if(recommendDTOS.size() == 0){
                    noneTextTitle.setVisibility(View.VISIBLE);
                    noneText.setVisibility(View.VISIBLE);
                    noneImage.setVisibility(View.VISIBLE);
                }
                else{
                    noneTextTitle.setVisibility(View.INVISIBLE);
                    noneText.setVisibility(View.INVISIBLE);
                    noneImage.setVisibility(View.INVISIBLE);
                }
                Information_Mark_RecylerAdapter adapter = new Information_Mark_RecylerAdapter(getApplicationContext(), recommendDTOS, mThis);
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new Information_Mark_RecylerAdapter.OnItemClickEventListener() {
                    @Override
                    public void onItemClick(View a_view, int a_position, List<RecommendDTO> commentDTOS) {
                        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0C9160"));
                        toolbar.setBackgroundDrawable(colorDrawable);

                        toolbarText.setVisibility(View.INVISIBLE);

                        markbool = true;

                        RecommendDTO dto = commentDTOS.get(a_position);

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        Fragment fragment = new Information_mark_detail();

                        Bundle bundle = new Bundle();
                        bundle.putString("placeName", dto.getRecommendAreaCode());
                        bundle.putString("placeId", dto.getRecommendId());

                        fragment.setArguments(bundle);
                        transaction.add(R.id.detailFrame, fragment, "mark").addToBackStack(null).commit();
                    }
                });

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();

            //사용자 정보가져오기
            DocumentReference docRef = Firestore.collection("users").document(email);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //사용자 이름 가져오기
                    UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);

                    dtos = userDTO.getUserFavorite();
                }
            });
            load();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            if(markbool){
                getSupportFragmentManager().popBackStack();
                markbool = false;

                ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00000000"));
                toolbar.setBackgroundDrawable(colorDrawable);

                toolbarText.setVisibility(View.VISIBLE);

                FirebaseUser currentUser = user.getCurrentUser();
                if (currentUser != null) {
                    email = currentUser.getEmail();

                    //사용자 정보가져오기
                    DocumentReference docRef = Firestore.collection("users").document(email);
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //사용자 이름 가져오기
                            UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);

                            dtos = userDTO.getUserFavorite();
                        }
                    });
                    load();
                }

            }
            else{
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
