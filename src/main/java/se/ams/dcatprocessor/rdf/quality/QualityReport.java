// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.quality;

import java.util.List;

public record QualityReport(boolean completed, List<MainClassQuality> mainClasses) {

    public record MainClassQuality(String mainClass, String uri, List<QualityIssue> issues) {
    }

    public record QualityIssue(String property, String message) {
    }

    public static QualityReport notCompleted() {
        return new QualityReport(false, List.of());
    }
}