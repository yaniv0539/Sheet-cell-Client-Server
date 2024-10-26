package component.main.center.dashboard.model;

import dto.enums.PermissionType;
import dto.enums.Status;

import java.util.Objects;

public class RequestTableLine {
    private String userName;
    private PermissionType permissionType;
    private Status requestStatus;

    public RequestTableLine(String userName, PermissionType permissionType, Status requestStatus) {
        this.userName = userName;
        this.permissionType = permissionType;
        this.requestStatus = requestStatus;
    }

    public String getUserName() {
        return userName;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public Status getRequestStatus() {
        return requestStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestTableLine that = (RequestTableLine) o;
        return Objects.equals(userName, that.userName) && permissionType == that.permissionType && requestStatus == that.requestStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, permissionType, requestStatus);
    }
}
