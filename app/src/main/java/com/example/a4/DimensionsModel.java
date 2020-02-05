package com.example.a4;

class DimensionsModel {
    private int viewW;
    private int viewH;
    private int failThres;
    private float addY;
    private float addXConst;
    private float pathRadius;

    DimensionsModel(int viewW, int viewH, float density) {
        this.viewW = viewW;
        this.viewH = viewH;
        addY = viewH * (3 / 4f); // Bottom 1/4th of the screen
        float edgeGap = 100 * density;
        addXConst = edgeGap + (viewW - 2 * edgeGap);
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

    float getAddY() {
        return addY;
    }

    float getAddX() {
        return (float) (Math.random() * addXConst);
    }

    float getPathRadius() {
        return pathRadius;
    }
}
