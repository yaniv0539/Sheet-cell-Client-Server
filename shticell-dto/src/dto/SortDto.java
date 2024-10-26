package dto;

import java.util.List;

public record SortDto(
        BoundariesDto boundariesDto,
        List<String> sortByColumns
) {}
