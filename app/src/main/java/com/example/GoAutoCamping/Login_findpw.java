package com.example.GoAutoCamping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Login_findpw extends AppCompatActivity{

    CoordinatorLayout snackbar;
    ConstraintLayout phoneLayout, emailLayout;
    TextInputLayout nameL_findpw, idL_findpw, phoneNumL_findpw, codeL_findpw;
    TextInputEditText name_findpw, id_findpw, phoneNum_findpw, code_findpw;
    MaterialButton btnfindpw, btnSendCode;
    SwitchMaterial switchMaterial;
    int mode = 2;   //이메일 - 1
    //전화번호 -2

    boolean phoneVerified = false;
    String phone = "";
    String name = "";
    String code = "";

    String memail = "";
    String mpass = "";

    //파이어베이스
    private FirebaseFirestore Firestore;
    private FirebaseAuth FireAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_findpw);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        phoneLayout = findViewById(R.id.phoneL);
        emailLayout = findViewById(R.id.emailL);

        snackbar = findViewById(R.id.snackbar_line);
        switchMaterial = findViewById(R.id.switchMaterial);

        nameL_findpw = findViewById(R.id.nameLayout_findpw);
        idL_findpw = findViewById(R.id.idLayout_findpw);
        phoneNumL_findpw = findViewById(R.id.phonenumLayout_findpw);
        codeL_findpw = findViewById(R.id.verificationCodeLayout_findpw);
        name_findpw = findViewById(R.id.nameText_findpw);
        id_findpw = findViewById(R.id.idText_findpw);
        phoneNum_findpw = findViewById(R.id.phonenumText_findpw);
        code_findpw = findViewById(R.id.verificationCode_findpw);

        btnfindpw = findViewById(R.id.btnFindPW);
        btnSendCode = findViewById(R.id.btnSendCode_findpw);

        Firestore = FirebaseFirestore.getInstance();
        FireAuth = FirebaseAuth.getInstance();

        switchMaterial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchMaterial.isChecked()){
                    mode = 1;
                    phoneNumL_findpw.setErrorEnabled(false);
                    emailLayout.setVisibility(View.VISIBLE);
                    phoneLayout.setVisibility(View.INVISIBLE);
                    switchMaterial.setText("이메일");
                    btnSendCode.setClickable(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(phoneNumL_findpw.getWindowToken(), 0);

                }
                else{
                    mode = 2;
                    idL_findpw.setErrorEnabled(false);
                    phoneLayout.setVisibility(View.VISIBLE);
                    emailLayout.setVisibility(View.INVISIBLE);
                    switchMaterial.setText("전화번호");
                    btnSendCode.setClickable(true);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(idL_findpw.getWindowToken(), 0);

                }
            }
        });


        //아이디 빈자리 체크
        id_findpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {   //텍스트가 바뀔때마다 실행
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(id_findpw.getText().toString()).matches() && mode == 1){
                    idL_findpw.setError("이메일 형식을 확인해주세요.");
                }
                else{
                    idL_findpw.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //이메일 형식 확인
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(id_findpw.getText().toString()).matches() && mode == 1){
                    idL_findpw.setError("이메일 형식을 확인해주세요.");
                }
                else{
                    idL_findpw.setErrorEnabled(false);
                }
            }
        });

        //전화번호 빈자리 체크
        phoneNum_findpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = phoneNum_findpw.getText().toString();
                if(text.equals("")){
                    phoneNumL_findpw.setError("필수 입력입니다");
                }else{
                    phoneNumL_findpw.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        code_findpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = code_findpw.getText().toString();
                if(text.equals("")){
                    codeL_findpw.setError("필수 입력입니다");
                }else{
                    codeL_findpw.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //이름
        name_findpw.addTextChangedListener(new TextWatcher() {
            @Override //텍스트 변경전 호출
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override //텍스트 변경시마다 호출
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = name_findpw.getText().toString();
                if(text.equals("")){
                    nameL_findpw.setError("필수 입력입니다");
                }else{
                    nameL_findpw.setErrorEnabled(false);
                }
            }

            @Override //텍스트 변경 이후 호출
            public void afterTextChanged(Editable s) { }
        });

        //인증번호 보내기
        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm2()){
                    name = name_findpw.getText().toString();
                    phone = phoneNum_findpw.getText().toString();
                    checkUserPhone();
                }
            }
        });

        btnfindpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mode == 1){
                    if(validateForm1()){
                        String name = name_findpw.getText().toString();
                        String email = id_findpw.getText().toString();
                        checkUser(name, email);
                    }
                }
                else if(mode == 2){
                    Log.d("실행 됌>", "ㅇㅇ");
                    if(validateForm2()){
                        Log.d("실행 됌>", "ㅇㅇ");
                        code = code_findpw.getText().toString();
                        if(!TextUtils.isEmpty(code)){
                            Log.d("실행 됌>", "ㅇㅇ");


                            verifyPhoneNumberWithCode(mVerificationId, code);
                        }
                    }
                }

            }
        });


        //휴대폰 인증 번호 전송 콜백
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d("TAG", "onVerificationCompleted:" + credential);
                Log.d("인증 성공", "실험용입니다.");

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                } else if (e instanceof FirebaseTooManyRequestsException) {

                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d("TAG", "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;
            }

        };

    }



    //폼 확인
    private boolean validateForm1() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        //이름
        String name = name_findpw.getText().toString();
        //이메일
        String email = id_findpw.getText().toString();

        if(TextUtils.isEmpty(name)){
            valid = false;
            name_findpw.requestFocus();
            imm.showSoftInput(name_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else if (TextUtils.isEmpty(email)) {
            valid = false;
            id_findpw.requestFocus();
            imm.showSoftInput(id_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(id_findpw.getError() != null){
            valid = false;
            id_findpw.requestFocus();
            imm.showSoftInput(id_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else{
            //mBinding.fieldPassword.setError(null);
        }

        return valid;
    }

    //폼 확인
    private boolean validateForm2() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        //이름
        String name = name_findpw.getText().toString();
        //전화번호
        String phone = phoneNum_findpw.getText().toString();

        if(TextUtils.isEmpty(name)){
            valid = false;
            name_findpw.requestFocus();
            imm.showSoftInput(name_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else if (TextUtils.isEmpty(phone)) {
            valid = false;
            phoneNum_findpw.requestFocus();
            imm.showSoftInput(phoneNum_findpw, InputMethodManager.SHOW_IMPLICIT);
        }
        else{
            //mBinding.fieldPassword.setError(null);
        }

        return valid;
    }



    //회원 정보 확인 - 이메일
    public void checkUser(String name, String email){
        Firestore.collection("users").document(email).get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if(documentSnapshot.get("userName") != null){
                        if(!documentSnapshot.get("userName").toString().equals(name)){
                            Snackbar.make(snackbar, "회원 정보가 존재하지 않습니다.", Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            sendPWmail(email);
                            Snackbar.make(snackbar, "메일이 전송되었습니다!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Snackbar.make(snackbar, "회원 정보가 존재하지 않습니다.", Snackbar.LENGTH_SHORT).show();
                    }

                }
                else{
                    Snackbar.make(snackbar, "회원 정보가 존재하지 않습니다.", Snackbar.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(snackbar, "회원 정보가 존재하지 않습니다.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void checkUserPhone(){



        Firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    boolean check = false;
                    List<DocumentSnapshot> dd = task.getResult().getDocuments();
                    for(int i = 0; i < dd.size(); i++){
                        if(dd.get(i).get("userName").toString().equals(name) && dd.get(i).get("userPhone").toString().equals(phone)){

                            memail = dd.get(i).getId();
                            mpass = dd.get(i).get("userPasswd").toString();

                            Snackbar.make(snackbar, "인증번호가 전송되었습니다!", Snackbar.LENGTH_SHORT).show();
                            startPhoneNumberVerification(phone);
                            codeL_findpw.setVisibility(View.VISIBLE);
                            check = true;
                            return;
                        }
                    }
                    if(!check)
                        Snackbar.make(snackbar, "회원 정보가 존재하지 않습니다.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


    }

    //이메일 보내기
    public void sendPWmail(String emailAddress)
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("이메일 보내기", "Email sent.");
                        }
                    }
                });

    }


    //인증번호 전송하기
    private void startPhoneNumberVerification(String phoneNumber) {
        String phonenum = "+82"+phoneNumber;
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FireAuth)
                        .setPhoneNumber(phonenum)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);   //문자보내기

        Snackbar.make(snackbar, "인증번호가 전송되었습니다", Snackbar.LENGTH_SHORT).show();

    }

    //인증번호 재전송하기
    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FireAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        Snackbar.make(snackbar, "인증번호가 재전송되었습니다", Snackbar.LENGTH_SHORT).show();
    }

    //인증번호로 로그인작업하기
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    //인증번호로 로그인 - 인증 번호 확인 절차
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FireAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d("TAG", "signInWithCredential:success");
                            phoneVerified = true;
                            checkPhoneVerification(phoneVerified);

                        } else {

                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Snackbar.make(snackbar, "잘못된 인증번호입니다", Snackbar.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            }
                        }
                    }
                });
    }

    private void checkPhoneVerification(boolean phoneVerified){
        if(phoneVerified) {
            //계정삭제
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("계정 삭제해야함", "전번 계정 삭제");
                            }
                        }
                    });

            //화면 전환시켜주기 - 비밀번호 다시 작성
            Intent intent = new Intent(this, Login_findpw_resetpw.class);
            intent.putExtra("email", memail);
            intent.putExtra("passwd", mpass);
            startActivity(intent);
            finish();

        }
        else{
            //올바르지 않다고 표기
        }
    }

}
