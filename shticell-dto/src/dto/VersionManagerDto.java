package dto;

import engine.version.manager.api.VersionManagerGetters;

import java.util.ArrayList;
import java.util.List;

public class VersionManagerDto {

    private final List<SheetDto> versions;

    public VersionManagerDto(VersionManagerGetters versionManagerGetters) {
        this.versions = new ArrayList<>();
        versionManagerGetters.getVersions().forEach(version -> versions.add(new SheetDto(version)));
    }
}
