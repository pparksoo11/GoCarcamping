package com.example.GoAutoCamping;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Login_findid extends AppCompatActivity {

    CoordinatorLayout snackbar;
    TextInputLayout nameL_findid, phoneNumL_findid, codeL_findid;
    TextInputEditText name_findid, phoneNum_findid, code_findid;

    MaterialButton btnSendCode, btnfindid;
    Button btnResendCode;
    boolean phoneVerified = false;
    String phone = "";
    String name = "";
    String code = "";

    //파이어베이스
    private FirebaseFirestore Firestore;
    private FirebaseAuth FireAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_findid);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FireAuth = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();

        snackbar = findViewById(R.id.snackbar_line);
        nameL_findid = findViewById(R.id.nameLayout_findid);
        phoneNumL_findid = findViewById(R.id.phonenumLayout_findid);
        codeL_findid = findViewById(R.id.verificationCodeLayout_findid);
        name_findid = findViewById(R.id.nameText_findid);
        phoneNum_findid = findViewById(R.id.phonenumText_findid);
        code_findid = findViewById(R.id.verificationCode_findid);

        btnSendCode = findViewById(R.id.btnSendCode_findid);
        btnfindid = findViewById(R.id.btnFindid_findid);
        btnResendCode = findViewById(R.id.btnResendCode);

        //이름
        name_findid.addTextChangedListener(new TextWatcher() {
            @Override //텍스트 변경전 호출
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override //텍스트 변경시마다 호출
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = name_findid.getText().toString();
                if(text.equals("")){
                    nameL_findid.setError("필수 입력입니다");
                }else{
                    nameL_findid.setErrorEnabled(false);
                }
            }

            @Override //텍스트 변경 이후 호출
            public void afterTextChanged(Editable s) { }
        });

        //전화번호
        phoneNum_findid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = phoneNum_findid.getText().toString();
                if(text.equals("")){
                    phoneNumL_findid.setError("필수 입력입니다");
                }else{
                    phoneNumL_findid.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //인증번호
        code_findid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = code_findid.getText().toString();
                if(text.equals("")){
                    codeL_findid.setError("필수 입력입니다");
                }else{
                    codeL_findid.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){
                    name = name_findid.getText().toString();
                    phone = phoneNum_findid.getText().toString();
                    startPhoneNumberVerification(phone);
                    codeL_findid.setVisibility(View.VISIBLE);
                }
            }
        });

        btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){
                    name = name_findid.getText().toString();
                    phone = phoneNum_findid.getText().toString();
                    resendVerificationCode(phone, mResendToken);
                }
            }
        });

        btnfindid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){

                    code = code_findid.getText().toString();

                    verifyPhoneNumberWithCode(mVerificationId, code);
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
    private boolean validateForm() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        //이름
        String name = name_findid.getText().toString();
        //전화번호
        String phonenum = phoneNum_findid.getText().toString();
        //코드
        String code = code_findid.getText().toString();

        if(TextUtils.isEmpty(name)){
            valid = false;
            name_findid.requestFocus();
            imm.showSoftInput(name_findid, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(TextUtils.isEmpty(phonenum)){
            valid = false;
            phoneNum_findid.requestFocus();
            imm.showSoftInput(phoneNum_findid, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(TextUtils.isEmpty(code)){
            code_findid.requestFocus();
            imm.showSoftInput(code_findid, InputMethodManager.SHOW_IMPLICIT);
        }
        else{
            //mBinding.fieldPassword.setError(null);
        }

        return valid;
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
        if(phoneVerified){
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

            //아이디 정보 찾아오기
            Firestore.collection("users").whereEqualTo("userPhone", phone).whereEqualTo("userName", name)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                String email = "";

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("이메일", document.getId() + " => " + document.getData());
                                    email = document.getId();
                                }

                                //아이디 정보 띄워주기
                                MaterialDialog dialog = new MaterialDialog(Login_findid.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                                dialog.title(null, "아이디 확인");
                                dialog.message(null, "회원님의 아이디는 " + email + "입니다", null);
                                dialog.positiveButton(null, "네", materialDialog -> {
                                    dialog.dismiss();
                                    return null;
                                });
                                dialog.show();
                            }
                            else {
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("왜 안됌", "되는건가?");
                }
            });


        }
        else{
            //올바르지 않다고 표기
        }
    }
}
