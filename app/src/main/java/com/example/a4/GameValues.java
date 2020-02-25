package com.example.a4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Region;
import android.os.SystemClock;

import androidx.annotation.Nullable;

class GameValues {
    /**
     * Intervals in milliseconds after which speed and acceleration increases
     */
    private static final long SCALE_INTERVAL = 8000;
    private static final int MAX_SCALE_FACTOR = 3;

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
    private Region clipRegion;
    private Bitmap edgeLogo;
    private Bitmap tileBg;
    private int edgeLogoPosX;
    private int edgeLogoPosY;

    GameValues(Context context, int viewW, int viewH, float density) {
        this.viewW = viewW;
        this.viewH = viewH;
        this.density = density;
        addY = viewH * (2 / 3f); // Bottom 1/3rd of the screen
        basePathRadius = 30 * density;
        failThresY = viewH - 1;
        failThresX = viewW - 1;
        clipRegion = new Region(0, 0, viewW, viewH);
        if (context != null) {
            Bitmap bg = BitmapFactory.decodeResource(context.getResources(), R.drawable.tile_bg);
            int lw = Math.max(viewH, viewW);
            tileBg = scaleBitmap(bg, lw, viewW, viewH);

            Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.edge_logo);
            edgeLogo = scaleBitmap(b, (int) (200 * density));
            edgeLogoPosX = (viewW - edgeLogo.getWidth()) / 2;
            edgeLogoPosY = (viewH - edgeLogo.getHeight()) / 2;
        }
        reset();
    }

    int getW() {
        return viewW;
    }

    int getH() {
        return viewH;
    }

    int getEdgeLogoPosX() {
        return edgeLogoPosX;
    }

    int getEdgeLogoPosY() {
        return edgeLogoPosY;
    }

    @Nullable
    Bitmap getTileBg() {
        return tileBg;
    }

    @Nullable
    Bitmap getEdgeLogo() {
        return edgeLogo;
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
        return (float) Math.max(basePathRadius, Math.random() * viewW - basePathRadius);
    }

    float getPathRadius() {
        return basePathRadius + (int) (Math.random() * 6 * density);
    }

    Region getClipRegion() {
        return clipRegion;
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
        float scale = getScale();
        if (currentTime >= nextAddTime) {
            lastFruitAddedTime = currentTime;
            int toAdd = (int) (Math.random() * 2 + Math.max(1, scale - 1));
            addFruitInterval = (int) (Math.random() * 2000 - scale / MAX_SCALE_FACTOR);
            addFruitInterval = Math.max(720, addFruitInterval);
            return toAdd;
        }
        return 0;
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int width) {
        int currentWidth = bitmap.getWidth();
        int currentHeight = bitmap.getHeight();
        float scale = ((float) width) / currentWidth;
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);

        Bitmap b = Bitmap.createBitmap(bitmap, 0, 0, currentWidth, currentHeight,
                matrix, true);
        bitmap.recycle();
        return b;
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int scaleSize, int width, int height) {
        Bitmap b = scaleBitmap(bitmap, scaleSize);
        Bitmap resizedBitmap = Bitmap.createBitmap(b, 0, 0, width, height);
        b.recycle();
        return resizedBitmap;
    }
}
