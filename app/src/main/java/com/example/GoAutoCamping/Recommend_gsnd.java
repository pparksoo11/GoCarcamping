package com.example.GoAutoCamping;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;


public class Recommend_gsnd extends Fragment implements Recommend_detail_filiterdialog.InputSelected{


    public static final int DATEPICKER_FRAGMENT=1;
    View view;
    ListView listView;
    ArrayList<RecommendDTO> dtos;
    ArrayList<CommunityDTO> dtos2;
    ArrayList<CommunityDTO> dtos3;

    Context context;

    //리사이클러뷰
    RecyclerView recyclerView;
    List<Recommend_hotDTO> models;
    Recommend_Adapter adapter;

    //필터
    Recommend_detail_filiterdialog dlg;
    FloatingActionButton filterbtn;

    public boolean[] checked = {false, false, false, false, false, false, false, false, false, false, false};
    String[] checkingName = { "산", "강", "바다", "계곡", "캠핑장", "공원", "주차장", "화장실", "샤워실", "매점", "취사"};
    HorizontalScrollView chipLayout;
    ChipGroup chipGroup;
    TextView noFilter, noFilterResultTV;
    ImageView noFilterResultImg;

    //파이어베이스
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    CommunityDTO communityDTO;

    public static Recommend_gsnd newInstance(){
        return new Recommend_gsnd();
    }

    @Override
    public void clearAll() {
        clearChip();
    }

    @Override
    public void sendBoolenArray(boolean[] ch, String[] chName) {

        ArrayList<String> checkName = new ArrayList<String>();
        ArrayList<String> checkFilterName = new ArrayList<String>();

        for(int i=0; i <ch.length; i++){
            checked[i] = ch[i];

        }

        for(int i=0; i<checked.length; i++){
            Log.d("살려주세요",  i + "번째 " + checked[i]);

            if(checked[i])
                checkName.add(checkingName[i]);
            //checkFilterName.add(checkingFilter[i]);
        }

        clearChip();
        if(checkChipGroup())
            clearChip();
        addChipView(checkName);
        filterLoad(checked);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.recommend_gsnd, container, false);

        //하단바 숨기기
        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(true);

        ActionBar mainTB = ((MainActivity) getActivity()).getSupportActionBar();

        noFilterResultTV = view.findViewById(R.id.text_none);
        noFilterResultImg = view.findViewById(R.id.image_none);

        //리스트뷰

        listView = view.findViewById(R.id.list_recomend);

        load();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecommendDTO dto = (RecommendDTO) parent.getItemAtPosition(position);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment fragment = new Recommend_detail();

                Bundle bundle = new Bundle();
                bundle.putString("placeName", "place_gsnd");
                bundle.putString("placeId", dto.getRecommendId());

