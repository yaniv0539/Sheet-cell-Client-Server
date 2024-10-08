package sheet.range.impl;

import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateFactory;
import sheet.range.api.Range;
import sheet.range.boundaries.api.Boundaries;

import java.io.Serializable;
import java.util.*;

public class RangeImpl implements Range, Serializable {

    String name;
    Boundaries boundaries;

    private RangeImpl(String name, Boundaries boundaries) {
        this.name = name.toUpperCase();
        this.boundaries = boundaries;
    }

    public static RangeImpl create(String name, Boundaries boundaries) {
        Coordinate from = boundaries.getFrom();
        Coordinate to = boundaries.getTo();

        if (from.getRow() > to.getRow() || from.getCol() > to.getCol()) {
            throw new IllegalArgumentException("Invalid boundaries! From: " + from + " To: " + to + "is not logical.");
        }
        return new RangeImpl(name, boundaries);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Boundaries getBoundaries() {
        return this.boundaries;
    }

    @Override
    public void setBoundaries(Boundaries boundaries) {
        this.boundaries = boundaries;
    }

    @Override
    public Collection<Coordinate> toCoordinateCollection() {
        //logic
        Set<Coordinate> coordinates = new HashSet<>();
        Coordinate from = boundaries.getFrom();
        Coordinate to = boundaries.getTo();

        for(int row = from.getRow(); row <= to.getRow(); row++) {
            for(int col = from.getCol(); col <= to.getCol(); col++) {
                coordinates.add(CoordinateFactory.createCoordinate(row,col));
            }
        }
        return coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeImpl range = (RangeImpl) o;
        return Objects.equals(name, range.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
