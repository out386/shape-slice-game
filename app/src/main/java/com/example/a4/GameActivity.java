/*
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery
 */
package com.example.a4;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Observable;
import java.util.Observer;

public class GameActivity extends AppCompatActivity implements Observer {
    private static final int BLOCK_BACK_THRES = 2000;
    private Model model;
    private TextView scoreView;
    private RatingBar lifeView;
    private MainView mainView;
    private long lastBackTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // create the views and add them to the main activity
        scoreView = findViewById(R.id.score);
        lifeView = findViewById(R.id.life);
        View floatingView = findViewById(R.id.floating_view);
        lifeView.setIsIndicator(true);

        floatingView.setOnClickListener(view -> {
            floatingView.setOnClickListener(null);
            floatingView.setVisibility(View.GONE);
            mainView = new MainView(this);
            model = mainView.getModel();
            model.addObserver(this);
            ViewGroup v2 = findViewById(R.id.main_2);
            v2.addView(mainView);
            mainView.init(this);

            // notify all views
            model.initObservers();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mainView != null)
            mainView.stop();
        finish();
    }

    @SuppressLint("SetTextI18n")  // As if I'm planning to ever translate this
    @Override
    public void update(Observable o, Object arg) {
        runOnUiThread(() -> {
            scoreView.setText("Score: " + model.score);
            lifeView.setRating(model.life);
        });
    }

    void startRestartActivity() {
        runOnUiThread(() -> {
            Intent i = new Intent(this, RestartActivity.class)
                    .putExtra("score", model.score);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            mainView.stop();
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (lastBackTime == 0 || SystemClock.uptimeMillis() - lastBackTime > BLOCK_BACK_THRES) {
            Toast.makeText(this, "Press once again to exit", Toast.LENGTH_SHORT).show();
            lastBackTime = SystemClock.uptimeMillis();
        } else {
            super.onBackPressed();
        }

    }
}
