package net.jonathangiles.azure.backcompat;

public class SDK {
    private String name;
    private String oldRelease;
    private String newRelease;

    public SDK() {

    }

    public SDK(String name, String oldRelease, String newRelease) {
        this.name = name;
        this.oldRelease = oldRelease;
        this.newRelease = newRelease;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOldRelease() {
        return oldRelease;
    }

    public void setOldRelease(String oldRelease) {
        this.oldRelease = oldRelease;
    }

    public String getNewRelease() {
        return newRelease;
    }

    public void setNewRelease(String newRelease) {
        this.newRelease = newRelease;
    }

    @Override
    public String toString() {
        return "SDK [\n" +
                "  name = " + name + '\n' +
                "  oldRelease = " + oldRelease + '\n' +
                "  newRelease = " + newRelease + '\n' +
                ']';
    }
}
