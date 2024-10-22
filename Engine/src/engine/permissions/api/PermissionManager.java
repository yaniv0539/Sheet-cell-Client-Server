package engine.permissions.api;

import java.util.Set;

public interface PermissionManager {
    String getOwner();
    Set<String> getReaders();
    Set<String> getWriters();
    void addReader(String reader);
    void addWriter(String writer);
    boolean removeReader(String reader);
    boolean removeWriter(String writer);
    boolean canRead(String reader);
    boolean canWrite(String writer);
}
