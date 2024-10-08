package sheet.range.boundaries.impl;

import sheet.coordinate.api.Coordinate;
import sheet.range.boundaries.api.Boundaries;

import java.io.Serializable;

public class BoundariesImpl implements Boundaries, Serializable {
    private Coordinate from;
    private Coordinate to;

    private BoundariesImpl(Coordinate from, Coordinate to) {
        this.from = from;
        this.to = to;
    }

    public static Boundaries create(Coordinate from, Coordinate to) {
        return new BoundariesImpl(from, to);
    }

    @Override
    public Coordinate getFrom() {
        return this.from;
    }

    @Override
    public Coordinate getTo() {
        return this.to;
    }


}
