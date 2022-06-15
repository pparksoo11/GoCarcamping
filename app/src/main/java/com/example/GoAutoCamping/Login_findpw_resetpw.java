package com.example.GoAutoCamping;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class Login_findpw_resetpw extends AppCompatActivity {

    TextInputLayout passwdL_findpw_reset, passwdOkL_findpw_reset;
    TextInputEditText passwd_findpw_reset, passwdOk_findpw_reset;

    MaterialButton btnReset;

    private FirebaseFirestore Firestore;
    private FirebaseAuth user;
    private String email;
    private String passwd;

    UserDTO exUserDTO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_findpw_resetpw);

        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();

        passwdL_findpw_reset = findViewById(R.id.passwdLayout_findpw_reset);
        passwdOkL_findpw_reset = findViewById(R.id.passwdOkLayout_findpw_reset);

        passwd_findpw_reset = findViewById(R.id.passwdText_findpw_reset);
        passwdOk_findpw_reset = findViewById(R.id.passwdOkText_findpw_reset);

        btnReset = findViewById(R.id.btnFindPW_reset);

        //비밀번호
        passwd_findpw_reset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = passwd_findpw_reset.getText().toString();
                if(text.equals("")){
                    passwdL_findpw_reset.setError("필수 입력입니다");
                }
                else if(text.length() < 6){
                    passwdL_findpw_reset.setError("6자 이상 입력해주세요");
                }
                else{
                    passwdL_findpw_reset.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = passwd_findpw_reset.getText().toString();
                if(text.equals("")){
                    passwdL_findpw_reset.setError("필수 입력입니다");
                }
                else if(text.length() < 6){
                    passwdL_findpw_reset.setError("6자 이상 입력해주세요");
                }
                else{
                    passwdL_findpw_reset.setErrorEnabled(false);
                }
            }
        });

        //비밀번호 확인
        passwdOk_findpw_reset.addTextChangedListener(new TextWatcher() {
            String text = null;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                text = passwdOk_findpw_reset.getText().toString();
                if(text.equals("")){
                    passwdOkL_findpw_reset.setError("필수 입력입니다.");
                }else{
                    passwdOkL_findpw_reset.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String pw = passwd_findpw_reset.getText().toString();
                if(!text.equals(pw)){
                    passwdOkL_findpw_reset.setError("비밀번호와 맞지 않습니다.");
                }else{
                    passwdOkL_findpw_reset.setErrorEnabled(false);
                }
            }
        });


        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){
                    String password = passwd_findpw_reset.getText().toString();
                    updateUser(password);
                }
            }
        });
    }

    //폼 확인
    private boolean validateForm() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        String password = passwd_findpw_reset.getText().toString();
        String passwdOk = passwdOk_findpw_reset.getText().toString();

        //비밀번호
        if (TextUtils.isEmpty(password)) {
            valid = false;
            passwd_findpw_reset.requestFocus();
            imm.showSoftInput(passwd_findpw_reset, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(passwdL_findpw_reset.getError() != null){
            valid = false;
            passwd_findpw_reset.requestFocus();
            imm.showSoftInput(passwd_findpw_reset, InputMethodManager.SHOW_IMPLICIT);
        }
        //비밀번호 확인
        else if(TextUtils.isEmpty(passwdOk)){
            valid = false;
            passwdOk_findpw_reset.requestFocus();
            imm.showSoftInput(passwdOk_findpw_reset, InputMethodManager.SHOW_IMPLICIT);
        } else if(passwdOkL_findpw_reset.getError() != null){
            valid = false;
            passwdOk_findpw_reset.requestFocus();
            imm.showSoftInput(passwdOk_findpw_reset, InputMethodManager.SHOW_IMPLICIT);
        }

        return valid;
    }

    //사용자 정보 업데이트
    private void updateUser(String passwd){
        //로그인 프로세스 시작 동시에 다이얼로그 띄워주기
        Dialog dialog = new Dialog(Login_findpw_resetpw.this);
        dialog.setContentView(R.layout.loading_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView tv = dialog.findViewById(R.id.loading_text);
        tv.setText("변경 중");
        dialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(exUserDTO.getUserId());
        userDTO.setUserName(exUserDTO.getUserName());
        userDTO.setUserNickname(exUserDTO.getUserNickname());
        userDTO.setUserProfile(exUserDTO.getUserProfile());
        userDTO.setUserBirth(exUserDTO.getUserBirth());
        userDTO.setUserPhone(exUserDTO.getUserPhone());
        userDTO.setUserPosts(exUserDTO.getUserPosts());
        userDTO.setUserFavorite(exUserDTO.getUserFavorite());
        userDTO.setUserPasswd(passwd);

        try{
            //비밀번호 업데이트
            user.updatePassword(passwd)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("비밀번호 바꿈", "User password updated.");

                                //사용자 정보 업데이트
                                Firestore.collection("users").document(email).set(userDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                            dialog.dismiss();
                        }
                    });
        }
        catch (Exception e){
            Log.d("회원 정보 수정", "수정 오류");
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String iemail = intent.getStringExtra("email");
        String ipasswd = intent.getStringExtra("passwd");

        //로그인
        signIn(iemail, ipasswd);
        email = iemail;


    }

    //로그인
    private void signIn(String email, String password) {
        Log.d("TAG", "signIn:" + email);

        user.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d("TAG", "signInWithEmail:success");

                            //사용자 정보 가져오기
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                            if (currentUser != null) {
                                String email = currentUser.getEmail();
                                if(!email.equals("")){
                                    //사용자 정보가져오기
                                    DocumentReference docRef = Firestore.collection("users").document(email);
                                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            //사용자 이름 가져오기
                                            exUserDTO = documentSnapshot.toObject(UserDTO.class);
                                        }
                                    });
                                }
                            }

                        } else {

                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
