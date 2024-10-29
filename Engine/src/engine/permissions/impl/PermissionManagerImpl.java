package engine.permissions.impl;

import dto.enums.PermissionType;
import dto.enums.Status;
import engine.permissions.api.PermissionManager;
import engine.permissions.request.Request;

import java.util.*;

public class PermissionManagerImpl implements PermissionManager {
    private final String owner;
    private final Set<String> readers;
    private final Set<String> writers;
    private final List<Request> requestsHistory;

    public PermissionManagerImpl(String owner) {
        this.owner = owner;
        this.readers = new HashSet<>();
        this.writers = new HashSet<>();
        this.requestsHistory = new ArrayList<>();
        this.requestsHistory.add(new Request(owner,PermissionType.OWNER,Status.CONFIRMED));
    }

    public static PermissionManagerImpl create(String owner) {
        return new PermissionManagerImpl(owner);
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public Set<String> getReaders() {
        return Collections.unmodifiableSet(readers);
    }

    @Override
    public Set<String> getWriters() {
        return Collections.unmodifiableSet(writers);
    }

    @Override
    public List<Request> getRequestsHistory() {
        return Collections.unmodifiableList(requestsHistory);
    }

    @Override
    public PermissionType getPermission(String userName) {
        PermissionType permissionType = PermissionType.NONE;

        if(owner.equals(userName)) {
            permissionType = PermissionType.OWNER;

        } else if (readers.contains(userName)) {
            permissionType = PermissionType.READER;
        }
        else if (writers.contains(userName)) {
            permissionType = PermissionType.WRITER;
        }

        return permissionType;
    }

    @Override
    public void addRequest(String requesterName, PermissionType permissionType) {
        Request pendingRequest = new Request(requesterName, permissionType, Status.PENDING);


        synchronized (this.requestsHistory) {

            if (requestsHistory.contains(pendingRequest)) {
                throw new RuntimeException("Request already pending.");
            }

            requestsHistory.add(pendingRequest);
        }
    }

    @Override
    public void confirmRequest(String requesterName, PermissionType permissionType) {
        Request pendingRequest = new Request(requesterName, permissionType, Status.PENDING);
        Request confirmedRequest = new Request(requesterName, permissionType, Status.CONFIRMED);


            if (!requestsHistory.contains(pendingRequest)) {
                throw new RuntimeException("Cannot find pending request for this requester.");
            }
            //it contains : itay add
            int index = requestsHistory.indexOf(pendingRequest);
            requestsHistory.set(index, confirmedRequest); //switch


        if (permissionType == PermissionType.WRITER) {
            writers.add(requesterName);
            readers.remove(requesterName);
        } else if (permissionType == PermissionType.READER) {
            readers.add(requesterName);
            writers.remove(requesterName);
        } else {
            throw new IllegalArgumentException("Unsupported permission type: " + permissionType);
        }
    }

    @Override
    public void denyRequest(String requesterName, PermissionType permissionType) {
        Request pendingRequest = new Request(requesterName, permissionType, Status.PENDING);
        Request deniedRequest = new Request(requesterName, permissionType, Status.DENIED);

            if (!requestsHistory.contains(pendingRequest)) {
                throw new RuntimeException("Cannot find pending request for this requester.");
            }

            int index = requestsHistory.indexOf(pendingRequest);
            requestsHistory.set(index, deniedRequest); //switch

    }

    @Override
    public void updateRequest(String userName, PermissionType permissionType, Status status, Status response) {

        if(status != Status.PENDING) {
            throw new RuntimeException("Request is not PENDING.");
        }

        if (response.equals(Status.CONFIRMED)) {
            confirmRequest(userName, permissionType);
        } else if (response.equals(Status.DENIED)) {
            denyRequest(userName, permissionType);
        }

    }

    @Override
    public boolean canRead(String reader) {
        return readers.contains(reader) || writers.contains(reader) || owner.equals(reader);
    }

    @Override
    public boolean canWrite(String writer) {
        return writers.contains(writer) || owner.equals(writer);
    }
}
