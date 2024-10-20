package dto;

import java.util.List;
import java.util.Map;

public class PermissionDto {
    private LayoutDto layout;
    private String owner;
    private List<String> readers;
    private List<String> writers;
    private Map<String, RequestDto> requests;
}
