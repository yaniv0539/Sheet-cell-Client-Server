package dto;

public class BoundariesStringDto {
    private String name;
    private String boundaries;

    public BoundariesStringDto(String name, String boundaries) {
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
