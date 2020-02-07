package com.example.a4;

import android.os.SystemClock;

class GameValues {
    /**
     * Intervals in milliseconds after which speed and acceleration increases
     */
    private static final long SCALE_INTERVAL = 8000;
    private static final int MAX_SCALE_FACTOR = 4;

    private int viewW;
    private int viewH;
    private int failThresY;
    private int failThresX;
    private float addY;
    private float basePathRadius;
    private float density;
    private long gameStartTime;
    private int addFruitInterval;
    private long lastFruitAddedTime;

    GameValues(int viewW, int viewH, float density) {
        this.viewW = viewW;
        this.viewH = viewH;
        this.density = density;
        addY = viewH * (2 / 3f); // Bottom 1/3rd of the screen
        basePathRadius = 30 * density;
        failThresY = viewH - 1;
        failThresX = viewW - 1;
        reset();
    }

    int getW() {
        return viewW;
    }

    int getH() {
        return viewH;
    }

    int getFailThresY() {
        return failThresY;
    }

    int getFailThresX() {
        return failThresX;
    }

    float getDensity() {
        return density;
    }

    float getAddY() {
        return addY - (float) (Math.random() * (MAX_SCALE_FACTOR - getScale()) * 200);
    }

    float getAddX() {
        return (float) (Math.random() * viewW);
    }

    float getPathRadius() {
        return basePathRadius + (int) (Math.random() * 6 * density);
    }

    void reset() {
        gameStartTime = SystemClock.uptimeMillis();
        addFruitInterval = (int) (Math.random() * 1000 + 1500);
        lastFruitAddedTime = 0;
    }

    int getScale() {
        long scale = (SystemClock.uptimeMillis() - gameStartTime) / SCALE_INTERVAL;
        return (int) Math.min(Math.max(1, scale), MAX_SCALE_FACTOR);
    }

    int numFruitsToAdd() {
        long currentTime = SystemClock.uptimeMillis();

        if (lastFruitAddedTime == 0) {
            lastFruitAddedTime = SystemClock.uptimeMillis();
            return 2;
        }

        long nextAddTime = lastFruitAddedTime + addFruitInterval;
        if (currentTime >= nextAddTime) {
            lastFruitAddedTime = currentTime;
            int toAdd = (int) (Math.random() * 2 + 1);
            addFruitInterval = (int) (Math.random() * 2000 - getScale() / MAX_SCALE_FACTOR);
            addFruitInterval = Math.max(800, addFruitInterval);
            return toAdd;
        }
        return 0;
    }
}
