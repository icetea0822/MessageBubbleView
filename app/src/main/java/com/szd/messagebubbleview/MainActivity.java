package com.szd.messagebubbleview;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.szd.messagebubble.MessageBubbleView;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    MessageBubbleView bezierView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bezierView = (MessageBubbleView) findViewById(R.id.bezierView);
        bezierView.setNumber("99+");

        bezierView.setOnActionListener(new MessageBubbleView.ActionListener() {
            @Override
            public void onDrag() {
                Log.d(TAG, "onDrag: ");
            }

            @Override
            public void onDisappear() {
                Log.d(TAG, "onDisappear: ");
            }

            @Override
            public void onRestore() {
                Log.d(TAG, "onRestore: ");
            }

            @Override
            public void onMove() {
                Log.d(TAG, "onMove: ");
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bezierView.resetBezierView();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListActivity.class));
            }
        });
    }
}
