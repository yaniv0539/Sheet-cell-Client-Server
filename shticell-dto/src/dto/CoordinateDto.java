package dto;

import sheet.coordinate.api.Coordinate;

public class CoordinateDto {
    public int row;
    public int column;

    public CoordinateDto(Coordinate coordinate) {
        this.row = coordinate.getRow();
        this.column = coordinate.getCol();
    }
}
