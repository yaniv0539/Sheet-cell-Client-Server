package dto;

import dto.enums.PermissionType;
import dto.enums.Status;
import engine.permissions.request.Request;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestDto that = (RequestDto) o;
        return status == that.status && Objects.equals(requesterName, that.requesterName) && permissionType == that.permissionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requesterName, permissionType, status);
    }
}
