package com.example.GoAutoCamping;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Login_join_step3 extends AppCompatActivity implements Information_modi_bottomsheet.BottomSheetListener {

    int REQUEST_IMAGE_CODE = 1001;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1002;

    TextInputLayout nameL_join, nickNameL_join, birthdayL_join;
    TextInputEditText name_join, nickName_join, birthday_join;

    ImageView profile_join;

    Button btnNickCheck;
    MaterialButton btnjoin;

    boolean nicknameCheck = false;
    boolean imageValid = false;
    public String phoneNumber = "";
    public String id = "";
    public String passwd = "";

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
        setContentView(R.layout.login_join_step3);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storage = FirebaseStorage.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        name_join = findViewById(R.id.nameText_join);
        nickName_join = findViewById(R.id.nicknameText_join);
        birthday_join = findViewById(R.id.birthdayText_join);

        nameL_join = findViewById(R.id.nameLayout_join);
        nickNameL_join = findViewById(R.id.nickNameLayout_join);
        birthdayL_join = findViewById(R.id.birthdayLayout_join);

        profile_join = findViewById(R.id.profileImg_join);

        btnNickCheck = findViewById(R.id.btnnicknamecheck);

        //프로필 기본 이미지 설정
        RequestOptions cropOptions = new RequestOptions();
        Glide.with(getApplicationContext())
                .load(R.drawable.profile)
                .apply(cropOptions.optionalCircleCrop())
                .into(profile_join);

        //외부 스토리지 허용 권한 설정
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
            }
        } else {
        }

        //프로필이미지
        profile_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_IMAGE_CODE);
            }
        });

        //닉네임 중복 체크
        btnNickCheck.setVisibility(View.INVISIBLE);
        btnNickCheck.setClickable(false);

        btnNickCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Firestore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){

                                if(document.get("userNickname").toString().equals(nickName_join.getText().toString())){ //아이디가 존재하면 다시입력

                                    Log.d("검사가 됩니까?", "되야하는데");

                                    nickNameL_join.setError("닉네임 중복");
                                    nickNameL_join.setErrorEnabled(true);
                                    nicknameCheck = false;
                                    return;
                                }
                                else{
                                    nickNameL_join.setErrorEnabled(false);
                                    nicknameCheck = true;
                                }
                            }


                        } else {
                            Log.d("err", "no user");
                        }
                    }
                });
            }
        });

        //이름
        name_join.addTextChangedListener(new TextWatcher() {
            @Override //텍스트 변경전 호출
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override //텍스트 변경시마다 호출
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = name_join.getText().toString();
                if(text.equals("")){
                    nameL_join.setError("");
                }else{
                    nameL_join.setErrorEnabled(false);
                }
            }

            @Override //텍스트 변경 이후 호출
            public void afterTextChanged(Editable s) {
                String text = name_join.getText().toString();
                if(text.equals("")){
                    nameL_join.setError("");
                }else{
                    nameL_join.setErrorEnabled(false);
                }
            }
        });

        //닉네임
        nickName_join.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                btnNickCheck.performClick();
            }
        });

        //가상 키보드 비활성화
        birthday_join.setClickable(false);
        birthday_join.setFocusable(false);

        //가상키보드 올라오는거 막기
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(birthdayL_join.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        //온클릭 리스너로 생년월일 bottomSheet
        birthdayL_join.setOnClickListener(birthBottom);
        birthday_join.setOnClickListener(birthBottom);

        //가입하기 버튼
        btnjoin = findViewById(R.id.btnJoin);

        btnjoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){
                    String birth = birthday_join.getText().toString();
                    if(TextUtils.isEmpty(birth)){
                        birthday_join.performClick();
                    } else {

                        createAccount(id, passwd);
                        uploadImg(imageUrl);
                        signIn(id, passwd);
                    }
                }
            }
        });


    }

    //생년월일 bottomSheet 올라오는거
    View.OnClickListener birthBottom = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Information_modi_bottomsheet bsd = new Information_modi_bottomsheet();
            bsd.show(getSupportFragmentManager(),"birth_bottom_sheet");
        }
    };

    //birthBottomSheet 에서 선택된 생년월일 값 받아서 setText
    @Override
    public void setDate(int year, int month, int date) {
        birthday_join.setText(year + "년 "+ month + "월 " + date +"일");
    }


    //사진 고른 후 돌아오는 코드
    //로컬 파일에서 업로드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == REQUEST_IMAGE_CODE)
        {
            try{
                Uri image = data.getData();
                try{
                    imageValid = true;
                    imageUrl = getRealPathFromUri(image);

                    RequestOptions cropOptions = new RequestOptions();
                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .apply(cropOptions.optionalCircleCrop())
                            .into(profile_join);


                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            }catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //사진 절대경로를 구한다.
    private String getRealPathFromUri(Uri uri)
    {
        String[] proj=  {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this,uri,proj,null,null,null);
        Cursor cursor = cursorLoader.loadInBackground();

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String url = cursor.getString(columnIndex);
        cursor.close();
        return  url;
    }

    //회원가입
    private void createAccount(String email, String password) {
        Log.d("TAG", "createAccount:" + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    //storage에 사진 올리고, 데이터베이스에 회원정보 저장하기
    private void uploadImg(String uri)
    {
        if(!imageValid){
            uploadwithoutimage();
        }
        else{
            try {
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();

                Uri file = Uri.fromFile(new File(uri));
                final StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
                UploadTask uploadTask = riversRef.putFile(file);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return riversRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(Login_join_step3.this, "업로드 성공", Toast.LENGTH_SHORT).show();

                            //파이어베이스에 데이터베이스 업로드
                            @SuppressWarnings("VisibleForTests")
                            Uri downloadUrl = task.getResult();

                            UserDTO userDTO = new UserDTO();
                            userDTO.setUserProfile(downloadUrl.toString());
                            userDTO.setUserPosts(postList);
                            userDTO.setUserName(name_join.getText().toString());
                            userDTO.setUserNickname(nickName_join.getText().toString());
                            userDTO.setUserId(id);
                            userDTO.setUserPhone(phoneNumber);
                            userDTO.setUserPasswd(passwd);
                            userDTO.setUserBirth(birthday_join.getText().toString());
                            userDTO.setUserFavorite(favoriteList);

                            String email = id;

                            Firestore.collection("users").document(email).set(userDTO)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("성공", "성공성공");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("실패함", "Error adding document", e);
                                        }
                                    });

                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

            }catch (NullPointerException e)
            {
                Toast.makeText(Login_join_step3.this, "이미지 선택 안함", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadwithoutimage(){

        //파이어베이스에 데이터베이스 업로드

        String basicprofile = "https://firebasestorage.googleapis.com/v0/b/goautocamping-549ca.appspot.com/o/" +
                "images%2FBasicProfile.png?alt=media&token=e78dd651-5f1e-4951-9112-58b4892f574b";

        UserDTO userDTO = new UserDTO();
        userDTO.setUserProfile(basicprofile);
        userDTO.setUserName(name_join.getText().toString());
        userDTO.setUserNickname(nickName_join.getText().toString());
        userDTO.setUserId(id);
        userDTO.setUserPhone(phoneNumber);
        userDTO.setUserPasswd(passwd);
        userDTO.setUserBirth(birthday_join.getText().toString());
        userDTO.setUserFavorite(Arrays.asList());

        String email = id;

        //image 라는 테이블에 json 형태로 담긴다.
        //database.getReference().child("Profile").setValue(imageDTO);
        //  .push()  :  데이터가 쌓인다.
        Firestore.collection("users").document(email).set(userDTO)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("성공", "성공성공");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("실패함", "Error adding document", e);
                    }
                });

    }

    //로그인
    private void signIn(String email, String password) {
        Log.d("TAG", "signIn:" + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d("TAG", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {

                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    //폼 확인
    private boolean validateForm() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean valid = true;

        String name = name_join.getText().toString();
        String nickname = nickName_join.getText().toString();

        //이름
        if(TextUtils.isEmpty(name)){
            valid = false;
            name_join.requestFocus();
            imm.showSoftInput(name_join, InputMethodManager.SHOW_IMPLICIT);

        }
        else if(nameL_join.getError() != null){
            valid = false;
            name_join.requestFocus();
            imm.showSoftInput(name_join, InputMethodManager.SHOW_IMPLICIT);
        }
        //닉네임
        else if(TextUtils.isEmpty(nickname)){
            valid = false;
            nickName_join.requestFocus();
            imm.showSoftInput(nickName_join, InputMethodManager.SHOW_IMPLICIT);
        }
        else if(nickNameL_join.getError() != null){
            valid = false;
            nickName_join.requestFocus();
            imm.showSoftInput(nickName_join, InputMethodManager.SHOW_IMPLICIT);
        }

        return valid;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
        id = intent.getStringExtra("id");
        passwd = intent.getStringExtra("passwd");


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
