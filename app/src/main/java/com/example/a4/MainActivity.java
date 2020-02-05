/*
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery
 */
package com.example.a4;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;

public class MainActivity extends Activity {
    private Model model;
    public static Point displaySize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setTitle("2D fruit ninja Demo");

        // save display size
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        // initialize model
        model = new Model();

        // set view
        setContentView(R.layout.main);
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
