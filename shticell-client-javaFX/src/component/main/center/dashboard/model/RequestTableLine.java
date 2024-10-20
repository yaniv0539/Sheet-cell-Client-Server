package component.main.center.dashboard.model;

import dto.enums.PermissionType;
import dto.enums.Status;

public class RequestTableLine {
    private String userName;
    private String sheetName;
    private PermissionType permissionRequested;
    private Status requestStatus;

    public RequestTableLine(String userName, String sheetName, PermissionType permissionRequested, Status requestStatus) {
        this.userName = userName;
        this.sheetName = sheetName;
        this.permissionRequested = permissionRequested;
        this.requestStatus = requestStatus;
    }

    public String getUserName() {
        return userName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public PermissionType getPermissionRequested() {
        return permissionRequested;
    }

    public Status getRequestStatus() {
        return requestStatus;
    }
}
