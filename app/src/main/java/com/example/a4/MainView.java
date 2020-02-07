/**
 * CS349 Winter 2014 Assignment 4 Demo Code Jeff Avery & Michael Terry
 */
package com.example.a4;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/*
 * View of the main game area.
 * Displays pieces of fruit, and allows players to slice them.
 */
@SuppressLint("ViewConstructor")
public class MainView extends View {
    private int addFruitInterval = (int) (Math.random() * 1000 + 1500);
    private long lastFruitAddedTime = 0;
    private long gameStartTime;
    private Handler handler;
    @Nullable
    private DimensionsModel dimens;

    private final Model model;
    private final MouseDrag drag = new MouseDrag();
    public Context viewContext;

    public boolean addCuts = false;
    public int toAdd = 2;
    public ArrayList<Fruit> newFruits = new ArrayList();
    private DrawRunnable drawRunnable;

    // Constructor
    @SuppressLint("ClickableViewAccessibility")
    MainView(Context context, Model m) {
        super(context);
        viewContext = context;
        handler = new Handler();
        // register this view with the model
        model = m;
        gameStartTime = SystemClock.uptimeMillis();

        setOnTouchListener((v, event) -> {
            if (dimens == null)
                return true;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drag.start(event.getX(), event.getY());
                    break;

                case MotionEvent.ACTION_UP:
                    drag.stop(event.getX(), event.getY());
                    // find intersected shapes
                    for (Fruit s : model.getShapes()) {
                        if (s.intersects(drag.getStart(), drag.getEnd(), dimens)) {
                            s.setFillColor(Color.RED);

                            try {
                                Fruit[] goingAL = s.split(drag.getStart(), drag.getEnd(), dimens);
                                newFruits.addAll(Arrays.asList(goingAL));
                                s.cutted = true;
                                addCuts = true;

                            } catch (Exception ignored) {
                            }
                        }
                    }
                    break;
            }
            return true;
        });
    }

    public void init() {
        lastFruitAddedTime = 0;
        gameStartTime = SystemClock.uptimeMillis();
        model.clear();
        drag.reset();
        addCuts = false;
        toAdd = 2;
        newFruits = new ArrayList<>();
        //goFullscreen(((Activity) viewContext).getWindow());
        setBackgroundColor(Color.TRANSPARENT);
        invalidate();
        if (dimens != null) {
            if (drawRunnable == null)
                drawRunnable = new DrawRunnable();
            handler.post(drawRunnable);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        dimens = new DimensionsModel(w, h, getResources().getDisplayMetrics().density);
        if (drawRunnable == null) {
            drawRunnable = new DrawRunnable();
            handler.post(drawRunnable);
        }
    }

    // inner class to track mouse drag
    // a better solution *might* be to dynamically track touch movement
    // in the controller above
    class MouseDrag {
        private float startx, starty;
        private float endx, endy;

        PointF getStart() {
            return new PointF(startx, starty);
        }

        PointF getEnd() {
            return new PointF(endx, endy);
        }

        void start(float x, float y) {
            this.startx = x;
            this.starty = y;
        }

        void stop(float x, float y) {
            this.endx = x;
            this.endy = y;
        }

        void reset() {
            startx = starty = endx = endy = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dimens == null)
            return;

        // draw all pieces of fruit
        for (Fruit s : model.getShapes()) {
            s.draw(canvas, dimens);
        }
    }

    class DrawRunnable implements Runnable {
        @Override
        public void run() {
            for (Iterator<Fruit> it = model.shapes.iterator(); it.hasNext(); ) {
                if (dimens == null)
                    continue;
                Fruit temp = it.next();
                Region tempRegion = new Region();
                Region clip = new Region(0, 0, dimens.getW(), dimens.getH());
                tempRegion.setPath(temp.getTransformedPath(), clip);

                Rect bounds = tempRegion.getBounds();
                if (bounds.left < 1 && temp.speedx < 0
                        || bounds.right > dimens.getFailThresX() && temp.speedx > 0)
                    temp.speedx *= -1;

                temp.translate(temp.speedx, temp.speedy);
                temp.speedx += temp.accx;
                temp.speedy += temp.accy;


                if (bounds.bottom > dimens.getFailThresY()) {
                    if (!temp.part) {
                        model.life--;
                        model.notifyObs();
                        if (model.life <= 0) {
                            // Not bothering to clean up here as init() will do it soon anyway
                            showDialog();
                            return;
                        }
                    }
                    it.remove();
                } else if (temp.cutted) {
                    if (!temp.part) {
                        model.score++;
                    }
                    it.remove();
                    model.notifyObs();
                }
            }

            setShouldAddFruit();
            while (toAdd > 0) {
                toAdd--;
                addFruit();
            }

            if (addCuts) {
                for (Fruit f : newFruits) {
                    model.add(f);
                }
                addCuts = false;
                newFruits.clear();
            }
            invalidate();

            // '16' will keep the frame rate at or just below 60 FPS
            handler.postDelayed(this, 16);
        }
    }

    private void setShouldAddFruit() {
        long currentTime = SystemClock.uptimeMillis();
        long nextAddTime = lastFruitAddedTime + addFruitInterval;
        if (currentTime >= nextAddTime) {
            lastFruitAddedTime = currentTime;
            toAdd = (int) (Math.random() * 2 + 1);
            addFruitInterval = (int) (Math.random() * 1000 + 1500);
        }
    }

    private void showDialog() {
        AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(viewContext);
        dialogBuilder.setMessage("Restart?");
        dialogBuilder.setPositiveButton("Yes", (dialog, which) -> init());

        dialogBuilder.setNegativeButton("No", (dialog, which) ->
                ((Activity) viewContext).finish());

        AlertDialog dialog = dialogBuilder.create();
        //goFullscreen(dialog.getWindow());
        dialog.show();
    }

    private void goFullscreen(Window w) {
        if (w != null) {
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }

    private void addFruit() {
        if (dimens != null) {
            Path newPath = new Path();
            newPath.addCircle(dimens.getAddX(), dimens.getAddY(), dimens.getPathRadius(),
                    Path.Direction.CCW);
            Fruit f = new Fruit(newPath, gameStartTime, dimens);
            model.add(f);
        }
    }
}
