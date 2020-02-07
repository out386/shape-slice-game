/*
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery
 */
package com.example.a4;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;

/**
 * Class that represents a Fruit. Can be split into two separate fruits.
 */
public class Fruit {
    private static final int[] COLOURS = {0xFF961C1C, 0xFF6B1142, 0xFF3F1672, 0xFF171F6F, 0xFF0B3D66,
            0xFF006064, 0xFF044E41, 0xFF2A5818, 0xFFA54813, 0xFFC06B21, 0xFF83781A};

    private Path path = new Path();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Matrix transform = new Matrix();
    private int paintColour;
    private boolean alreadySplit;
    float speedx;
    float speedy;
    float accx;
    float accy;
    boolean part;
    boolean cutted;

    /**
     * A fruit is represented as Path, typically populated by a series of points
     */

    Fruit(Path path, GameValues dimens) {
        init();

        this.part = false;

        this.path.reset();
        this.path = path;

        int scale = dimens.getScale();
        float density = dimens.getDensity();

        this.speedx = (float) (Math.random() * 2 - 1) * density * scale;

        this.speedy = (float) -(Math.random() * 2 + 3) * density * scale;
        this.accx = 0;
        this.accy = (float) 0.1125 * density * scale;
        this.cutted = false;
    }

    private Fruit(Path path, float sx, float sy, float accy, int colour) {
        this.paintColour = colour;
        this.alreadySplit = true;
        this.paint.setColor(colour);
        this.paint.setStrokeWidth(5);

        this.part = true;

        this.path.reset();
        this.path = path;

        this.speedx = sx;

        this.speedy = sy;

        this.accx = 0;
        this.accy = accy;
        this.cutted = false;
    }

    private void init() {
        paintColour = COLOURS[(int) (Math.random() * COLOURS.length)];
        this.paint.setColor(paintColour);
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

    void translate(float tx, float ty) {
        transform.postTranslate(tx, ty);
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
    void draw(Canvas canvas, GameValues dimens) {
        Region tempRegion = new Region();
        Region clip = new Region(0, 0, dimens.getW(), dimens.getH());
        tempRegion.setPath(getTransformedPath(), clip);

        canvas.drawPath(getTransformedPath(), paint);
    }

    /**
     * Tests whether the line represented by the two points intersects this Fruit.
     */
    boolean intersects(PointF p1, PointF p2, GameValues dimens) {
        Region fruitRegion = new Region();
        Region clip = new Region(0, 0, dimens.getW(), dimens.getH());
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
     * Tests whether the line represented by the two points intersects this Fruit.
     */
    boolean intersectPoint(float oldx, float oldy, float x, float y, GameValues dimens) {
        Region fruitRegion = new Region();
        Region clip = new Region(0, 0, dimens.getW(), dimens.getH());
        fruitRegion.setPath(getTransformedPath(), clip);

        Path cut = new Path();
        cut.moveTo(oldx, oldy);
        cut.lineTo(x, y);
        cut.lineTo(x, y - 1);
        cut.lineTo(oldx, oldy - 1);
        cut.moveTo(x, y);

        Region cutRegion = new Region();
        cutRegion.setPath(cut, clip);
        return cutRegion.op(fruitRegion, Region.Op.INTERSECT);

    }

    /**
     * Returns whether the given point is within the Fruit's shape.
     */
    public boolean contains(PointF p1, GameValues dimens) {
        Region region = new Region();
        Region clip = new Region(0, 0, dimens.getW(), dimens.getH());
        boolean valid = region.setPath(getTransformedPath(), clip);
        return valid && region.contains((int) p1.x, (int) p1.y);
    }

    /**
     * This method assumes that the line represented by the two points intersects the fruit. If not,
     * unpredictable results will occur. Returns two new Fruits, split by the line represented by
     * the two points given.
     */
    Fruit[] split(PointF p1, PointF p2, GameValues dimens) {
        if (alreadySplit)
            return null;

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

        Path leftCoverPath;
        Path rightCoverPath;

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

        Region clip = new Region(0, 0, dimens.getW(), dimens.getH());
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
            return new Fruit[]{new Fruit(leftPath, -2, speedy, accy, paintColour),
                    new Fruit(rightPath, 2, speedy, accy, paintColour)};
        }
        return new Fruit[0];
    }
}
