package com.example.arcoreapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button sceneformButton, imageButton, cloudButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sceneformButton = (Button) findViewById(R.id.sceneformButton);
        imageButton = (Button) findViewById(R.id.ImageButton);
        cloudButton = (Button) findViewById(R.id.cloudButton);
        sceneformButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SceneformActivity.class));
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AugmentedImagesActivity.class));
            }
        });

        cloudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CloudAnchorActivity.class));
            }
        });
    }
}
