package engine.versions.impl;

import engine.versions.api.VersionManager;
import sheet.api.Sheet;
import sheet.impl.SheetImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VersionManagerImpl implements VersionManager, Serializable {

    private final static int FIRST_VERSION = 1;
    private final List<Sheet> versions;

    private VersionManagerImpl() {
        this.versions = new ArrayList<>();
    }

    public static VersionManagerImpl create() {
        return new VersionManagerImpl();
    }

    @Override
    public List<Sheet> getVersions() {
        return this.versions;
    }

    @Override
    public Sheet getVersion(int version) {
        
        for (Sheet sheet : this.versions) {
            if (sheet.getVersion() == version) {
                return sheet;
            }
        }

        throw new IllegalArgumentException("Version " + version + " not found");
    }

    @Override
    public Sheet getLastVersion() {
        return this.versions.getLast();
    }

    @Override
    public void init(Sheet sheet) {
        this.versions.clear();
        Sheet firstVersion = copySheet(sheet);
        firstVersion.setVersion(FIRST_VERSION);
        this.versions.add(firstVersion);
    }

    @Override
    public void makeNewVersion() {
        Sheet lastVersion = getLastVersion();

        if (lastVersion == null) {
            throw new IllegalArgumentException("No version found, please use the init function to make the first version");
        }

        Sheet newVersion = copySheet(lastVersion);
        newVersion.setVersion(newVersion.getVersion() + 1);
        this.versions.add(newVersion);
    }

    @Override
    public void deleteLastVersion() {
        this.versions.removeLast();
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

//    @Override
//    public void increaseVersion(Sheet sheet) {
//        sheet.setVersion(sheet.getVersion() + 1);
//    }
//
//    @Override
//    public void decreaseVersion(Sheet sheet) {
//        sheet.setVersion(sheet.getVersion() - 1);
//    }
//
//    @Override
//    public void clearVersions() {
//        this.versions.clear();
//    }
}
