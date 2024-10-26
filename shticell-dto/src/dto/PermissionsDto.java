package dto;

import engine.permissions.api.PermissionManager;

import java.util.*;

public record PermissionsDto(
        String owner,
        Set<String> readers,
        Set<String> writers,
        List<RequestDto> requests
) {
    public PermissionsDto(PermissionManager permissionManager) {
        this(
                permissionManager.getOwner(),
                permissionManager.getReaders(),
                permissionManager.getWriters(),
                createRanges(permissionManager)
        );
    }

    private static List<RequestDto> createRanges(PermissionManager permissionManager) {
        List<RequestDto> requests = new ArrayList<>();
        permissionManager.getRequestsHistory().forEach(request -> requests.add(new RequestDto(request)));

        return Collections.unmodifiableList(requests);  // Ensure immutability
    }
}
