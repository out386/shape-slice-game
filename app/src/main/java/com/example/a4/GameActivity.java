/*
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery
 */
package com.example.a4;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

public class GameActivity extends Activity implements Observer {
    private Model model;
    private TextView scoreView;
    private RatingBar lifeView;
    private MainView mainView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new Model();

        setContentView(R.layout.main);
        /*getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );*/
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // create the views and add them to the main activity
        scoreView = findViewById(R.id.score);
        lifeView = findViewById(R.id.life);
        lifeView.setIsIndicator(true);
        model.addObserver(this);

        mainView = new MainView(this, model);
        ViewGroup v2 = findViewById(R.id.main_2);
        v2.addView(mainView);
        mainView.init();


        // notify all views
        model.initObservers();
    }

    @SuppressLint("SetTextI18n")  // As if I'm planning to ever translate this
    @Override
    public void update(Observable o, Object arg) {
        scoreView.setText("Score: " + model.score);
        lifeView.setRating(model.life);
    }

    void startRestartActivity() {
        Intent i = new Intent(this, RestartActivity.class)
                .putExtra("score", model.score);
        startActivityForResult(i, 1);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK)
                mainView.init();
            else if (resultCode == Activity.RESULT_CANCELED)
                finish();
        }
    }
}
