package engine.permissions.api;

import dto.enums.PermissionType;
import dto.enums.Status;
import engine.permissions.request.Request;

import java.util.List;
import java.util.Set;

public interface PermissionManager {
    String getOwner();
    Set<String> getReaders();
    Set<String> getWriters();
    List<Request> getRequestsHistory();
    void addRequest(String userName, PermissionType permissionType);
    void confirmRequest(String requesterName, PermissionType permissionType);
    void denyRequest(String requesterName, PermissionType permissionType);

    boolean canRead(String reader);
    boolean canWrite(String writer);

    PermissionType getPermission(String userName);

    void updateRequest(String userName, PermissionType permissionType, Status status, Status response);
}
