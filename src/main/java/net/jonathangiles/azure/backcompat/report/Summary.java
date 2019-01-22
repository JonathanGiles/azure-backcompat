package net.jonathangiles.azure.backcompat.report;

import net.jonathangiles.azure.backcompat.model.CompareRequest;

public class Summary {
    private final String name;
    private final String oldVersion;
    private final String newVersion;
    private final boolean success;
    private final int issues;

    public Summary(CompareRequest request, int issues) {
        this.name = request.getGA();
        this.oldVersion = request.getOldVersion().getVersionString();
        this.newVersion = request.getNewVersion().getVersionString();
        this.issues = issues;
        this.success = issues == 0;
    }
}
