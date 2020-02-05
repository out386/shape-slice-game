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
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/*
 * View of the main game area.
 * Displays pieces of fruit, and allows players to slice them.
 */
@SuppressLint("ViewConstructor")
public class MainView extends View implements Observer {
    public int notiTimes = 0;
    public int fails = 0;
    private final Model model;
    private final MouseDrag drag = new MouseDrag();
    public Timer drawer;
    public Timer creator;
    public Context viewContext;

    public boolean addCuts = false;
    public int toAdd = 0;
    public ArrayList<Fruit> newFruits = new ArrayList();

    // Constructor
    @SuppressLint("ClickableViewAccessibility")
    MainView(Context context, Model m) {
        super(context);
        viewContext = context;
        // register this view with the model
        model = m;
        model.addObserver(this);

        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drag.start(event.getX(), event.getY());
                    break;

                case MotionEvent.ACTION_UP:
                    drag.stop(event.getX(), event.getY());

                    // find intersected shapes
                    for (Fruit s : model.getShapes()) {
                        if (s.intersects(drag.getStart(), drag.getEnd())) {
                            s.setFillColor(Color.RED);

                            try {
                                Fruit[] goingAL = s.split(drag.getStart(), drag.getEnd());
                                newFruits.addAll(Arrays.asList(goingAL));
                                s.cutted = true;
                                addCuts = true;

                            } catch (Exception ex) {
                                //Log.e("fruit_ninja", "Error: " + ex.getMessage());
                            }
                        } else {
                            s.setFillColor(Color.BLUE);
                        }
                        invalidate();
                    }
                    break;
            }
            return true;
        });
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw background
        setBackgroundColor(Color.WHITE);

        // draw all pieces of fruit
        for (Fruit s : model.getShapes()) {
            s.draw(canvas);
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        invalidate();
    }

    class MyDrawTask extends TimerTask {
        @Override
        public void run() {
            ((Activity) viewContext).runOnUiThread(() -> {

                notiTimes++;
                if (notiTimes >= 60) {
                    model.timePassed++;
                    notiTimes = 0;
                }

                for (Iterator<Fruit> it = model.shapes.iterator(); it.hasNext(); ) {
                    Fruit temp = it.next();
                    Region tempRegion = new Region();
                    Region clip = new Region(0, 0, 480, 800);
                    tempRegion.setPath(temp.getTransformedPath(), clip);

                    temp.translate(temp.speedx, temp.speedy);
                    temp.speedx += temp.accx;
                    temp.speedy += temp.accy;

                    if (tempRegion.getBounds().top > 800 - 135) {
                        if (!temp.part) {
                            fails++;
                            //Log.d("fails", String.valueOf(fails));
                            if (fails >= 5) {
                                //failed or time up(restart)

                                model.score = 0;
                                model.timePassed = 0;

                                drawer.cancel();  // Terminates this timer, discarding any currently scheduled tasks.
                                drawer.purge();

                                creator.cancel();
                                creator.purge();

                                newFruits.clear();
                                model.shapes.clear();
                                fails = 0;
                                //fail text
                                AlertDialog.Builder dialogBuilder =
                                        new AlertDialog.Builder(viewContext);
                                dialogBuilder.setMessage("restart");
                                dialogBuilder.setPositiveButton("Yes", (dialog, which) -> init());

                                dialogBuilder.setNegativeButton("Noo", (dialog, which) ->
                                        ((Activity) viewContext).finish());

                                dialogBuilder.create().show();
                                break;
                            }
                        }
                        it.remove();
                    } else if (temp.cutted) {
                        if (!temp.part) {
                            model.score++;
                        }
                        it.remove();
                        model.notifyObservers();
                    }
                }

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
            });
        }
    }

    int penInterval = (int) (Math.random() * 1000 + 1500);

    class MyCreateTask extends TimerTask {
        @Override
        public void run() {
            ((Activity) viewContext).runOnUiThread(() -> {
                toAdd = (int) (Math.random() * 2 + 1);
                penInterval = (int) (Math.random() * 1000 + 1500);
            });
        }
    }

    public void init() {
        drawer = new Timer();
        creator = new Timer();
        drawer.scheduleAtFixedRate(new MyDrawTask(), 1000, 1000 / 20);
        creator.scheduleAtFixedRate(new MyCreateTask(), 1000, penInterval);

    }

    private void addFruit() {
        float y = 800 - 135;
        float x = (float) (Math.random() * 300 + 100);
        Path newPath = new Path();
        newPath.addCircle(x, y, 30, Path.Direction.CCW);
        Fruit f = new Fruit(newPath);
        model.add(f);

    }
}
