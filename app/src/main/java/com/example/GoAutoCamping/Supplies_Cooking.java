package com.example.GoAutoCamping;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class Supplies_Cooking extends Fragment {

    Context context;
    GridView gridView;

    //파이어베이스
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;

    ArrayList<SuppliesDTO> dtos;

    Button btnSearch;
    EditText search;
    String word;
    ArrayList<SuppliesDTO> suppliesData ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.supplies_cooking, container, false);

        gridView = view.findViewById(R.id.gridView);

        search = view.findViewById(R.id.searchView);
        btnSearch = view.findViewById(R.id.btnFind_supCook);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SuppliesDTO suppliesDTO = (SuppliesDTO) parent.getItemAtPosition(position);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                Fragment fragment = new Supplies_Detail();

                Bundle bundle = new Bundle();
                bundle.putString("supplyKind", "category_cooking");
                bundle.putString("postId", suppliesDTO.getPost_id());

                fragment.setArguments(bundle);
                transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_from_right);
                transaction.replace(R.id.main_frame, fragment).addToBackStack(null).commit();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                word = search.getText().toString();
                Log.d("keywordTest", ""+word);
                searchWord(word); //검색어
                search.setText("");
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

        return view;
    }

    //용품 데이터 가져오기
    public void load(){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        //용품 데이터
        Firestore.collection("supplies").document("category_cooking").collection("posts")
                .orderBy("post_like", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        SuppliesDTO suppliesDTO = d.toObject(SuppliesDTO.class);
                        suppliesDTO.setPost_id(d.getId());
                        dtos.add(suppliesDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }
                    //어탭터 생성 및 setAdapter
                    Supplies_adapter adapter = new Supplies_adapter(context, 3, dtos);
                    gridView.setAdapter(adapter);
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

    //검색어
    void searchWord(String text) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        String w = text;
        Log.d("test", "함수 실행");

        suppliesData = new ArrayList<>();

        String titleName;
        String description;
        for(int i = 0; i < dtos.size(); i++){

            titleName = dtos.get(i).getPost_name();
            description = dtos.get(i).getPost_memo();

            if(titleName.contains(w) || description.contains(w)){
                suppliesData.add(dtos.get(i));
            }else{
                Log.d("keywordTest", "안맞음");
            }
        }

        if(suppliesData.isEmpty()){
            Toast.makeText(this.getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
        }else{
            setListData();
        }
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
    }

    // 목록바텀시트 데이터 반영
    void setListData() {
        Supplies_adapter adapter = new Supplies_adapter(context, 1, suppliesData);
        gridView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        load();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }
}