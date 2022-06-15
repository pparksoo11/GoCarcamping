package com.example.GoAutoCamping;

import android.content.Intent;
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

public class Information_post extends AppCompatActivity {

    Information_Post_Adapter adapter;
    RecyclerView recyclerView;
    ImageView noneImage;
    TextView noneText, noneTextTitle, toolbarText;

    Toolbar toolbar;

    List<String> dtos = new ArrayList<>();
    ArrayList<CommunityDTO> communityDTOS;

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
        setContentView(R.layout.information);

        //툴바생성
        toolbar = findViewById(R.id.postTB);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //현재 사용자 정보
        user = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        toolbarText = findViewById(R.id.toolbarText);
        noneTextTitle = findViewById(R.id.textTitle_none);
        noneText = findViewById(R.id.text_none);
        noneImage = findViewById(R.id.image_none);

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }


    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        communityDTOS = new ArrayList<>();

        Firestore.collectionGroup("communication")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                //도큐먼트 리스트 생성
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                for(DocumentSnapshot d : list){
                    Log.d("값 가져와짐?", d.getId());
                    if(dtos.contains(d.getId())){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        communityDTO.setCommunityId(d.getId());
                        Log.d("값 가져와짐?", d.getId());

                        communityDTOS.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }

                }

                Log.d("사이즈", communityDTOS.size() +"");
                if(communityDTOS.size() == 0){
                    noneTextTitle.setVisibility(View.VISIBLE);
                    noneText.setVisibility(View.VISIBLE);
                    noneImage.setVisibility(View.VISIBLE);
                }
                else{
                    noneTextTitle.setVisibility(View.INVISIBLE);
                    noneText.setVisibility(View.INVISIBLE);
                    noneImage.setVisibility(View.INVISIBLE);
                }

                Information_Post_Adapter adapter = new Information_Post_Adapter(getApplicationContext(), communityDTOS);
                recyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new Information_Post_Adapter.OnItemClickEventListener() {
                    @Override
                    public void onItemClick(View a_view, int a_position, List<CommunityDTO> communityDTOS) {
                        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0C9160"));
                        toolbar.setBackgroundDrawable(colorDrawable);

                        toolbarText.setVisibility(View.INVISIBLE);

                        markbool = true;

                        CommunityDTO communityDTO = communityDTOS.get(a_position);

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        Fragment fragment = new Information_post_detail();

                        Bundle bundle = new Bundle();
                        bundle.putString("postId", communityDTO.getCommunityId());

                        fragment.setArguments(bundle);
                        transaction.add(R.id.detailFrame, fragment, "").addToBackStack(null).commit();


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

                    dtos = userDTO.getUserPosts();
                }
            });

            load();
        }


    }

    public void getUserPosts(){
        user = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();

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

                    dtos = userDTO.getUserPosts();
                }
            });
            load();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            if(markbool){
                reloadAfterDelete();
            }
            else{
                Intent intent = new Intent();
                intent.putExtra("name", 1);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void reloadAfterDelete(){
        getSupportFragmentManager().popBackStack();
        markbool = false;

        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#00000000"));
        toolbar.setBackgroundDrawable(colorDrawable);

        toolbarText.setVisibility(View.VISIBLE);

        getUserPosts();



    }
}
