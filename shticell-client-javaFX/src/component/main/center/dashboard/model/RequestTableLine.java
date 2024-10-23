package component.main.center.dashboard.model;

import dto.enums.PermissionType;
import dto.enums.Status;

public class RequestTableLine {
    private String userName;
    private PermissionType permissionRequested;
    private Status requestStatus;

    public RequestTableLine(String userName, PermissionType permissionRequested, Status requestStatus) {
        this.userName = userName;
        this.permissionRequested = permissionRequested;
        this.requestStatus = requestStatus;
    }

    public String getUserName() {
        return userName;
    }

    public PermissionType getPermissionRequested() {
        return permissionRequested;
    }

    public Status getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(Status ownerAnswer) {
        this.requestStatus = ownerAnswer;
    }
}
