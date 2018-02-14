package net.jonathangiles.azure.backcompat;

import org.revapi.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CollectingReporter implements Reporter {
    private final List<Report> reports = new ArrayList<>();

    public List<Report> getReports() {
        return reports;
    }

    @Override
    public String getExtensionId() {
        return null;
    }

    @Override
    public Reader getJSONSchema() {
        return null;
    }

    @Override
    public void initialize(AnalysisContext properties) {
    }

    @Override
    public void report(Report report) {
        if (report.getDifferences().isEmpty()) {
            return;
        }

        DifferenceSeverity maxReportedSeverity = DifferenceSeverity.NON_BREAKING;
        for (Difference d : report.getDifferences()) {
            for (DifferenceSeverity c : d.classification.values()) {
                if (c.compareTo(maxReportedSeverity) > 0) {
                    maxReportedSeverity = c;
                }
            }
        }

        if (maxReportedSeverity.compareTo(Main.MIN_SEVERITY_LEVEL) < 0) {
            return;
        }

        reports.add(report);
    }

    @Override
    public void close() throws IOException {
    }
}