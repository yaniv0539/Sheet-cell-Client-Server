package engine.version.manager.api;

import sheet.api.SheetGetters;

import java.util.List;

public interface VersionManagerGetters {
    List<SheetGetters> getVersions();
    SheetGetters getVersion(int version);
}
