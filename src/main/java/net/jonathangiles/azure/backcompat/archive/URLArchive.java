package net.jonathangiles.azure.backcompat.archive;

import org.revapi.Archive;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class URLArchive implements Archive {
    private URL url;

    public URLArchive(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public URLArchive(URL url) {
        this.url = url;
    }

    public String getName() {
        return this.url.toString();
    }

    public InputStream openStream() throws IOException {
        return this.url.openStream();
    }
}