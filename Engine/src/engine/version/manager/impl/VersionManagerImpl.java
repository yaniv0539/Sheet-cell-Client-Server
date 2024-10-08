package engine.version.manager.impl;

import engine.version.manager.api.VersionManager;
import sheet.api.Sheet;
import sheet.api.SheetGetters;
import sheet.impl.SheetImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VersionManagerImpl implements VersionManager, Serializable {

    private int currentVersion = 0;

    private final List<SheetGetters> versions;

    private VersionManagerImpl() {
        this.versions = new ArrayList<>();
    }

    public static VersionManagerImpl create() {
        return new VersionManagerImpl();
    }

    public List<SheetGetters> getVersions() {
        return Collections.unmodifiableList(this.versions);
    }

    @Override
    public SheetGetters getVersion(int version) {
        
        for (SheetGetters sheetGetters : this.versions) {
            if (sheetGetters.getVersion() == version) {
                return sheetGetters;
            }
        }

        throw new IllegalArgumentException("Version " + version + " not found");
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    @Override
    public void addVersion(Sheet sheet) {
        this.versions.add(copySheet(sheet));
        currentVersion++;
    }

    public void increaseVersion(Sheet sheet) {
        sheet.setVersion(sheet.getVersion() + 1);
    }

    public void decreaseVersion(Sheet sheet) {
        sheet.setVersion(sheet.getVersion() - 1);
    }

    @Override
    public void clearVersions() {
        currentVersion = 0;
        this.versions.clear();
    }

    private Sheet copySheet(Sheet sheet) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(sheet);
            oos.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            return (SheetImpl) ois.readObject();
        } catch (Exception e) {
            // deal with the runtime error that was discovered as part of invocation
            throw new RuntimeException(e);
        }
    }
}
