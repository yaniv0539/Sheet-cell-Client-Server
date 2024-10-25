package dto;

import dto.enums.PermissionType;
import dto.enums.Status;
import engine.permissions.request.Request;

public record RequestDto(
        String requesterName,
        PermissionType permissionType,
        Status status
) {
    public RequestDto(Request request) {
        this(
                request.getRequesterName(),
                request.getPermissionType(),
                request.getStatus()
        );
    }
}
