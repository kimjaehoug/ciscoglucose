package com.study.ciscoglucose;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import androidx.annotation.Nullable;

public class PredictActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;
    private Button btnOpenDialog;
    private LineChart glucoseChart;
    private TextView currentGlu;
    private LineDataSet lineDataSet;
    private LineData lineData;
    private ArrayList<Entry> entries = new ArrayList<>();

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmmss"); // X축 표현용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnOpenDialog = findViewById(R.id.btn_open_dialog);
        glucoseChart = findViewById(R.id.glucose_chart);
        currentGlu = findViewById(R.id.current_glu);
        String userId = mFirebaseAuth.getCurrentUser() != null ?
                mFirebaseAuth.getCurrentUser().getUid() : "anonymous";

        btnOpenDialog.setOnClickListener(view -> {
            stateDialog dialog = new stateDialog(PredictActivity.this, "상태 입력", userId);
            dialog.show();
        });

        setupChart();
        observeGlucose(userId);
    }

    private void setupChart() {
        lineDataSet = new LineDataSet(entries, "혈당 (mg/dL)");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawValues(false);

        lineData = new LineData(lineDataSet);
        glucoseChart.setData(lineData);

        XAxis xAxis = glucoseChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis yAxis = glucoseChart.getAxisLeft();
        yAxis.setAxisMinimum(40f);
        yAxis.setAxisMaximum(300f);
        glucoseChart.getAxisRight().setEnabled(false);
        glucoseChart.getDescription().setEnabled(false);
    }

    private void observeGlucose(String userId) {
        db.collection("users")
                .document(userId)
                .collection("glulog")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Firebase", "Listen failed.", e);
                            return;
                        }

                        if (snapshots == null) return;

                        entries.clear();

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            String timestampStr = dc.getDocument().getString("timestamp");
                            Double glucose = dc.getDocument().getDouble("glucose");

                            if (timestampStr != null && glucose != null) {
                                try {
                                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestampStr);
                                    float xVal = Float.parseFloat(timeFormatter.format(date));
                                    String glucoseStr=glucose.toString().trim();
                                    currentGlu.setText(glucoseStr);
                                    entries.add(new Entry(xVal, glucose.floatValue()));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        // 정렬: 시간 순서대로
                        Collections.sort(entries, Comparator.comparing(Entry::getX));

                        lineDataSet.notifyDataSetChanged();
                        glucoseChart.getData().notifyDataChanged();
                        glucoseChart.notifyDataSetChanged();
                        glucoseChart.invalidate();
                    }
                });
    }
}