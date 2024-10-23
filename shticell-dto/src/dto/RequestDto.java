package dto;

import dto.enums.PermissionType;
import dto.enums.Status;
import engine.permissions.request.Request;

public class RequestDto {
    public String requesterName;
    public PermissionType permissionType;
    public Status status;

    public RequestDto() {}

    public RequestDto(Request request) {
        this.requesterName = request.getRequesterName();
        this.permissionType = request.getPermissionType();
        this.status = request.getStatus();
    }

}
