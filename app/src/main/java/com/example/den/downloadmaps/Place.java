package com.example.den.downloadmaps;

import android.support.annotation.NonNull;

import java.util.Comparator;

/**
 * Created by den on 29.01.2018.
 */

public class Place implements Comparable {
    private String name;
    private int depth;
    private boolean map;

    public Place(String name, int depth, boolean map) {
        this.name = name;
        this.depth = depth;
        this.map = map;
    }//Place

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isMap() {
        return map;
    }

    public void setMap(boolean map) {
        this.map = map;
    }

    public static final Comparator<Place> COMPARE_BY_NAME = new Comparator<Place>() {
        @Override
        public int compare(Place name, Place name1) {
            return name.getName().compareTo(name1.getName());
        }
    };

    @Override
    public int compareTo(@NonNull Object o) {
        return 0;
    }
}//Place
