/**
 * CS349 Winter 2014 Assignment 4 Demo Code Jeff Avery & Michael Terry
 */
package com.example.a4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/*
 * View of the main game area.
 * Displays pieces of fruit, and allows players to slice them.
 */
@SuppressLint("ViewConstructor")
public class MainView extends SurfaceView implements Runnable {
    private boolean isRunning;
    private Thread drawThread;
    private SurfaceHolder surfaceHolder;
    @Nullable
    private GameValues gameValues;

    private Model model;
    private final MouseDrag drag = new MouseDrag();
    public GameActivity activity;

    public boolean addCuts = false;
    public LinkedList<Fruit> newFruits = new LinkedList<>();
    private EffectsPlayer effectsPlayer;


    public MainView(Context context) {
        super(context);
        init();
    }

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        model = new Model();
        drawThread = new Thread(this);
        surfaceHolder = getHolder();

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
                    break;
            }
            return true;
        });
    }

    Model getModel() {
        return model;
    }

    void init(GameActivity activity) {
        this.activity = activity;
        effectsPlayer = new EffectsPlayer(activity);
        model.clear();
        drag.reset();
        addCuts = false;
        newFruits = new LinkedList<>();
        //goFullscreen(((Activity) activity).getWindow());
        setBackgroundColor(Color.TRANSPARENT);
        invalidate();
        if (gameValues != null) {
            gameValues.reset();
            if (!isRunning) {
                isRunning = true;
                drawThread.start();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gameValues = new GameValues(getContext(), w, h, getResources().getDisplayMetrics().density);
        if (!isRunning) {
            isRunning = true;
            drawThread.start();
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
            this.endx = 0;
            this.endy = 0;
        }

        void stop(float x, float y) {
            this.endx = x;
            this.endy = y;
        }

        void reset() {
            startx = starty = endx = endy = 0;
        }
    }

    private void findIntersects() {
        if (gameValues == null)
            return;
        PointF start = drag.getStart();
        PointF end = drag.getEnd();
        if (start.x == 0 || end.x == 0) // drag in progress or not started
            return;
        drag.reset();
        for (Fruit s : model.getShapes()) {
            if (Fruit.intersects(s, start, end, gameValues)) {
                try {
                    Fruit[] goingAL = Fruit.split(s, start, end, gameValues);
                    newFruits.addAll(Arrays.asList(goingAL));
                    s.cutted = true;
                    addCuts = true;
                    effectsPlayer.play();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void run() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            if (!surfaceHolder.getSurface().isValid()) {
                continue;
            }

            findIntersects();

            for (Iterator<Fruit> it = model.getShapes().iterator(); it.hasNext(); ) {
                if (gameValues == null) {
                    continue;
                }
                Fruit temp = it.next();

                RectF bounds = new RectF();
                temp.getCurrentPath().computeBounds(bounds, true);

                if (bounds.left < 0 && temp.speedx < 0
                        || bounds.right > gameValues.getFailThresX() && temp.speedx > 0)
                    temp.speedx *= -1;

                Fruit.translate(temp, temp.speedx, temp.speedy);
                temp.speedx += temp.accx;
                temp.speedy += temp.accy;


                if (bounds.bottom > gameValues.getFailThresY()) {
                    if (!temp.part) {
                        effectsPlayer.vibrate();
                        model.life--;
                        model.notifyObs();
                        if (model.life <= 0) {
                            if (activity != null)
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
                while (toAdd-- > 0) {
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
            drawScene();
        }
    }

    private void drawScene() {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            if (gameValues != null) {
                drawBg(canvas);
                // draw all pieces of fruit
                for (Fruit s : model.getShapes()) {
                    Fruit.draw(s, canvas, gameValues);
                }
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBg(Canvas canvas) {
        @SuppressWarnings("ConstantConditions")
        Bitmap b = gameValues.getEdgeLogo();
        Bitmap tileBg = gameValues.getTileBg();
        if (tileBg != null)
            canvas.drawBitmap(tileBg, 0, 0, null);
        if (b != null)
            canvas.drawBitmap(b, gameValues.getEdgeLogoPosX(), gameValues.getEdgeLogoPosY(), null);
    }

    void stop() {
        isRunning = false;
        if (drawThread != null) {
            drawThread.interrupt();
        }
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
