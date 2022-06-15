package com.example.GoAutoCamping;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private View drawerView;

    private boolean backFlag = false, btomFlag = false;
    public ImageView alert; //알람설정
    int pointY = 0;

    private boolean isCreated = false;

    //파이어베이스
    private FirebaseAuth user;
    private FirebaseStorage storage;
    private String imageUrl = "";
    private FirebaseFirestore Firestore;
    private String email;
    private long backBtnTime = 0;

    public static Context mContext;
    Toolbar toolbar;
    //내정보
    TextView name, myPostBtn, myInfoBtn, noticeBtn;
    Button btnLogout, btnMark;
    ImageView profile_image;

    ListView listview;
    String[] itemList = {"내 정보 수정", "내 게시물", "즐겨찾기", "공지사항"};
    //baseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //툴바생성
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        //현재 사용자 정보
        user = FirebaseAuth.getInstance();
        Firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        //바텀네비 - 하단
        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.item_fragment_home);
        Home home = new Home();
        Bundle bundle = new Bundle(); // 파라미터의 숫자는 전달하려는 값의 갯수
        bundle.putInt("pop", 0);
        home.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.main_frame, home).commit();

        /*
        listview = findViewById(R.id.menu_list);
        adapter = new baseAdapter();
        listview.setAdapter(adapter);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = null;
                switch (itemList[i]) {
                    case "내 정보 수정":
                        intent = new Intent(MainActivity.this, Information_modi.class);
                        startActivity(intent);
                        ;
                        break;
                    case "내 게시물":
                        intent = new Intent(MainActivity.this, Information_post.class);
                        startActivity(intent);
                        ;
                        break;
                    case "즐겨찾기":
                        intent = new Intent(MainActivity.this, Information_Mark.class);
                        startActivity(intent);
                        ;
                        break;
                    case "공지사항":
                        intent = new Intent(MainActivity.this, Information_notice.class);
                        startActivity(intent);
                        ;
                        break;
                }
            }
        });*/
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_fragment_home:
                        isCreated = false;
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Home()).commit();
                        break;
                    case R.id.item_fragment_map:
                        isCreated = false;
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Map()).commit();
                        break;
                    case R.id.item_fragment_recommend:
                        isCreated = false;
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Recommend()).commit();
                        break;
                    case R.id.item_fragment_community:
                        isCreated = true;
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Community(), "comm").commit();
                        break;
                    case R.id.item_fragment_campingSupplies:
                        isCreated = false;
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Supplies()).commit();
                        break;
                }
                return true;
            }
        });

        //커스텀 네비게이션
        drawerLayout = findViewById(R.id.main_drawer_layout);
        drawerView = findViewById(R.id.navigation_view);
        ImageView openNavDraw = findViewById(R.id.infoMark);
        openNavDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        drawerLayout.setDrawerListener(listener);

        //내정보 네비게이션 드로어

        btnMark = findViewById(R.id.btnMark);
        myPostBtn = findViewById(R.id.myPostTv);
        myInfoBtn = findViewById(R.id.myInfoTv);
        noticeBtn = findViewById(R.id.noticeTv);
        btnLogout = findViewById(R.id.logoutBtn);
        name = findViewById(R.id.profileName);
        profile_image = findViewById(R.id.profileImg);

        btnMark.setOnClickListener(infoControl);

        myPostBtn.setOnClickListener(infoControl);
        myInfoBtn.setOnClickListener(infoControl);
        noticeBtn.setOnClickListener(infoControl);
        btnLogout.setOnClickListener(infoControl);


    }



    //내정보 온클릭 리스너
    View.OnClickListener infoControl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = null;

            user = FirebaseAuth.getInstance();
            FirebaseUser currentUser = user.getCurrentUser();

            if(currentUser != null){
                if (v.getId() == R.id.btnMark) {
                    intent = new Intent(MainActivity.this, Information_Mark.class);
                    startActivity(intent);
                } else if (v.getId() == R.id.myInfoTv) {
                    intent = new Intent(MainActivity.this, Information_modi_login.class);
                    startActivity(intent);
                } else if (v.getId() == R.id.myPostTv) {
                    intent = new Intent(MainActivity.this, Information_post.class);
                    startActivityForResult(intent, 10);
                } else if (v.getId() == R.id.noticeTv) {
                    intent = new Intent(MainActivity.this, Information_notice.class);
                    startActivity(intent);
                } else if (v.getId() == R.id.logoutBtn) {
                    //인텐트 전환? 로그인 화면?
                    user.signOut();
                    currentUser = user.getCurrentUser();
                    if (currentUser == null) {
                        Log.d("로그아웃됌?", "ㅇㅇ됌");
                    }
                    finish();
                }
            }else{
                if (v.getId() == R.id.btnMark) {
                    MaterialDialog dialog = new MaterialDialog(MainActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "로그인 오류");
                    dialog.message(null, "로그인이 필요한 작업입니다. \n로그인 해주세요.", null);
                    //dialog.icon(null, getResources().getDrawable(R.drawable.ic_baseline_report_24));
                    dialog.positiveButton(null, "확인", materialDialog -> {
                        dialog.dismiss();
                        return null;
                    });
                    dialog.show();
                } else if (v.getId() == R.id.myInfoTv) {
                    MaterialDialog dialog = new MaterialDialog(MainActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "로그인 오류");
                    dialog.message(null, "로그인이 필요한 작업입니다. \n로그인 해주세요.", null);
                    //dialog.icon(null, getResources().getDrawable(R.drawable.ic_baseline_report_24));
                    dialog.positiveButton(null, "확인", materialDialog -> {
                        dialog.dismiss();
                        return null;
                    });
                    dialog.show();
                } else if (v.getId() == R.id.myPostTv) {
                    MaterialDialog dialog = new MaterialDialog(MainActivity.this, MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "로그인 오류");
                    dialog.message(null, "로그인이 필요한 작업입니다. \n로그인 해주세요.", null);
                    //dialog.icon(null, getResources().getDrawable(R.drawable.ic_baseline_report_24));
                    dialog.positiveButton(null, "확인", materialDialog -> {
                        dialog.dismiss();
                        return null;
                    });
                    dialog.show();
                } else if (v.getId() == R.id.noticeTv) {
                    intent = new Intent(MainActivity.this, Information_notice.class);
                    startActivity(intent);
                } else if (v.getId() == R.id.logoutBtn) {
                    finish();
                }
            }
        }


    };

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10){
            if(data.getIntExtra("name", 1) == 1 && isCreated){
                ((Community)getSupportFragmentManager().findFragmentByTag("comm")).load();
            }
        }
    }

    //홈화면 게시된 포스트 클릭시 바텀네비게이션 상태 변경
    public void setBottomNavi(String name) {
        switch (name) {
            case "recommend":
                bottomNavigationView.setSelectedItemId(R.id.item_fragment_recommend);
                break;
            case "supplies":
                bottomNavigationView.setSelectedItemId(R.id.item_fragment_campingSupplies);
                break;
            case "community":
                bottomNavigationView.setSelectedItemId(R.id.item_fragment_community);
                break;
        }
        ;
    }

    public void hideBottomNavi(boolean flag) {
        this.btomFlag = flag;

        if (btomFlag) {
            bottomNavigationView.setVisibility(View.GONE);
            //toolbar.setVisibility(View.GONE);
        } else bottomNavigationView.setVisibility(View.VISIBLE);

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        View view = findViewById(R.id.nav_view);

        pointY = view.getHeight();
    }

    public void setBackBtn(int num, boolean flag) {
        ActionBar tb = getSupportActionBar();
        if (tb != null) {
            if (num == 0) {
                tb.setDisplayHomeAsUpEnabled(false);
            } else if (num == 1 || num == 2) {
                tb.setDisplayHomeAsUpEnabled(true);
            }
            backFlag = flag;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                FragmentManager activity = getSupportFragmentManager();
                activity.popBackStack();


                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    //시작 시 사용자 정보 가져오기
    @Override
    protected void onStart() {
        super.onStart();

        user = FirebaseAuth.getInstance();
        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
            if(!email.equals("")){
                //사용자 정보가져오기
                DocumentReference docRef = Firestore.collection("users").document(email);
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //사용자 이름 가져오기
                        UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);
                        name.setText(userDTO.getUserNickname());
                        btnMark.setText(userDTO.getUserFavorite().size() + "건");

                        //사용자 프로필 사진 가져오기
                        RequestOptions cropOptions = new RequestOptions();
                        String url = userDTO.getUserProfile();
                        Glide.with(getApplicationContext())
                                .load(url)
                                .apply(cropOptions.optionalCircleCrop())
                                .into(profile_image);
                    }
                });
            }



        }
        else{
            btnLogout.setText("로그인 화면으로");
        }
    }

    public void setMarkbtn(){
        user = FirebaseAuth.getInstance();
        FirebaseUser currentUser = user.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();

            //사용자 정보가져오기
            DocumentReference docRef = Firestore.collection("users").document(email);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    UserDTO userDTO = documentSnapshot.toObject(UserDTO.class);
                    btnMark.setText(userDTO.getUserFavorite().size() + "건");

                }
            });
        }
    }

    //뒤로가기 버튼 막기(추가 05.12)
    @Override
    public void onBackPressed() {

        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(0 <= gapTime && 2000 >= gapTime) {

            /*
            android.os.Process.killProcess(android.os.Process.myPid());

            Intent intent = new Intent(this, Loading.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("kill", true);
            startActivity(intent);*/

            moveTaskToBack(true);
            finish();
            System.exit(0);
        }
        else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        backFlag = false;
    }

    /*
    class baseAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return itemList.length;
        }

        @Override
        public Object getItem(int i) {
            return itemList[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.main_navdraw_item,viewGroup,false);

            TextView tv = view.findViewById(R.id.itemText);

            tv.setText(itemList[i]);

            return view;
        }*/
}


