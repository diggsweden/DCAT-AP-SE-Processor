// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.validate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class RDFValidationErrorTest {

    @ParameterizedTest
    @CsvSource({
        "DatatypeConstraintComponent, Value has wrong datatype",
        "MinCountConstraintComponent, Mandatory value missing",
        "MaxCountConstraintComponent, Too many values",
        "PatternConstraintComponent,  Value does not match required pattern",
        "ClassConstraintComponent,    Value is not of the required class",
        "NodeKindConstraintComponent, Value has wrong node kind (IRI/literal/blank)",
        "InConstraintComponent,       Value is not in the allowed set",
        "UnknownConstraintComponent,  UnknownConstraintComponent"
    })
    void testThatNullMessageFallsBackBasedOnConstraintType(String constraintType, String expected) {
        RDFValidationError error = new RDFValidationError();
        error.constraintType = constraintType;

        assertEquals(expected, error.effectiveMessage(null));
    }

    @Test
    void testThatExistingMessageIsReturnedUnchanged() {
        RDFValidationError error = new RDFValidationError();
        error.constraintType = "DatatypeConstraintComponent";
        String shapeMessage = "Validation message from shapes";
        String expected = error.effectiveMessage(shapeMessage);

        assertEquals(shapeMessage, expected);
    }

    @Test
    void testThatNullMessageAndNullConstraintTypeReturnsNull() {
        RDFValidationError error = new RDFValidationError();
        assertNull(error.effectiveMessage(null));
    }

    @ParameterizedTest
    @CsvSource({
        "'http://www.w3.org/ns/shacl#DatatypeConstraintComponent',  DatatypeConstraintComponent",
        "'http://example.org/MyConstraint',                         http://example.org/MyConstraint",
    })
    void testThatShortValueExtractsLocalName(String constraintIri, String expected) {
        IRI violation = Values.iri("https://example.org/violation/1");
        LinkedHashModel report = new LinkedHashModel();
        report.add(violation, SHACL.SOURCE_CONSTRAINT_COMPONENT, Values.iri(constraintIri));
    
        // shortValue() is called from the constructor of RDFValidationError
        RDFValidationError error = new RDFValidationError(violation, report);
    
        assertEquals(expected, error.constraintType);
    }
}