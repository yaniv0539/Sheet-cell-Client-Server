package engine.permissions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PermissionManager {
    private final String owner;
    private final Set<String> readers;
    private final Set<String> writers;

    public PermissionManager(String owner) {
        this.owner = owner;
        this.readers = new HashSet<>();
        this.writers = new HashSet<>();
    }

    public String getOwner() {
        return owner;
    }

    public Set<String> getReaders() {
        return Collections.unmodifiableSet(readers);
    }

    public Set<String> getWriters() {
        return Collections.unmodifiableSet(writers);
    }

    public void addReader(String reader) {
        readers.add(reader);
    }

    public void addWriter(String writer) {
        writers.add(writer);
    }

    public boolean removeReader(String reader) {
        return readers.remove(reader);
    }

    public boolean removeWriter(String writer) {
        return writers.remove(writer);
    }
}
