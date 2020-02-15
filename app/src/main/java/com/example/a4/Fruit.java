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
    private static final int[] COLOURS = {0xFFC62828, 0xFFAD1457, 0xFF6A1B9A, 0xFF1565C0, 0xFF00838F,
            0xFF058372, 0xFF358A39, 0xFF9E9D24, 0xFFD88115, 0xFFE04E21};

    private Path originalPath;
    private Path currentPath;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Matrix transform = new Matrix();
    private int paintColour;
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
        paintColour = COLOURS[(int) (Math.random() * COLOURS.length)];
        paint.setColor(paintColour);
        paint.setStrokeWidth(5);

        part = false;
        originalPath = path;
        currentPath = new Path();

        int scale = dimens.getScale();
        float density = dimens.getDensity();

        speedx = (float) (Math.random() * 2 - 1) * density * scale;

        speedy = (float) -(Math.random() * 2 + 3) * density * scale;
        accx = 0;
        accy = (float) 0.1125 * density * scale;
        cutted = false;
    }

    private Fruit(Path path, float sx, float sy, float accy, int colour) {
        paintColour = colour;
        paint.setColor(colour);
        paint.setStrokeWidth(5);

        part = true;
        originalPath = path;
        currentPath = new Path(path);

        speedx = sx;

        speedy = sy;

        accx = 0;
        this.accy = accy;
        cutted = false;
    }

    private static Path makePath(float[] pointsx, float[] pointsy) {
        Path result = new Path();
        result.moveTo(pointsx[0], pointsy[0]);
        for (int i = 1; i < pointsx.length; i += 1) {
            result.lineTo(pointsx[i], pointsy[i]);
        }
        result.moveTo(pointsx[0], pointsy[0]);

        return result;
    }

    static void setFillColor(Fruit fruit, int color) {
        fruit.paint.setColor(color);
    }

    static void translate(Fruit fruit, float tx, float ty) {
        fruit.transform.postTranslate(tx, ty);
        fruit.originalPath.transform(fruit.transform, fruit.currentPath);
    }

    /**
     * Paints the Fruit to the screen using its current affine transform and paint settings (fill,
     * outline)
     */
    static void draw(Fruit fruit, Canvas canvas, GameValues dimens) {
        Region tempRegion = new Region();
        tempRegion.setPath(fruit.currentPath, dimens.getClipRegion());

        canvas.drawPath(fruit.currentPath, fruit.paint);
    }

    /**
     * Tests whether the line represented by the two points intersects this Fruit.
     */
    static boolean intersects(Fruit fruit, PointF p1, PointF p2, GameValues dimens) {
        Region fruitRegion = new Region();
        fruitRegion.setPath(fruit.currentPath, dimens.getClipRegion());

        Path cut = new Path();
        cut.moveTo(p1.x, p1.y);
        cut.lineTo(p2.x, p2.y);
        cut.lineTo(p2.x, p2.y - 1);
        cut.lineTo(p1.x, p1.y - 1);
        cut.moveTo(p1.x, p1.y);


        Region cutRegion = new Region();
        cutRegion.setPath(cut, dimens.getClipRegion());
        return cutRegion.op(fruitRegion, Region.Op.INTERSECT);
    }

    /**
     * Returns whether the given point is within the Fruit's shape.
     */
    public static boolean contains(Fruit fruit, PointF p1, GameValues dimens) {
        Region region = new Region();
        boolean valid = region.setPath(fruit.currentPath, dimens.getClipRegion());
        return valid && region.contains((int) p1.x, (int) p1.y);
    }

    Path getCurrentPath() {
        return currentPath;
    }

    /**
     * This method assumes that the line represented by the two points intersects the fruit. If not,
     * unpredictable results will occur. Returns two new Fruits, split by the line represented by
     * the two points given.
     */
    static Fruit[] split(Fruit fruit, PointF p1, PointF p2, GameValues dimens) {

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
            float[] xl = {p1X, p2X, p1X - 800, p2X - 800};
            float[] yl = {p1Y, p2Y, p1Y, p2Y};
            leftCoverPath = makePath(xl, yl);

            float[] xr = {p1X, p2X, p1X + 800, p2X + 800};
            float[] yr = {p1Y, p2Y, p1Y, p2Y};
            rightCoverPath = makePath(xr, yr);
        } else {
            float[] xl = {p1X, p2X, p1X, p2X};
            float[] yl = {p1Y, p2Y, p1Y - 800, p2Y - 800};
            leftCoverPath = makePath(xl, yl);

            float[] xr = {p1X, p2X, p1X, p2X};
            float[] yr = {p1Y, p2Y, p1Y + 800, p2Y + 800};
            rightCoverPath = makePath(xr, yr);
        }

        Region leftRegion = new Region();
        Region rightRegion = new Region();

        leftRegion.setPath(leftCoverPath, dimens.getClipRegion());
        rightRegion.setPath(rightCoverPath, dimens.getClipRegion());

        Region thisRegion = new Region();
        thisRegion.setPath(fruit.currentPath, dimens.getClipRegion());

        boolean resultl = leftRegion.op(thisRegion, Region.Op.INTERSECT);
        boolean resultr = rightRegion.op(thisRegion, Region.Op.INTERSECT);

        Path leftPath = leftRegion.getBoundaryPath();
        Path rightPath = rightRegion.getBoundaryPath();

        if (resultl && resultr) {
            return new Fruit[]{new Fruit(leftPath, -2, fruit.speedy, fruit.accy, fruit.paintColour),
                    new Fruit(rightPath, 2, fruit.speedy, fruit.accy, fruit.paintColour)};
        }
        return new Fruit[0];
    }
}
