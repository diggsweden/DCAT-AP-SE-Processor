// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.testutil;

import java.io.StringWriter;
import java.util.function.Consumer;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class TestRDFBuilder {

    private final ModelBuilder builder;
    
    private TestRDFBuilder(ModelBuilder builder) {
        this.builder = builder;
    }
    
    public ModelBuilder builder() {
        return builder;
    }

    public TestRDFBuilder modify(Consumer<ModelBuilder> fn) {
        fn.accept(builder);
        return this;
    }

    public TestRDFBuilder remove(Resource subject, IRI predicate) {
        builder.build().remove(subject, predicate, null);
        return this;
    }

    public static TestRDFBuilder minimalValidRdf() {
        ModelBuilder b = new ModelBuilder()
            .setNamespace("dcat", DCAT.NAMESPACE)
            .setNamespace("dcterms", DCTERMS.NAMESPACE)
            .setNamespace("foaf", FOAF.NAMESPACE)

            .subject("https://example.org/catalog/1")
                .add(RDF.TYPE, DCAT.CATALOG)
                .add(DCTERMS.TITLE, Values.literal("Testkatalog", "sv"))
                .add(DCTERMS.DESCRIPTION, Values.literal("En katalog för test", "sv"))
                .add(DCTERMS.PUBLISHER, Values.iri("https://example.org/agent/1"))
                .add(DCTERMS.LICENSE, Values.iri("http://creativecommons.org/publicdomain/zero/1.0/"))
                .add(DCAT.HAS_DATASET, Values.iri("https://example.org/dataset/1"))

            .subject("https://example.org/agent/1")
                .add(RDF.TYPE, FOAF.AGENT)
                .add(FOAF.NAME, "Test Org")
                .add(DCTERMS.TYPE, Values.iri("http://purl.org/adms/publishertype/NationalAuthority"))

            .subject("https://example.org/dataset/1")
                .add(RDF.TYPE, DCAT.DATASET)
                .add(DCTERMS.TITLE, Values.literal("Testdataset", "sv"))
                .add(DCTERMS.DESCRIPTION, Values.literal("Ett dataset för test", "sv"))
                .add(DCTERMS.PUBLISHER, Values.iri("https://example.org/agent/1"));

        return new TestRDFBuilder(b);
    }

    public String toRdfString() {
        StringWriter sw = new StringWriter();
        Rio.write(builder.build(), sw, RDFFormat.RDFXML);
        return sw.toString();
    }
}
