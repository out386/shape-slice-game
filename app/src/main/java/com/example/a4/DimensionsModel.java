package com.example.a4;

class DimensionsModel {
    private int viewW;
    private int viewH;
    private int failThresY;
    private int failThresX;
    private float addY;
    private float basePathRadius;
    private float density;

    DimensionsModel(int viewW, int viewH, float density) {
        this.viewW = viewW;
        this.viewH = viewH;
        this.density = density;
        addY = viewH * (3 / 4f); // Bottom 1/4th of the screen
        basePathRadius = 20 * density;
        failThresY = viewH - 1;
        failThresX = viewW - 1;
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
        return addY;
    }

    float getAddX() {
        return (float) (Math.random() * viewW);
    }

    float getPathRadius() {
        return basePathRadius + (int) (Math.random() * 6 * density);
    }
}
