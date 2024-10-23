package engine.permissions.api;

import dto.enums.PermissionType;
import engine.permissions.request.Request;

import java.util.Set;

public interface PermissionManager {
    String getOwner();
    Set<String> getReaders();
    Set<String> getWriters();
    Set<Request> getRequestsHistory();
    void addRequest(String userName, PermissionType permissionType);
    void confirmRequest(String requesterName, PermissionType permissionType);
    void denyRequest(String requesterName, PermissionType permissionType);

    boolean canRead(String reader);
    boolean canWrite(String writer);
}
