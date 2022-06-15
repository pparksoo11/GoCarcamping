package com.example.GoAutoCamping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class Community extends Fragment {

    public View view;
    public GridView gridV;
    EditText search;
    FloatingActionButton btnPlus;

    Context context;

    ArrayList<CommunityDTO> dtos;

    //파이어베이스
    private FirebaseStorage storage;
    private String imageUrl="";
    public FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    String userNickName;

    String word, address, content, w;
    ArrayList<CommunityDTO> communityData;
    Button btnSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.community, container, false);

        gridV = view.findViewById(R.id.gridV);
        btnPlus = view.findViewById(R.id.btnPlus);
        search = view.findViewById(R.id.searchView);
        btnSearch = view.findViewById(R.id.btnSearch);

        user = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        //플러스 버튼 클릭 시 ->게시물 추가 페이지로 이동
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ch = checkLogin();
                if(ch){
                    Intent intent = new Intent(getActivity(), Community_add.class);
                    startActivityForResult(intent, 1);

                    getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.none);
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                word = search.getText().toString();
                Log.d("keywordTest", ""+word);
                searchWord(word); //검색어
                search.setText("");
            }
        });

        //그리드뷰 클릭 시 화면 전환
        gridV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    CommunityDTO communityDTO = (CommunityDTO) adapterView.getItemAtPosition(position);

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment fragment = new Community_detail(position);

                    Log.d("포지션", position+"");

                    Bundle bundle = new Bundle();
                    if(word != null) {
                        bundle.putString("address", address);
                        bundle.putString("content", content);
                        bundle.putString("word", w);
                    }
                    bundle.putString("postId", communityDTO.getCommunityId());

                    fragment.setArguments(bundle);
                    transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_right);
                    transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
                }
        });

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch(actionId){
                    case EditorInfo.IME_ACTION_SEARCH:
                        word = search.getText().toString();
                        Log.d("keywordTest", ""+word);
                        searchWord(word); //검색어
                        search.setText("");
                        break;
                    default:return false;
                }

                return false;
            }
        });

        Bundle extra = this.getArguments();
        if (extra != null) {
            String getstr = extra.getString("send");
            Log.d("!!!!", getstr);
            if (getstr.equals("finish")) {
                load();
            }
        }

        return view;
    }

    //검색어
    void searchWord(String text) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        w = text;
        Log.d("test", "함수 실행");

        communityData = new ArrayList<>();

        for(int i = 0; i < dtos.size(); i++){

            address = dtos.get(i).getCommunityAddress();
            content = dtos.get(i).getCommunityContent();

            if(address.contains(w) || content.contains(w) ){
                communityData.add(dtos.get(i));
            }else{
                Log.d("keywordTest", "안맞음");
            }
        }

        if(communityData.isEmpty()){
            if(search.getText().toString().equals("")) {
                ;
            }
            else
                Toast.makeText(this.getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
        }else{
            setListData();
        }
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
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
            dialog.positiveButton(null, "확인", materialDialog -> {
                dialog.dismiss();
                return null;
            });
            dialog.show();
        }
        return ch;

    }

    // 목록바텀시트 데이터 반영
    void setListData() {
        Community_Adapter adapter = new Community_Adapter(context, communityData);
        gridV.setAdapter(adapter);
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

                    Log.d("실행중2", "실행중2");
                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        communityDTO.setCommunityId(d.getId());
                        Log.d("실행중", "실행중");
                        dtos.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }

                    //어탭터 생성 및 setAdapter
                    Community_Adapter adapter = new Community_Adapter(context, dtos);
                    gridV.setAdapter(adapter);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                load();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        FragmentActivity activity = getActivity();
        if(activity!=null){
            ((MainActivity)activity).setBackBtn(0,false);
        }

        btnSearch.performClick();

        Log.d("상태", "상태");
    }

    //시작 시 사용자 정보 가져오기
    @Override
    public void onStart() {
        super.onStart();

        load();

        FirebaseUser currentUser = user.getCurrentUser();
        if(currentUser != null){
            email = currentUser.getEmail();

            //사용자 정보가져오기
            DocumentReference docRef = Firestore.collection("users").document(email);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //사용자 닉네임 가져오기
                    UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);
                    userNickName = userDTO.getUserNickname();
                }
            });
        }
    }

    //시작 시 context 받아오기
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }
}