package net.jonathangiles.azure.backcompat.report;

import org.revapi.Difference;
import org.revapi.Report;

import java.util.List;

public class Result {
    private String oldAPI;
    private String newAPI;
    private List<Difference> differences;

    public static Result create(Report report) {
        Result r = new Result();
        r.oldAPI = report.getOldElement() == null ? "null" : report.getOldElement().getFullHumanReadableString();
        r.newAPI = report.getNewElement() == null ? "null" : report.getNewElement().getFullHumanReadableString();
        r.differences = report.getDifferences();
        return r;
    }
}
