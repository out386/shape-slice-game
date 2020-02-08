/**
 * CS349 Winter 2014 Assignment 4 Demo Code Jeff Avery & Michael Terry
 */
package com.example.a4;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
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
    private Handler handler;
    @Nullable
    private GameValues gameValues;

    private final Model model;
    private final MouseDrag drag = new MouseDrag();
    public MainActivity activity;

    public boolean addCuts = false;
    public ArrayList<Fruit> newFruits = new ArrayList();
    private DrawRunnable drawRunnable;
    private EffectsPlayer effectsPlayer;

    // Constructor
    @SuppressLint("ClickableViewAccessibility")
    MainView(MainActivity activity, Model m) {
        super(activity);
        this.activity = activity;
        handler = new Handler();
        effectsPlayer = new EffectsPlayer(activity);
        // register this view with the model
        model = m;

        setOnTouchListener((v, event) -> {
            if (gameValues == null)
                return true;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drag.start(event.getX(), event.getY());
                    break;

                case MotionEvent.ACTION_UP:
                    drag.stop(event.getX(), event.getY());
                    // find intersected shapes
                    for (Fruit s : model.getShapes()) {
                        if (s.intersects(drag.getStart(), drag.getEnd(), gameValues)) {
                            s.setFillColor(Color.RED);

                            try {
                                Fruit[] goingAL = s.split(drag.getStart(), drag.getEnd(), gameValues);
                                newFruits.addAll(Arrays.asList(goingAL));
                                s.cutted = true;
                                addCuts = true;
                                effectsPlayer.play();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    break;
            }
            return true;
        });
    }

    void init() {
        model.clear();
        drag.reset();
        addCuts = false;
        newFruits = new ArrayList<>();
        //goFullscreen(((Activity) activity).getWindow());
        setBackgroundColor(Color.TRANSPARENT);
        invalidate();
        if (gameValues != null) {
            gameValues.reset();
            if (drawRunnable == null)
                drawRunnable = new DrawRunnable();
            handler.post(drawRunnable);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gameValues = new GameValues(w, h, getResources().getDisplayMetrics().density);
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
        if (gameValues == null)
            return;

        // draw all pieces of fruit
        for (Fruit s : model.getShapes()) {
            s.draw(canvas, gameValues);
        }
    }

    class DrawRunnable implements Runnable {
        @Override
        public void run() {
            for (Iterator<Fruit> it = model.shapes.iterator(); it.hasNext(); ) {
                if (gameValues == null)
                    continue;
                Fruit temp = it.next();
                Region tempRegion = new Region();
                Region clip = new Region(0, 0, gameValues.getW(), gameValues.getH());
                tempRegion.setPath(temp.getTransformedPath(), clip);

                Rect bounds = tempRegion.getBounds();
                if (bounds.left < 1 && temp.speedx < 0
                        || bounds.right > gameValues.getFailThresX() && temp.speedx > 0)
                    temp.speedx *= -1;

                temp.translate(temp.speedx, temp.speedy);
                temp.speedx += temp.accx;
                temp.speedy += temp.accy;


                if (bounds.bottom > gameValues.getFailThresY()) {
                    if (!temp.part) {
                        effectsPlayer.vibrate();
                        model.life--;
                        model.notifyObs();
                        if (model.life <= 0) {
                            // Not bothering to clean up here as init() will do it soon anyway
                            activity.startRestartActivity();
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

            if (gameValues != null) {
                int toAdd = gameValues.numFruitsToAdd();
                while (toAdd > 0) {
                    toAdd--;
                    addFruit();
                }
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

    void stop() {
        handler.removeCallbacksAndMessages(null);
        effectsPlayer.destroy();
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
        if (gameValues != null) {
            Path newPath = new Path();
            newPath.addCircle(gameValues.getAddX(), gameValues.getAddY(), gameValues.getPathRadius(),
                    Path.Direction.CCW);
            Fruit f = new Fruit(newPath, gameValues);
            model.add(f);
        }
    }
}
