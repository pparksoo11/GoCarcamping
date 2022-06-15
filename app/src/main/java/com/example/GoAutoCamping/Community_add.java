package com.example.GoAutoCamping;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Community_add extends AppCompatActivity {

    int REQUEST_IMAGE_CODE = 1001;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1002;
    String STATE_IMAGE = "StoreImage";

    CoordinatorLayout snackbar;
    ImageView image_address;
    TextView address_NameTv, textTitle;
    MaterialButton btnOk;
    EditText editContent;
    RatingBar starRate;

    private FirebaseStorage storage;
    private String imageUrl="";
    private FirebaseFirestore Firestore;
    private String email;
    private FirebaseAuth user;
    private float starNum;

    Context context;

    String userNickName,userProfile;

    byte[] img;
    String place;

    Bitmap bitmap;

    //커뮤니티 데이터형 어레이리스트 변수
    ArrayList<CommunityDTO> dtos;
    ArrayList<CommunityDTO> dtos2;
    ArrayList<CommunityDTO> communityData;

    ArrayList<String> likeName = new ArrayList<>();
    ArrayList<String> declearName = new ArrayList<>();

    int pos;

    //게시물 수정
    String postId, address, content, id, image, nickName, profile, uploadTime, rec2, home, word;
    Float star;
    int like, pos2;
    ArrayList<String> likeNames;

    String Add, title = "이름";
    Float Lat, Lng;

    //바텀바 숨기기 체크
    boolean checking = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community_add);

        image_address = findViewById(R.id.image_address);
        address_NameTv = findViewById(R.id.addressNameTv);
        btnOk = findViewById(R.id.btnOk);
        editContent = findViewById(R.id.editContent);
        starRate = findViewById(R.id.starRate);
        textTitle = findViewById(R.id.textTitle);
        snackbar = findViewById(R.id.snackbar_line);

        storage = FirebaseStorage.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance();

        //레이팅바 선택된 별 개수 저장
        starRate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float star, boolean b) {
                starNum = star;
            }
        });

        //값 전달 받음
        Intent intent = getIntent();
        String place = intent.getStringExtra("place");
        String update = intent.getStringExtra("update");

        if(update != null) {
            updateStart();
        }

        if(rec2 != null) {
            loadRecommend();
        }
        else if ( home != null) {
            loadHome();
        }

        else if (word != null) {
            loadSearch();
        }

        else {
            //불러오기
            load();
        }

        Toolbar toolbar = findViewById(R.id.consL);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //장소 선택 텍스트 클릭 시 장소 선택 화면으로 연결
        address_NameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), Community_add_placesChoice.class);
                startActivityForResult(intent,10);
            }
        });

        //외부 스토리지 허용 권한 설정
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
            }
        } else {
        }

        //TODO - 이미지뷰 클릭 시 갤러리 연동
        image_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_CODE);
            }
        });

        Log.d("url", imageUrl);

        //TODO - 완료버튼 클릭 시 파이어베이스에 정보 입력.
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //게시물 내용 예외처리
                if(editContent.getText().toString().equals("")) {
                    Snackbar.make(snackbar, "게시물의 내용을 작성해주세요", Snackbar.LENGTH_SHORT).show();
                }

                //게시물 장소 예외처리
                else if(address_NameTv.getText().toString().equals("장소 선택")) {
                    Snackbar.make(snackbar, "게시물의 장소를 선택해주세요", Snackbar.LENGTH_SHORT).show();
                }

                //TODO - 예외가 없을 시 수정 또는 게시
                else {
                    //게시물 수정
                    if (update != null) {
                        update(imageUrl);
                        Toast.makeText(getApplication(), "게시물 수정이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        //게시물 등록
                        //게시물 사진 예외처리
                        if(img == null) {
                            Snackbar.make(snackbar, "게시물의 사진을 선택해주세요", Snackbar.LENGTH_SHORT).show();
                        }
                        uploadImg(imageUrl);
                        Log.d("실행됨", "실행됨");
                        Log.d("###", imageUrl);
                    }
                }
            }
        });

    }

    //갤러리에서 이미지를 받아와 이미지뷰에 띄움.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CODE) {
            try {
                Uri image = data.getData();
                try {
                    imageUrl = getRealPathFromUri(image);

                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                    img = bitmapToByteArray(bitmap); //비트맵의 이미지를 바이트배열에 저장
                    Log.d("###", img.length + "");

                    GradientDrawable drawable = (GradientDrawable) image_address.getContext().getDrawable(R.drawable.community_edge);

                    image_address.setBackground(drawable);
                    image_address.setClipToOutline(true);

                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .into(image_address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        //지역과 이미지바이트배열 전달
        else if (requestCode == 10) {
            if (data != null) {
                Add = data.getStringExtra("Add2");
                Lat = data.getFloatExtra("Lat2", 0.0f);
                Lng = data.getFloatExtra("Lng2", 0.0f);
                title = data.getStringExtra("title");
                Log.d("전달", title);

                //전달받은 값이 있으면 텍스트로 띄워줌
                if (title == null)
                    Log.d("초반 오류입니닥", "초반에는 널포인터임");
                else {
                    address_NameTv.setText(title);
                }

                //전달받은 값이 있으면 텍스트로 띄워줌
                if (img == null)
                    Log.d("초반 오류입니닥", "초반에는 널포인터임");
                else {

                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .into(image_address);
                }
            }
        }
    }

    //절대경로를 구한다.
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

    //게시물 수정
    public void update(String uri) {

        if(imageUrl.equals("")) {
            Firestore = FirebaseFirestore.getInstance();

            CommunityDTO communityDTO = new CommunityDTO();
            communityDTO.setCommunityId(id);
            communityDTO.setCommunityImage(image);
            communityDTO.setCommunityAddress(address_NameTv.getText().toString());
            communityDTO.setCommunityContent(editContent.getText().toString());
            communityDTO.setCommunityUserNickName(nickName);
            communityDTO.setCommunityStar(starNum);
            communityDTO.setCommunityLike(dtos.get(pos2).getCommunityLike());
            communityDTO.setCommunityUserProfile(profile);
            communityDTO.setCommunityLikeUser(dtos.get(pos2).getCommunityLikeUser());
            communityDTO.setCommunityUploadTime(dtos.get(pos2).getCommunityUploadTime());
            communityDTO.setCommunityDeclear(dtos.get(pos2).getCommunityDeclear());
            if(Add == null)
                communityDTO.setCommunityAddress2(dtos.get(pos2).getCommunityAddress2());
            else
                communityDTO.setCommunityAddress2(Add);


            Firestore.collection("communication").document(postId).set(communityDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Intent i = getIntent();
                    i.putExtra("check", 1);
                    if(rec2 != null)
                        i.putExtra("rec2", rec2);

                    setResult(Activity.RESULT_OK, i);
                    checking = true;
                    finish();
                    overridePendingTransition(R.anim.none, R.anim.exit_from_right);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
            //사용자가 이미지를 바꾸었을 때
        }else {
            try {
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();

                Uri file = Uri.fromFile(new File(uri));
                final StorageReference riversRef = storageRef.child("communityImages/" + file.getLastPathSegment());
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
                        if (task.isSuccessful()) {

                            //파이어베이스에 데이터베이스 업로드
                            @SuppressWarnings("VisibleForTests")
                            Uri downloadUrl = task.getResult();

                            Firestore = FirebaseFirestore.getInstance();

                            CommunityDTO communityDTO = new CommunityDTO();
                            communityDTO.setCommunityId(id);
                            communityDTO.setCommunityImage(downloadUrl.toString());
                            communityDTO.setCommunityAddress(address_NameTv.getText().toString());
                            communityDTO.setCommunityContent(editContent.getText().toString());
                            communityDTO.setCommunityUserNickName(nickName);
                            communityDTO.setCommunityStar(starNum);
                            communityDTO.setCommunityLike(dtos.get(pos2).getCommunityLike());
                            communityDTO.setCommunityUserProfile(profile);
                            communityDTO.setCommunityLikeUser(dtos.get(pos2).getCommunityLikeUser());
                            communityDTO.setCommunityUploadTime(dtos.get(pos2).getCommunityUploadTime());
                            communityDTO.setCommunityDeclear(dtos.get(pos2).getCommunityDeclear());
                            if(Add == null)
                                communityDTO.setCommunityAddress2(dtos.get(pos2).getCommunityAddress2());
                            else
                                communityDTO.setCommunityAddress2(Add);

                            //커뮤니케이션 리스트에 글 추가
                            Firestore.collection("communication").document(postId).set(communityDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Intent i = getIntent();
                                    i.putExtra("check", 1);
                                    setResult(Activity.RESULT_OK, i);
                                    checking = true;
                                    finish();
                                    overridePendingTransition(R.anim.none, R.anim.exit_from_right);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

            } catch (Exception e) {


                e.printStackTrace();
            }
        }
    }

    //커뮤니티 데이터 가져오기(검색창에서 연결)
    public void loadSearch(){
        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        communityData = new ArrayList<>();

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
                        communityData.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }
                    //TODO 나중에 이부분 5로 바꿔서 5개보여주기
                    for (int j = 0; j<communityData.size(); j++) {
                        if(communityData.get(j).getCommunityAddress().contains(word) || communityData.get(j).getCommunityContent().contains(word)) {
                            dtos.add(communityData.get(j));
                        }
                    }
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

    //커뮤니티 데이터 가져오기 (홈에서 연결)
    public void loadHome(){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        //커뮤니티의 모든 데이터 가져오기 - onComplete가 안먹어서 Success로 바꿨음 그에 따라 아래 방법도 바뀜
        Firestore.collection("communication").orderBy("communityLike", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        dtos.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }
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

    //커뮤니티 데이터 가져오기
    public void loadRecommend(){

        Firestore = FirebaseFirestore.getInstance();

        //어레이리스트 새로 생성
        dtos = new ArrayList<>();

        dtos2 = new ArrayList<>();

        //커뮤니티의 모든 데이터 가져오기 - onComplete가 안먹어서 Success로 바꿨음 그에 따라 아래 방법도 바뀜
        Firestore.collection("communication").orderBy("communityLike", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        dtos2.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }

                    //전달받은 지역에 따라서 값 지정
                    for (int j = 0; j<dtos2.size(); j++) {
                        if(dtos2.get(j).getCommunityAddress2().contains(rec2)) {
                            dtos.add(dtos2.get(j));
                        }
                    }

                    pos = dtos.size();
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

                    //도큐먼트 리스트 생성
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                    //도큐먼트 리스트에서 도큐먼트 하나씩 가져오기
                    for(DocumentSnapshot d : list){
                        CommunityDTO communityDTO = d.toObject(CommunityDTO.class);
                        dtos.add(communityDTO); //위 생성한 커뮤니티 데이터형 어레이 리스트에 도큐먼트 데이터 추가
                    }
                    pos = dtos.size();
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

    //파이어베이스에 업로드
    private void uploadImg(String uri)
    {
        final DocumentReference documentReferenceUser = Firestore.collection("users").document(email);

        try {
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();

            Uri file = Uri.fromFile(new File(uri));
            final StorageReference riversRef = storageRef.child("communityImages/"+file.getLastPathSegment());
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
                        //파이어베이스에 데이터베이스 업로드
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = task.getResult();

                        CommunityDTO communityDTO = new CommunityDTO();
                        communityDTO.setCommunityId("");
                        communityDTO.setCommunityImage(downloadUrl.toString());
                        communityDTO.setCommunityAddress(address_NameTv.getText().toString());
                        communityDTO.setCommunityContent(editContent.getText().toString());
                        communityDTO.setCommunityUserNickName(userNickName);
                        communityDTO.setCommunityStar(starNum);
                        communityDTO.setCommunityLike(0);
                        communityDTO.setCommunityUserProfile(userProfile);
                        communityDTO.setCommunityUploadTime(com.google.firebase.Timestamp.now());
                        communityDTO.setCommunityLikeUser(likeName);
                        communityDTO.setCommunityDeclear(declearName);
                        communityDTO.setCommunityAddress2(Add);

                        Firestore.collection("communication").add(communityDTO)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {

                                        //유저 글목록에 추가
                                        documentReferenceUser.update("userPosts", FieldValue.arrayUnion(documentReference.getId()));

                                        Intent i = getIntent();
                                        i.putExtra("check", 1);
                                        setResult(Activity.RESULT_OK, i);
                                        finish();
                                        overridePendingTransition(R.anim.none, R.anim.exit_from_right);

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
            e.printStackTrace();
        }
    }

    //비트맵이미지 바이트배열로 전환, 출처 : https://crazykim2.tistory.com/434
    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //툴바 뒤로가기 버튼
                finish();
                overridePendingTransition(R.anim.none, R.anim.exit_from_right);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

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
                    userProfile = userDTO.getUserProfile();
                }
            });
        }
    }

    //시작 시 사용자 정보 가져오기
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void updateStart() {
        //값 전달 받음
        Intent intent = getIntent();
        String update = intent.getStringExtra("update");
        if (update != null) {
            if((intent.getStringExtra("rec2") != null)) {
                rec2 = intent.getStringExtra("rec2");
            }
            else if((intent.getStringExtra("home") != null)) {
                home = intent.getStringExtra("home");
            }
            else if((intent.getStringExtra("word") != null)) {
                word = intent.getStringExtra("word");
            }

            postId = intent.getStringExtra("postId");
            address = intent.getStringExtra("address");
            content = intent.getStringExtra("content");
            id = intent.getStringExtra("id");
            image = intent.getStringExtra("image");
            like = intent.getIntExtra("like", 0);
            star = intent.getFloatExtra("star", 0);
            profile = intent.getStringExtra("profile");
            nickName = intent.getStringExtra("nickName");
            uploadTime = intent.getStringExtra("uploadTime");
            pos2 = intent.getIntExtra("pos", 0);

            Log.d("커뮤니티이름", postId);
        }

        //전달받은 값을 띄워줌
        if(update != null) {
            textTitle.setText("게시물 수정");
            address_NameTv.setText(address);
            GradientDrawable drawable = (GradientDrawable) image_address.getContext().getDrawable(R.drawable.community_edge);

            image_address.setBackground(drawable);
            image_address.setClipToOutline(true);
            Glide.with(this)
                    .load(image)
                    .into(image_address);
            starRate.setRating(star);
            editContent.setText(content);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("imageURI", imageUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageUrl = savedInstanceState.getString("imageURI");
    }
}