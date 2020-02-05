/*
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery
 */
package com.example.a4;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;

/**
 * Class that represents a Fruit. Can be split into two separate fruits.
 */
public class Fruit {
    private static int counter = 0;
    private int index;
    private Path path = new Path();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Matrix transform = new Matrix();
    float speedx;
    float speedy;
    float accx;
    float accy;
    private boolean belowLine;
    boolean part;
    boolean cutted;

    /**
     * A fruit is represented as Path, typically populated by a series of points
     */
    Fruit(float[] points) {
        init();

        counter++;
        this.part = false;
        this.index = counter;

        this.path.reset();
        this.path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            this.path.lineTo(points[i], points[i + 1]);
        }
        this.path.moveTo(points[0], points[1]);

        this.speedx = (float) (Math.random() * 6 + -3);

        this.speedy = (float) -(Math.random() * 10 + 15);

        this.accx = 0;
        this.accy = (float) 0.2;
        this.belowLine = false;
        this.cutted = false;
    }

    Fruit(Region region) {
        init();
        this.path = region.getBoundaryPath();
    }

    Fruit(Path path) {
        init();

        counter++;
        this.part = false;
        this.index = counter;

        this.path.reset();
        this.path = path;

        this.speedx = (float) (Math.random() * 6 + -3);

        this.speedy = (float) -(Math.random() * 5 + 7);

        this.accx = 0;
        this.accy = (float) 0.2;
        this.belowLine = false;
        this.cutted = false;
    }

    private Fruit(Path path, float sx, float sy) {
        init();

        counter++;
        this.part = true;
        this.index = counter;

        this.path.reset();
        this.path = path;

        this.speedx = sx;

        this.speedy = sy;

        this.accx = 0;
        this.accy = (float) 0.2;
        this.belowLine = false;
        this.cutted = false;
    }

    private void init() {
        this.paint.setColor(Color.BLUE);
        this.paint.setStrokeWidth(5);
    }

    private Path makePath(float[] pointsx, float[] pointsy) {
        Path result = new Path();
        result.moveTo(pointsx[0], pointsy[0]);
        for (int i = 1; i < pointsx.length; i += 1) {
            result.lineTo(pointsx[i], pointsy[i]);
        }
        result.moveTo(pointsx[0], pointsy[0]);

        return result;
    }

    /**
     * The color used to paint the interior of the Fruit.
     */
    public int getFillColor() {
        return paint.getColor();
    }

    void setFillColor(int color) {
        paint.setColor(color);
    }

    /**
     * The width of the outline stroke used when painting.
     */
    public double getOutlineWidth() {
        return paint.getStrokeWidth();
    }

    public void setOutlineWidth(float newWidth) {
        paint.setStrokeWidth(newWidth);
    }

    /**
     * Concatenates transforms to the Fruit's affine transform
     */
    public void rotate(float theta) {
        transform.postRotate(theta);
    }

    public void scale(float x, float y) {
        transform.postScale(x, y);
    }

    void translate(float tx, float ty) {
        transform.postTranslate(tx, ty);
    }

    /**
     * Returns the Fruit's affine transform that is used when painting
     */
    public Matrix getTransform() {
        return transform;
    }

    /**
     * The path used to describe the fruit shape.
     */
    Path getTransformedPath() {
        Path originalPath = new Path(path);
        Path transformedPath = new Path();
        originalPath.transform(transform, transformedPath);
        return transformedPath;
    }

    /**
     * Paints the Fruit to the screen using its current affine transform and paint settings (fill,
     * outline)
     */
    void draw(Canvas canvas) {
        Region tempRegion = new Region();
        Region clip = new Region(0, 0, 480, 800);
        tempRegion.setPath(getTransformedPath(), clip);


        canvas.drawPath(getTransformedPath(), paint);
    }

    /**
     * Tests whether the line represented by the two points intersects this Fruit.
     */
    boolean intersects(PointF p1, PointF p2) {
        Region fruitRegion = new Region();
        Region clip = new Region(0, 0, 480, 800);
        fruitRegion.setPath(getTransformedPath(), clip);

        Path cut = new Path();
        cut.moveTo(p1.x, p1.y);
        cut.lineTo(p2.x, p2.y);
        cut.lineTo(p2.x, p2.y - 1);
        cut.lineTo(p1.x, p1.y - 1);
        cut.moveTo(p1.x, p1.y);


        Region cutRegion = new Region();
        cutRegion.setPath(cut, clip);
        return cutRegion.op(fruitRegion, Region.Op.INTERSECT);

    }

    /**
     * Returns whether the given point is within the Fruit's shape.
     */
    public boolean contains(PointF p1) {
        Region region = new Region();
        Region clip = new Region(0, 0, 480, 800);
        boolean valid = region.setPath(getTransformedPath(), clip);
        return valid && region.contains((int) p1.x, (int) p1.y);
    }

    /**
     * This method assumes that the line represented by the two points intersects the fruit. If not,
     * unpredictable results will occur. Returns two new Fruits, split by the line represented by
     * the two points given.
     */
    @SuppressLint("NewApi")
    Fruit[] split(PointF p1, PointF p2) {

        float xLength = Math.abs(p1.x - p2.x);
        float yLength = Math.abs(p1.y - p2.y);
        float length = (float) Math.sqrt(Math.pow(xLength, 2) + Math.pow(yLength, 2));

        float sin = xLength / length;
        float cos = yLength / length;

        float xPlus = 100 * sin;
        float yPlus = 100 * cos;

        float p1X = p1.x;
        float p1Y = p1.y;
        float p2X = p2.x;
        float p2Y = p2.y;
        if (p1X < p2X) {
            p1X -= xPlus;
            p2X += xPlus;
        } else {
            p1X += xPlus;
            p2X -= xPlus;
        }

        if (p1Y < p2Y) {
            p1Y -= yPlus;
            p2Y += yPlus;
        } else {
            p1Y += yPlus;
            p2Y -= yPlus;
        }

        Path leftCoverPath = null;
        Path rightCoverPath = null;

        if (yLength > xLength) {
            float[] xl = {p1X, p2X, p1X - 200, p2X - 200};
            float[] yl = {p1Y, p2Y, p1Y, p2Y};
            leftCoverPath = makePath(xl, yl);

            float[] xr = {p1X, p2X, p1X + 200, p2X + 200};
            float[] yr = {p1Y, p2Y, p1Y, p2Y};
            rightCoverPath = makePath(xr, yr);
        } else {
            float[] xl = {p1X, p2X, p1X, p2X};
            float[] yl = {p1Y, p2Y, p1Y - 200, p2Y - 200};
            leftCoverPath = makePath(xl, yl);

            float[] xr = {p1X, p2X, p1X, p2X};
            float[] yr = {p1Y, p2Y, p1Y + 200, p2Y + 200};
            rightCoverPath = makePath(xr, yr);
        }

        Region clip = new Region(0, 0, 480, 800);
        Region leftRegion = new Region();
        Region rightRegion = new Region();

        leftRegion.setPath(leftCoverPath, clip);
        rightRegion.setPath(rightCoverPath, clip);

        Region thisRegion = new Region();
        thisRegion.setPath(getTransformedPath(), clip);

        boolean resultl = leftRegion.op(thisRegion, Region.Op.INTERSECT);
        boolean resultr = rightRegion.op(thisRegion, Region.Op.INTERSECT);

        Path leftPath = leftRegion.getBoundaryPath();
        Path rightPath = rightRegion.getBoundaryPath();

        if (resultl && resultr) {
            return new Fruit[]{new Fruit(leftPath, -2, this.speedy), new Fruit(rightPath, 2, this.speedy)};
        }
        return new Fruit[0];
    }
}
