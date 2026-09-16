// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

public record DcatResult(String rdf, String errorReport) {
    
    public boolean hasErrors() {
        return errorReport != null && !errorReport.isEmpty();
    }

    public static DcatResult success(String rdf) {
        return new DcatResult(rdf, null);
    }

    public static DcatResult errors(String errorReport) {
        return new DcatResult(null, errorReport);
    }
}
