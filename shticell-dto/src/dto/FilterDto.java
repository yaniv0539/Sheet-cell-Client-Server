package dto;

import java.util.List;

public record FilterDto(BoundariesDto boundariesDto, String filterByColumn, List<String> byValues) {
}
