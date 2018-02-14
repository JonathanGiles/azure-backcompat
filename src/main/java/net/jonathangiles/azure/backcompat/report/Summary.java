package net.jonathangiles.azure.backcompat.report;

public class Summary {
    private String name;
    private boolean success;
    private int issues;

    public Summary(String name, int issues) {
        this.name = name;
        this.issues = issues;
        this.success = issues == 0;
    }
}
