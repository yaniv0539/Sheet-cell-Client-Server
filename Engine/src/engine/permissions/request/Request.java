package engine.permissions.request;

import dto.enums.PermissionType;
import dto.enums.Status;

import java.util.Objects;

public class Request {
    private String requesterName;
    private PermissionType permissionType;
    private Status status;

    public Request() {}

    public Request(String requesterName, PermissionType permissionType, Status status) {
        this.requesterName = requesterName;
        this.permissionType = permissionType;
        this.status = status;
    }

    public String getRequesterName() {
        return requesterName;
    }

    private void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    private void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(requesterName, request.requesterName) && permissionType == request.permissionType && status == request.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requesterName, permissionType, status);
    }
}
