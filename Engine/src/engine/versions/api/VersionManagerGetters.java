package engine.versions.api;

import sheet.api.Sheet;

import java.util.List;

public interface VersionManagerGetters {
    List<Sheet> getVersions();
    Sheet getVersion(int version);
    Sheet getLastVersion();
}
