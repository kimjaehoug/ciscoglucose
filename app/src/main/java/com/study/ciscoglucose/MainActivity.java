package com.study.ciscoglucose;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Button login;           // 로그인 버튼
    private TextView email;         // 이메일 입력 필드
    private TextView password;      // 비밀번호 입력 필드
    private Handler handler = new Handler(); // 지연 실행을 위한 Handler
    private FirebaseAuth mFirebaseAuth;     // Firebase 인증 객체
    private FirebaseFirestore db;           // Firestore 데이터베이스 객체
    private Button register;        // 회원가입 버튼
    private String emailStr = "";
    private String passwordStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 엣지-투-엣지 디스플레이 활성화
        setContentView(R.layout.activity_main); // 레이아웃 설정
        FirebaseApp.initializeApp(this); // Firebase 초기화

        // UI 요소 초기화
        login = findViewById(R.id.btn_login);
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        register = findViewById(R.id.btn_signup);

        // 초기 로그인 버튼 색상 설정
        login.setBackgroundColor(Color.GRAY);

        // Firebase 객체 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 회원가입 버튼 클릭 리스너: RegisterActivity로 이동
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 비밀번호 입력 필드에 TextWatcher 추가: 입력 변화 감지
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (emailStr.length() < 1 || passwordStr.length() < 1) {
                    login.setBackgroundColor(Color.GRAY);
                } else {
                    login.setBackgroundColor(getResources().getColor(R.color.primary_color));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailStr = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (emailStr.length() < 1 || passwordStr.length() < 1) {
                    login.setBackgroundColor(Color.GRAY);
                } else {
                    login.setBackgroundColor(getResources().getColor(R.color.primary_color));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordStr = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 로그인 버튼 클릭 리스너: 로그인 시작
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login.setBackgroundColor(Color.GRAY);
                startLogin();
            }
        });

        // 윈도우 인셋 처리: 다양한 화면 크기 및 노치 대응
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 로그인 버튼 색상 업데이트 메서드
    private void updateLoginButtonColor() {
        if (emailStr.length() < 1 || passwordStr.length() < 1) {
            login.setBackgroundColor(Color.GRAY);
        } else {
            login.setBackgroundColor(getResources().getColor(R.color.primary_color));
        }
    }

    // 로그인 처리 메서드
    private void startLogin() {
        if (emailStr.length() > 0 && passwordStr.length() > 0) {
            mFirebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // 로그인 성공 시
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(MainActivity.this, MainPage.class);
                                        startActivity(intent);
                                    }
                                }, 3000); // 3초 후 MainPage로 이동
                            } else {
                                // 로그인 실패 시
                                login.setBackgroundColor(getResources().getColor(R.color.primary_color));
                                Toast.makeText(MainActivity.this, "이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            login.setBackgroundColor(getResources().getColor(R.color.primary_color));
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
        }
    }
}