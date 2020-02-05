/*
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery
 */
package com.example.a4;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends Activity {
    private Model model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new Model();

        setContentView(R.layout.main);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // create the views and add them to the main activity
        TitleView titleView = new TitleView(this, model);
        ViewGroup v1 = findViewById(R.id.main_1);
        v1.addView(titleView);

        MainView mainView = new MainView(this, model);
        ViewGroup v2 = findViewById(R.id.main_2);
        v2.addView(mainView);
        mainView.init();


        // notify all views
        model.initObservers();

    }
}
