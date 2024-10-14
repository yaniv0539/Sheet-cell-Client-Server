package engine.version.manager.api;

import sheet.api.Sheet;

public interface VersionManagerSetters {
    void init(Sheet sheet);
    void makeNewVersion();
    void deleteLastVersion();
//    void clearVersions();
//    void increaseVersion(Sheet sheet);
//    void decreaseVersion(Sheet sheet);
}
