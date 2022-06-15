package com.example.GoAutoCamping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class Information_modi_login extends AppCompatActivity {

    TextInputLayout passwdL_loginM;
    TextInputEditText passwd_loginM;
    MaterialButton btnLogin;

    //파이어베이스
    private FirebaseStorage storage;
    private FirebaseFirestore Firestore;
    private String email;

    //로그인 인증
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_modi_login);

        //툴바생성
        Toolbar toolbar = findViewById(R.id.modiTB);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        passwdL_loginM = findViewById(R.id.passwdLayout_modi);
        passwd_loginM = findViewById(R.id.passwdText_modi);
        btnLogin = findViewById(R.id.btnLogin_modi);

        firstLoad();

        //비밀번호 검사
        passwd_loginM.addTextChangedListener(new TextWatcher() {
            @Override// 텍스트 변경전 호출
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override //텍스트 변경시마다 호출
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = passwd_loginM.getText().toString();
                if(text.equals("")){
                    passwdL_loginM.setError("필수 입력입니다.");
                }else{
                    passwdL_loginM.setErrorEnabled(false);
                }
            }
            @Override//텍스트 변경 이후 호출
            public void afterTextChanged(Editable s) {
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){
                    String password = passwd_loginM.getText().toString();
                    signIn(email, password);
                }
            }
        });

    }

    //폼 확인
    private boolean validateForm() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        String password = passwd_loginM.getText().toString();


        //비밀번호
        if (TextUtils.isEmpty(password)) {
            valid = false;
            passwdL_loginM.setError("필수 입력입니다.");
            passwd_loginM.requestFocus();
            imm.showSoftInput(passwd_loginM, InputMethodManager.SHOW_IMPLICIT);
        }

        return valid;
    }

    //로그인
    private void signIn(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Intent intent = new Intent(getApplicationContext(), Information_modi.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            passwdL_loginM.setError("옳지 않은 비밀번호입니다");
                            Toast.makeText(getApplicationContext(), "로그인 실패",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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

    public void firstLoad(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            email = currentUser.getEmail();

        }
    }
}
