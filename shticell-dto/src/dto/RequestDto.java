package dto;

import dto.enums.PermissionType;
import dto.enums.Status;

public class RequestDto {
    private PermissionType requestType;
    private Status status;
    private String userName;


    public RequestDto(PermissionType requestType, Status status, String userName) {
        this.requestType = requestType;
        this.status = status;
        this.userName = userName;
    }

    public PermissionType getRequestType() {
        return requestType;
    }

    public Status getStatus() {
        return status;
    }
}
