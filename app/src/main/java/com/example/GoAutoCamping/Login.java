package com.example.GoAutoCamping;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.kakao.sdk.user.UserApiClient;

public class Login extends AppCompatActivity {

    private  static final String TAG = "DebugTag";

    Button loginbtn, kakaoLoginbtn, findIDbtn, findPWbtn, joinbtn, goHomebtn;
    EditText id, passwd;
    CoordinatorLayout snackbar;

    private FirebaseStorage storage;
    private String imageUrl="";
    private FirebaseFirestore Firestore;

    private long backBtnTime = 0;

    //로그인 인증
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        //파이어베이스
        storage = FirebaseStorage.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loginbtn = findViewById(R.id.login);
        kakaoLoginbtn = findViewById(R.id.kakaoLogin);
        joinbtn = findViewById(R.id.join);
        findIDbtn = findViewById(R.id.findId);
        findPWbtn = findViewById(R.id.findPW);
        goHomebtn = findViewById(R.id.btnGoHome);

        id = findViewById(R.id.idText);
        passwd = findViewById(R.id.passwdText);
        snackbar = findViewById(R.id.snackbar_line);

        //로그인 버튼
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = id.getText().toString();
                String pass = passwd.getText().toString();

                signIn(email, pass);

                id.setText("");
                passwd.setText("");
            }
        });

        //가상키보드 엔터 로그인
        passwd.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(passwd.getWindowToken(), 0);    //hide keyboard

                    loginbtn.performClick();

                    return true;
                }
                return false;
            }
        });

        kakaoLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                session.open(AuthType.KAKAO_LOGIN_ALL, MainActivity.this);
                UserApiClient.getInstance().loginWithKakaoAccount(Login.this, (token, loginError) -> {
                    if (loginError != null) {
                        Log.e(TAG, "로그인 실패", loginError);
                    } else {
                        Log.d(TAG, "로그인 성공");
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);

                        // 사용자 정보 요청
                        UserApiClient.getInstance().me((user, meError) -> {
                            if (meError != null) {
                                Log.e(TAG, "사용자 정보 요청 실패", meError);
                            } else {
                                Log.i(TAG, user.toString());
                            }
                            return null;
                        });
                    }
                    return null;
                });
            }
        });

        //회원가입 창으로 넘어가기
        /*
        뒤로 돌아가기 버튼 만들기. 회원가입 완료시 완료되었다며 현재 창으로 되돌아옴
         */
        joinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent = new Intent(getApplicationContext(), Login_join.class);
                startActivity(intent);

                 */

                Intent intent = new Intent(getApplicationContext(), Login_join_step1.class);
                startActivity(intent);
            }
        });

        //아이디 찾기 창으로 넘어가기
        findIDbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login_findid.class);
                startActivity(intent);
            }
        });

        //비밀번호 찾기 창으로 넘어가기
        findPWbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login_findpw.class);
                startActivity(intent);
            }
        });


        //TODO : 익명 로그인?
        //건너뛰고 홈화면으로 넘어가기
        goHomebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(0 <= gapTime && 2000 >= gapTime) {
            moveTaskToBack(true);
            finish();
            System.exit(0);
        }
        else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }
    }

    //로그인
    private void signIn(String email, String password) {
        Log.d("TAG", "signIn:" + email);

        if (!validateForm()) {
            return;
        }

        //로그인 프로세스 시작 동시에 다이얼로그 띄워주기
        Dialog dialog = new Dialog(Login.this);
        dialog.setContentView(R.layout.loading_progress);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        dialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Snackbar.make(snackbar, "로그인에 실패했습니다.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                            //checkForMultiFactorFailure(task.getException());

                        }

                        if (!task.isSuccessful()) {
                            //mBinding.status.setText(R.string.auth_failed);
                        }

                        //onComplete로 작업이 끝나고 화면 전환이 될때 다이얼로그 꺼주기 - 아마 이부분은 파이어베이스에서만 되는듯?
                        dialog.dismiss();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Snackbar.make(snackbar, "자동 로그인", Snackbar.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }


    //폼 확인
    private boolean validateForm() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        //이메일
        String email = id.getText().toString();
        //비밀번호
        String password = passwd.getText().toString();

        if (TextUtils.isEmpty(email)) {
            valid = false;
            id.requestFocus();
            imm.showSoftInput(id, InputMethodManager.SHOW_IMPLICIT);
        } else if (TextUtils.isEmpty(password)) {
            valid = false;
            passwd.requestFocus();
            imm.showSoftInput(passwd, InputMethodManager.SHOW_IMPLICIT);
        } else {
            //mBinding.fieldPassword.setError(null);
        }

        return valid;
    }


}
