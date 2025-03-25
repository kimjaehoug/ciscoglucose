package com.study.ciscoglucose;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private Button register;
    private Button cancel;
    private EditText email;
    private EditText password;
    private EditText nickname;
    private FirebaseAuth mFirebaseAuth; // Firebase 인증
    private FirebaseFirestore db; // Firestore 인스턴스
    private EditText sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI 요소 초기화
        register = findViewById(R.id.btn_register);
        cancel = findViewById(R.id.btn_cancel);
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        nickname = findViewById(R.id.input_nickname);
        sensor = findViewById(R.id.input_sensor);

        // 회원가입 버튼 클릭 시
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });

        // 취소 버튼 클릭 시
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 윈도우 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void startRegister() {
        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        String nicknameStr = nickname.getText().toString().trim();
        String sensorStr = sensor.getText().toString().trim();
        if (emailStr.isEmpty() || passwordStr.isEmpty() || nicknameStr.isEmpty()||sensorStr.isEmpty()) {
            Toast.makeText(this, "빈칸을 모두 입력해주세요", Toast.LENGTH_SHORT).show();
        } else {
            // Firebase로 계정 생성
            mFirebaseAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // 회원가입 성공
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            if (user != null) {
                                // Firestore에 사용자 정보 저장
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", emailStr);
                                userData.put("nickname",nicknameStr);
                                userData.put("sensor",sensorStr);
                                db.collection("users").document(user.getUid()).set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(RegisterActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish(); // 현재 화면 종료
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterActivity.this, "Firestore 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // 회원가입 실패
                            Toast.makeText(RegisterActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}