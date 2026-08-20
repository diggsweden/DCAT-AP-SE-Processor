// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.validate;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.SHACL;

public class RDFValidationError {
    public String focusNode;
    public String path;
    public String value;
    public String message;
    public String constraintType;
    public String severity;

    public RDFValidationError() {
    }

    public RDFValidationError(Resource violation, Model report) {
        this.focusNode = readString(report, violation, SHACL.FOCUS_NODE);
        this.path = readString(report, violation, SHACL.RESULT_PATH);
        this.constraintType = shortValue(readString(report, violation, SHACL.SOURCE_CONSTRAINT_COMPONENT));
        this.value = readString(report, violation, SHACL.VALUE);
        this.severity = shortValue(readString(report, violation, SHACL.RESULT_SEVERITY));
        this.message = effectiveMessage(readString(report, violation, SHACL.RESULT_MESSAGE));
    }

    @Override
    public String toString() {
        return String.format( "Severity: %s%nConstraintType: %s%nNode: %s%nPath: %s%nValue: %s%nMessage: %s",
            severity, constraintType, focusNode, path, value, message);
    }

    private static String readString(Model report, Resource subject, IRI predicate) {
        return report.filter(subject, predicate, null).stream()
                .findFirst()
                .map(st -> st.getObject().stringValue())
                .orElse(null);
    }

    // Take the local name: "...#DatatypeConstraintComponent" -> "DatatypeConstraintComponent"
    private String shortValue(String type){
        if (type == null) return null;
        
        int hash = type.lastIndexOf('#');
        return hash >= 0 ? type.substring(hash + 1) : type;
    }

    // Message is often null, this gives a message based on constraint type  
    public String effectiveMessage(String message) {
        if (message != null) return message;
        if (this.constraintType == null) return null;
         
        return switch (this.constraintType) {
            case "DatatypeConstraintComponent" -> "Value has wrong datatype";
            case "MinCountConstraintComponent" -> "Mandatory value missing";
            case "MaxCountConstraintComponent" -> "Too many values";
            case "PatternConstraintComponent"  -> "Value does not match required pattern";
            case "ClassConstraintComponent"    -> "Value is not of the required class";
            case "NodeKindConstraintComponent" -> "Value has wrong node kind (IRI/literal/blank)";
            case "InConstraintComponent"       -> "Value is not in the allowed set";
            default                            -> this.constraintType;
        };
    }
}