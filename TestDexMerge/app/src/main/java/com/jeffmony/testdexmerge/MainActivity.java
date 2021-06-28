package com.jeffmony.testdexmerge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mJumpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mJumpBtn = findViewById(R.id.jump_btn);

        mJumpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TestDexMergeActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public Resources getResources() {
        return getApplication() != null && getApplication().getResources() != null ? getApplication().getResources() : super.getResources();
    }
}