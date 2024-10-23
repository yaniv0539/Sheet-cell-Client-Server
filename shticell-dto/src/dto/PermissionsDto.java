package dto;

import engine.permissions.api.PermissionManager;

import java.util.Set;

public class PermissionsDto {
    public String owner;
    public Set<String> readers;
    public Set<String> writers;
    public Set<RequestDto> requests;

    public PermissionsDto() {}

    public PermissionsDto(PermissionManager permissionManager) {
        this.owner = permissionManager.getOwner();
        this.readers = permissionManager.getReaders();
        this.writers = permissionManager.getWriters();
        permissionManager.getRequestsHistory().forEach(request -> this.requests.add(new RequestDto(request)));
    }
}
