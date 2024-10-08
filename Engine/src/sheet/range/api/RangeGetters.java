package sheet.range.api;

import sheet.coordinate.api.Coordinate;
import sheet.range.boundaries.api.Boundaries;

import java.util.Collection;

public interface RangeGetters {
    String getName();
    Boundaries getBoundaries();
    Collection<Coordinate> toCoordinateCollection();
}
