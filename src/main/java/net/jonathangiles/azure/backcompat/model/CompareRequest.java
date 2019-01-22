package net.jonathangiles.azure.backcompat.model;

import net.jonathangiles.azure.backcompat.maven.Version;

public class CompareRequest {
    private String ga;
    private Version oldVersion;
    private Version newVersion;

    public CompareRequest() {

    }

    public CompareRequest(String ga, Version oldVersion, Version newVersion) {
        this.ga = ga;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    public String getGA() {
        return ga;
    }

    public Version getOldVersion() {
        return oldVersion;
    }

    public Version getNewVersion() {
        return newVersion;
    }

    public String getOldVersionGAV() {
        return ga + ":" + oldVersion.getVersionString();
    }

    public String getNewVersionGAV() {
        return ga + ":" + newVersion.getVersionString();
    }

    public String getVersionsString() {
        return oldVersion.getVersionString() + "-" + newVersion.getVersionString();
    }

    @Override
    public String toString() {
        return "CompareRequest [\n" +
                "  ga = " + ga + '\n' +
                "  oldRelease = " + oldVersion + '\n' +
                "  newRelease = " + newVersion + '\n' +
                ']';
    }
}
