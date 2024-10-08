package engine.version.manager.api;

import sheet.api.Sheet;

public interface VersionManagerSetters {
    void addVersion(Sheet sheet);
    void clearVersions();
    void increaseVersion(Sheet sheet);
    void decreaseVersion(Sheet sheet);
}
