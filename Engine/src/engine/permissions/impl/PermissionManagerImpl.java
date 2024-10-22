package engine.permissions.impl;

import engine.permissions.api.PermissionManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PermissionManagerImpl implements PermissionManager {
    private final String owner;
    private final Set<String> readers;
    private final Set<String> pendingToRead;
    private final Set<String> writers;
    private final Set<String> pendingToWrite;

    public PermissionManagerImpl(String owner) {
        this.owner = owner;
        this.readers = new HashSet<>();
        this.pendingToRead = new HashSet<>();
        this.writers = new HashSet<>();
        this.pendingToWrite = new HashSet<>();
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
    public void addReader(String reader) {
        readers.add(reader);
    }

    @Override
    public void addWriter(String writer) {
        writers.add(writer);
    }

    @Override
    public boolean removeReader(String reader) {
        return readers.remove(reader);
    }

    @Override
    public boolean removeWriter(String writer) {
        return writers.remove(writer);
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
