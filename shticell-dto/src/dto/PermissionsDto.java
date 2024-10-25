package dto;

import engine.permissions.api.PermissionManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public record PermissionsDto(
        String owner,
        Set<String> readers,
        Set<String> writers,
        Set<RequestDto> requests
) {
    public PermissionsDto(PermissionManager permissionManager) {
        this(
                permissionManager.getOwner(),
                permissionManager.getReaders(),
                permissionManager.getWriters(),
                createRanges(permissionManager)
        );
    }

    private static Set<RequestDto> createRanges(PermissionManager permissionManager) {
        Set<RequestDto> requests = new HashSet<>();
        permissionManager.getRequestsHistory().forEach(request -> requests.add(new RequestDto(request)));
        return Collections.unmodifiableSet(requests);  // Ensure immutability
    }
}