                fragment.setArguments(bundle);
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_right, R.anim.enter_from_right, R.anim.exit_from_right);
                transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
            }
        });


        //필터
        dlg = new Recommend_detail_filiterdialog();
        filterbtn = view.findViewById(R.id.filter);

        filterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dlg.setTargetFragment(Recommend_gsnd.this, 1);
                dlg.show(getActivity().getSupportFragmentManager(), "tag");
            }
        });

        //칩
        chipLayout = view.findViewById(R.id.chip_Layout);
        chipGroup = view.findViewById(R.id.chip_group);

        noFilter = view.findViewById(R.id.filter_text);


        //리사이클러뷰
        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        loadCommunity();

        return view;
    }

    //커뮤니티 데이터 가져오기
    public void loadCommunity(){
        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos2 = new ArrayList<>();
        dtos3 = new ArrayList<>();

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

                    Log.d("사이즈", dtos2.size()+"");

                    models = new ArrayList<>();
                    //TODO 나중에 이부분 5로 바꿔서 5개보여주기
                    int i =0;
                    for (int j = 0; j<dtos2.size(); j++) {
                        if(dtos2.get(j).getCommunityAddress2().contains("경상남도")) {
                            models.add(new Recommend_hotDTO(dtos2.get(j).getCommunityImage(), dtos2.get(j).getCommunityAddress()));
                            dtos3.add(dtos2.get(j));
                            i++;
                        }
                        if( i == 4)
                            break;
                    }

                    //어탭터 생성 및 setAdapter
                    adapter = new Recommend_Adapter(models, context);
                    recyclerView.setAdapter(adapter);

                    adapter.setOnItemClickListener(new Recommend_Adapter.Recommend_OnItemClickListener() {
                        @Override
                        public void onItemClick(Recommend_Adapter.ItemViewHolder holder, View view, int pos) {
                            openCommuDetail(pos);
                        }
                    });
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

    public void openCommuDetail(int pos) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment fragment = new Community_detail(pos);

        Bundle bundle = new Bundle();
        bundle.putString("postId", dtos3.get(pos).getCommunityId());
        bundle.putString("rec", "rec");
        bundle.putString("지역명", "경상남도");

        fragment.setArguments(bundle);
        transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
    }

    //장소추천 데이터 가져오기
    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        //장소 추천 데이터 - 별점 순
        Firestore.collection("places").document("place_gsnd").collection("innerPlaces")
                .orderBy("RecommendStar", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        RecommendDTO recommendDTO = d.toObject(RecommendDTO.class);
                        recommendDTO.setRecommendId(d.getId());
                        dtos.add(recommendDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }
                    //어탭터 생성 및 setAdapter
                    Recommend_detail_adapter adapter = new Recommend_detail_adapter(context, dtos);
                    listView.setAdapter(adapter);
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

    //필터로드
    public void filterLoad(boolean[] ch){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        //장소 추천 데이터 - 필터 값 받아오기
        Firestore.collection("places").document("place_gsnd").collection("innerPlaces")
                .orderBy("RecommendStar", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        RecommendDTO recommendDTO = d.toObject(RecommendDTO.class);
                        recommendDTO.setRecommendId(d.getId());
                        boolean check = false;

                        //필터 검사하기
                        for(int i = 0; i < 11; i++){
                            Log.d("필터 왜 안됨 ?", ""+ch[i]);
                            Log.d("필터 왜 안됨 ? 장소", ""+recommendDTO.getRecommendFilter().get(i));
                            if(ch[i] && recommendDTO.getRecommendFilter().get(i)){
                                check = true;
                            }
                        }

                        if(check){
                            dtos.add(recommendDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                        }
                    }

                    if(dtos.size() == 0){
                        noFilterResultTV.setVisibility(View.VISIBLE);
                        noFilterResultImg.setVisibility(View.VISIBLE);
                    }
                    else{
                        noFilterResultTV.setVisibility(View.INVISIBLE);
                        noFilterResultImg.setVisibility(View.INVISIBLE);
                    }

                    //어탭터 생성 및 setAdapter
                    Recommend_detail_adapter adapter = new Recommend_detail_adapter(context, dtos);
                    listView.setAdapter(adapter);
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

    //칩 그룹 검사
    public boolean checkChipGroup(){
        boolean tf = false;
        int i = chipGroup.getChildCount();
        Log.d("칩그룹에 있습니까?", i + "개 있음");

        if(i == 0)
            tf = true;

        return tf;

    }

    //칩 뷰 추가
    public void addChipView(ArrayList<String> name) {

        for (int i = 0; i < name.size(); i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.filter_chip_layout, chipGroup, false);
            chip.setText(name.get(i));

            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeChip(chip);
                }
            });

            chipGroup.addView(chip);
            noFilter.setVisibility(View.INVISIBLE);

        }
    }

    //칩 초기화
    public void clearChip(){
        int count = chipGroup.getChildCount();

        Log.d("몇개?", chipGroup.getChildCount()+ "게");

        for(int i = count-1; i > -1; i--){
            Log.d("몇번 돕니까?", i + "번");
            Chip chip = (Chip) chipGroup.getChildAt(i);
            chipGroup.removeView(chip);
        }
        noFilter.setVisibility(View.VISIBLE);
    }

    //칩 삭제
    public void removeChip(Chip chip){
        if(chip == null) return;

        //칩 이름 가져와서 칩정보랑 검사 - 칩 삭제 시 리스트 변경
        for(int i = 0; i < checkingName.length; i++){
            if(checkingName[i].equals(chip.getText()))
                checked[i] = false;
        }

        chipGroup.removeView(chip);

        //칩 정보가 하나도 없다면 - 별점 순으로 다시 로딩
        if(checkChipGroup()){
            noFilter.setVisibility(View.VISIBLE);
            load();
        }
        //칩 정보가 존재하면 - 존재하는 칩 정보로 다시 로딩
        else{
            filterLoad(checked);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if(activity!=null){
            ((MainActivity)activity).setBackBtn(1,true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        FragmentActivity activity = getActivity();
        ((MainActivity)activity).hideBottomNavi(false);
    }


}