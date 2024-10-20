package dto;

import dto.enums.PermissionType;
import dto.enums.Status;

public class RequestDto {
    private PermissionType requestType;
    private Status status;


    public RequestDto(PermissionType requestType, Status status) {
        this.requestType = requestType;
        this.status = status;
    }

    public PermissionType getRequestType() {
        return requestType;
    }

    public Status getStatus() {
        return status;
    }
}
