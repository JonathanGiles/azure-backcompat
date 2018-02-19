package net.jonathangiles.azure.backcompat.report;

import com.google.gson.annotations.SerializedName;
import org.revapi.DifferenceSeverity;
import static org.revapi.DifferenceSeverity.*;

public enum Grade {

    @SerializedName("grade-fyi")
    FYI,

    @SerializedName("grade-low")
    LOW_RISK,

    @SerializedName("grade-mid")
    MID_RISK,

    @SerializedName("grade-high")
    HIGH_RISK,;

    public static Grade compute(DifferenceSeverity binary, DifferenceSeverity source) {
        if (binary.equals(BREAKING) || source.equals(BREAKING)) {
            return HIGH_RISK;
        } else if (binary.equals(POTENTIALLY_BREAKING) || source.equals(POTENTIALLY_BREAKING)) {
            return MID_RISK;
        } else if (binary.equals(EQUIVALENT) && source.equals(EQUIVALENT)) {
            return LOW_RISK;
        } else {
            return FYI;
        }
    }
}
