package com.example.GoAutoCamping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class Login_join_step2 extends AppCompatActivity {

    TextInputLayout idL_join, passwdL_join, passwdOkL_join;
    TextInputEditText id_join, passwd_join, passwdOk_join;

    Button btnIdCheck;
    MaterialButton btnNextStep2;

    boolean emailCheck = false;
    boolean nicknameCheck = false;
    boolean imageValid = false;
    public String phoneNumber = "";

    ArrayList<String> favoriteList = new ArrayList<>();
    ArrayList<String> postList = new ArrayList<>();

    //파이어베이스
    private FirebaseStorage storage;
    private String imageUrl="";
    private FirebaseFirestore Firestore;

    //로그인 인증
    private FirebaseAuth mAuth;

    //갤러리에서 사진 가져오기
    private int GALLEY_CODE = 10;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_join_step2);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storage = FirebaseStorage.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        id_join = findViewById(R.id.idText_join);
        passwd_join = findViewById(R.id.passwdText_join);
        passwdOk_join = findViewById(R.id.passwdOkText_join);

        idL_join = findViewById(R.id.idLayout_join);
        passwdL_join = findViewById(R.id.passwdLayout_join);
        passwdOkL_join = findViewById(R.id.passwdOkLayout_join);

        btnNextStep2 = findViewById(R.id.btnNextStep2);
        btnIdCheck = findViewById(R.id.btnidcheck);

        //아이디 중복체크
        btnIdCheck.setVisibility(View.INVISIBLE);
        btnIdCheck.setClickable(false);

        btnIdCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){


                                if(document.getId().equals(id_join.getText().toString())){ //아이디가 존재하면 다시입력

                                    Log.d("검사가 됩니까?", "되야하는데");

                                    idL_join.setError("이메일 중복");
                                    idL_join.setErrorEnabled(true);
                                    emailCheck = false;
                                    return;
                                }
                                else{
                                    idL_join.setErrorEnabled(false);
                                    emailCheck = true;
                                }
                            }


                        } else {
                            Log.d("err", "no user");
                        }
                    }
                });
            }
        });

        //아이디 중복체크
        id_join.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {   //텍스트가 바뀔때마다 실행

                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(id_join.getText().toString()).matches()){
                    idL_join.setError("이메일 형식을 확인해주세요.");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                //이메일 형식 확인
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(id_join.getText().toString()).matches()){
                    idL_join.setError("이메일 형식을 확인해주세요.");
                }
                //형식이 맞으면 검사
                else{
                    btnIdCheck.performClick();
                }
            }
        });

        //비밀번호
        passwd_join.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = passwd_join.getText().toString();
                if(text.equals("")){
                    passwdL_join.setError("필수 입력입니다");
                }
                else if(text.length() < 6){
                    passwdL_join.setError("6자 이상이어야 합니다");
                }
                else{
                    passwdL_join.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


        //비밀번호 확인
        passwdOk_join.addTextChangedListener(new TextWatcher() {
            String text = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                text = passwdOk_join.getText().toString();
                if(text.equals("")){
                    passwdOkL_join.setError("필수 입력입니다");
                }else{
                    passwdOkL_join.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String pw = passwd_join.getText().toString();
                if(!text.equals(pw)){
                    passwdOkL_join.setError("비밀번호와 맞지 않습니다");
                }else{
                    passwdOkL_join.setErrorEnabled(false);
                }
            }
        });

        btnNextStep2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validateForm()){
                    String id = id_join.getText().toString();
                    String passwd = passwd_join.getText().toString();

                    Intent intent = new Intent(Login_join_step2.this, Login_join_step3.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.putExtra("id", id);
                    intent.putExtra("passwd", passwd);
                    startActivity(intent);
                    finish();
                }
            }
        });


    }


    //폼 확인
    private boolean validateForm() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        String email = id_join.getText().toString();
        String password = passwd_join.getText().toString();
        String passwdOk = passwdOk_join.getText().toString();

        //이메일
        if (TextUtils.isEmpty(email)) {
            valid = false;
            id_join.requestFocus();
            imm.showSoftInput(id_join, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(idL_join.getError() != null){
            valid = false;
            id_join.requestFocus();
            imm.showSoftInput(id_join, InputMethodManager.SHOW_IMPLICIT);
        }
        //비밀번호
        else if (TextUtils.isEmpty(password)) {
            valid = false;
            passwd_join.requestFocus();
            imm.showSoftInput(passwd_join, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(passwdL_join.getError() != null){
            valid = false;
            passwd_join.requestFocus();
            imm.showSoftInput(passwd_join, InputMethodManager.SHOW_IMPLICIT);
        }
        //비밀번호 확인
        else if(TextUtils.isEmpty(passwdOk)){
            valid = false;
            passwdOk_join.requestFocus();
            imm.showSoftInput(passwdOk_join, InputMethodManager.SHOW_IMPLICIT);
        } else if(passwdOkL_join.getError() != null){
            valid = false;
            passwdOk_join.requestFocus();
            imm.showSoftInput(passwdOk_join, InputMethodManager.SHOW_IMPLICIT);
        }

        return valid;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNum");

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //툴바 뒤로가기 버튼
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
