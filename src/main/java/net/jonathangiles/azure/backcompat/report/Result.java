package net.jonathangiles.azure.backcompat.report;

import org.revapi.CompatibilityType;
import org.revapi.Difference;
import org.revapi.Report;

import java.util.List;

public class Result {
    private String oldAPI;
    private String newAPI;
    private List<Difference> differences;
    private Grade grade;

    public static Result create(Report report) {
        Result r = new Result();
        r.oldAPI = report.getOldElement() == null ? "null" : report.getOldElement().getFullHumanReadableString();
        r.newAPI = report.getNewElement() == null ? "null" : report.getNewElement().getFullHumanReadableString();
        r.differences = report.getDifferences();

        // for now we only concern ourselves with the first difference
        Difference diff1 = r.differences.get(0);
        r.grade = Grade.compute(
                diff1.classification.get(CompatibilityType.BINARY),
                diff1.classification.get(CompatibilityType.SOURCE));

        return r;
    }
}
