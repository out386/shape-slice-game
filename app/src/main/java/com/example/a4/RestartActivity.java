package com.example.a4;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class RestartActivity extends Activity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restart);

        TextView scoreTv = findViewById(R.id.restart_score);
        TextView hScoreTv = findViewById(R.id.restart_hscore);
        Button yesButton = findViewById(R.id.restart_yes);
        Button noButton = findViewById(R.id.restart_no);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        int score = getIntent().getIntExtra("score", 0);
        int hscore = prefs.getInt("hscore", 0);

        if (score > hscore) {
            hscore = score;
            prefs.edit()
                    .putInt("hscore", hscore)
                    .apply();
        }

        scoreTv.setText("Your score: " + score);
        hScoreTv.setText("Your highscore: " + hscore);

        yesButton.setOnClickListener(view -> {
            setResult(Activity.RESULT_OK);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        noButton.setOnClickListener(view -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }
}
