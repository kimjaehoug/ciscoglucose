package com.study.ciscoglucose;

import static com.google.api.AnnotationsProto.http;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class stateDialog extends Dialog {

    private Button state_Meal, state_Exercise, state_Sleep, state_HypoEvent;
    private FirebaseFirestore db;
    private String username;

    public stateDialog(@NonNull Context context, String contents, String username) {
        super(context);
        setContentView(R.layout.stateDialog);

        this.username = username;
        db = FirebaseFirestore.getInstance();

        // XML의 버튼 ID에 맞게 findViewById 설정
        state_Meal = findViewById(R.id.state_Meal);
        state_Exercise = findViewById(R.id.state_Exercise);
        state_Sleep = findViewById(R.id.state_Sleep);
        state_HypoEvent = findViewById(R.id.state_HypoEvent);

        state_Meal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStateToFirestore("Meal");
                sendPredictRequest("Meal");
                dismiss();
            }
        });

        state_Exercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStateToFirestore("Exercise");
                sendPredictRequest("Exercise");
                dismiss();
            }
        });

        state_Sleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStateToFirestore("Sleep");
                sendPredictRequest("Sleep");
                dismiss();
            }
        });

        state_HypoEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStateToFirestore("HypoEvent");
                sendPredictRequest("HypoEvent");
                dismiss();
            }
        });
    }

    private void saveStateToFirestore(String stateType) {
        // 현재 시간 구하기
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // 저장할 데이터 구성
        HashMap<String, Object> data = new HashMap<>();
        data.put("state", stateType);
        data.put("time", currentTime);

        // Firestore에 저장: users/{username}/state/{auto-doc-id}
        db.collection("users")
                .document(username)
                .collection("state")
                .add(data)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getContext(), stateType + " 저장 완료!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
    private void sendPredictRequest(String stateType) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("state", stateType);
                json.put("timestamp", currentTime);

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.get("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url("http://210.117.143.172:8888/")
                        .post(body)
                        .build();

                // 응답을 받지 않고 요청만 보냄
                client.newCall(request).execute().close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}