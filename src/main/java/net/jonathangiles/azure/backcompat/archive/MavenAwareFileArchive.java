package net.jonathangiles.azure.backcompat.archive;

import org.revapi.Archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MavenAwareFileArchive implements Archive {
    private final File file;
    private final String gav;

    public MavenAwareFileArchive(String gav, File file) {
        this.gav = gav;
        this.file = file;
    }

    public String getName() {
        return this.gav;
    }

    public InputStream openStream() throws IOException {
        return new FileInputStream(file);
    }
}