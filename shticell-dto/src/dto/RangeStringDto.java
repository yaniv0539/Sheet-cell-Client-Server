package dto;

public class RangeStringDto {
    private String name;
    private String boundaries;

    public RangeStringDto(String name, String boundaries) {
        this.name = name;
        this.boundaries = boundaries;
    }

    public String getName() {
        return name;
    }

    public String getBoundaries() {
        return boundaries;
    }
}
