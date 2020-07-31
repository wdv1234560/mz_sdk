package com.mz.mzyd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.mz.segiu.mvp.ui.activity.MzWebActivity;
import com.mz.segiu.mvp.ui.activity.ScanActivity;

//import com.mz.segiu.mvp.ui.activity.MzWebActivity;
//import com.mz.segiu.mvp.ui.activity.ScanActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(v ->
                startActivity(new Intent(this, MzWebActivity.class))
        );
        findViewById(R.id.button2).setOnClickListener(v ->
                startActivity(new Intent(this, ScanActivity.class))
        );
    }
}
