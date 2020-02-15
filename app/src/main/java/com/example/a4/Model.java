/*
 * CS349 Winter 2014
 * Assignment 4 Demo Code
 * Jeff Avery & Michael Terry
 */
package com.example.a4;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/*
 * Class the contains a list of fruit to display.
 * Follows MVC pattern, with methods to add observers,
 * and notify them when the fruit list changes.
 */
public class Model extends Observable {
    // List of fruit that we want to display
    private LinkedList<Fruit> shapes;
    int score;
    int life;

    // Constructor
    Model() {
        shapes = new LinkedList<>();
        score = 0;
        life = 5;
    }

    void clear() {
        shapes = new LinkedList<>();
        score = 0;
        life = 5;
        initObservers();
    }

    void notifyObs() {
        setChanged();
        notifyObservers();
    }

    // Model methods
    // You may need to add more methods here, depending on required functionality.
    // For instance, this sample makes to effort to discard fruit from the list.
    void add(Fruit s) {
        shapes.add(s);
    }

    public void remove(Fruit s) {
        shapes.remove(s);
    }

    List<Fruit> getShapes() {
        return shapes;
    }

    // MVC methods
    // Basic MVC methods to bind view and model together.
    public void addObserver(Observer observer) {
        super.addObserver(observer);
    }

    // a helper to make it easier to initialize all observers
    void initObservers() {
        setChanged();
        notifyObservers();
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
        super.deleteObserver(observer);
        setChanged();
        notifyObservers();
    }

    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
        setChanged();
        notifyObservers();
    }
}
