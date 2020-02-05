package com.example.a4;

class DimensionsModel {
    private int viewW;
    private int viewH;
    private int failThres;
    private float addY;
    private float pathRadius;
    private float density;

    DimensionsModel(int viewW, int viewH, float density) {
        this.viewW = viewW;
        this.viewH = viewH;
        this.density = density;
        addY = viewH * (3 / 4f); // Bottom 1/4th of the screen
        pathRadius = 20 * density;
        failThres = viewH - (int) pathRadius;
    }

    int getW() {
        return viewW;
    }

    int getH() {
        return viewH;
    }

    int getFailThres() {
        return failThres;
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
        return pathRadius;
    }
}
