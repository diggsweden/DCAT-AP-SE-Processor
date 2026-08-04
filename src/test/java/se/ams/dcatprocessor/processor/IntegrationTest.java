// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import se.ams.dcatprocessor.converter.Converter;
import se.ams.dcatprocessor.testutil.TestHelper;

@SpringBootTest 
public class IntegrationTest {

    @Autowired
    private ObjectProvider<Manager> managerProvider;

    private Manager manager;
    private final ValueFactory vf = SimpleValueFactory.getInstance();
    private final String API_DEF_FILE = "src/test/resources/apidef/json_v3/json_oas_301.json";
    
    @BeforeEach
	void setup() throws Exception {
        TestHelper.resetSingeltons();
        Converter.errors.clear();
		manager = managerProvider.getObject();
	}

    @Test
    void testThatByteSizeIsNonNegativeInteger() throws RDFParseException, UnsupportedRDFormatException, IOException{
        IRI distribution = vf.createIRI("https://www.example.se/#distributionC");

        String result = manager.createDcatFromFile(API_DEF_FILE);
        Model model = Rio.parse(new StringReader(result), "", RDFFormat.RDFXML);

        Literal actual = Models.objectLiteral(
                model.filter(distribution, DCAT.BYTE_SIZE, null))
            .orElseThrow(() -> new AssertionError("No byteSize on " + distribution));

        assertEquals("3000", actual.getLabel(), "byteSize value");
        assertEquals(XSD.NON_NEGATIVE_INTEGER, actual.getDatatype(), "byteSize datatype");
    }
}
