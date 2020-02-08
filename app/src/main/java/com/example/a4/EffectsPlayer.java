package com.example.a4;

import android.content.Context;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

class EffectsPlayer {
    private SoundPool soundPool;
    private int[] ids;
    private Vibrator vibrator;
    private VibrationEffect vibrationEffect;

    EffectsPlayer(Context context) {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .build();
        ids = new int[6];
        ids[0] = soundPool.load(context, R.raw.slice1, 1);
        ids[1] = soundPool.load(context, R.raw.slice2, 1);
        ids[2] = soundPool.load(context, R.raw.slice3, 1);
        ids[3] = soundPool.load(context, R.raw.slice4, 1);
        ids[4] = soundPool.load(context, R.raw.slice5, 1);
        ids[5] = soundPool.load(context, R.raw.slice6, 1);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26)
            vibrationEffect = VibrationEffect.createOneShot(30, 80);
    }

    void play() {
        int id = ids[(int) (Math.random() * ids.length)];
        soundPool.play(id, 1f, 1f, 0, 0, 1);
    }

    void destroy() {
        if (soundPool != null)
            soundPool.release();
        soundPool = null;
    }

    void vibrate() {
        if (Build.VERSION.SDK_INT >= 26)
            vibrator.vibrate(vibrationEffect);
        else
            vibrator.vibrate(30);
    }
}
