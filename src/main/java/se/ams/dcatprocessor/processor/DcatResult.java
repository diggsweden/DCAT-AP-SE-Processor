// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

import se.ams.dcatprocessor.rdf.quality.QualityReport;

public record DcatResult(String rdf, String errorReport, QualityReport qualityReport) {
    
    public boolean hasErrors() {
        return errorReport != null && !errorReport.isEmpty();
    }

    public static DcatResult success(String rdf, QualityReport qualityReport) {
        return new DcatResult(rdf, null, qualityReport);
    }

    public static DcatResult errors(String errorReport) {
        return new DcatResult(null, errorReport, null);
    }
}
